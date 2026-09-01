#!/usr/bin/env python3
"""Generate the GitHub Actions CI pipeline described by this skill.

Writes two files into a target repository:
  - .github/workflows/ci.yml            (lint, unit-test, UI-test, build jobs)
  - .github/actions/job-set-up/action.yml  (shared JDK + Gradle setup)

This is a faithful, parameterized encoding of `template.md`. `template.md` and
`references/details.md` remain the source of truth for the *reasoning*; this
script just stamps out the files quickly so the agent doesn't retype ~200 lines
of YAML by hand. After running, review the result and adjust per the skill.

Stdlib only; runs anywhere Python 3.8+ is available. Example:

  python generate_ci.py --app-module app --variant Debug --jdk 17 --api-level 34
  python generate_ci.py --output ../other-repo --default-branch master --secret BASE_URL
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

WORKFLOW = """\
name: CI

on:
  push:
    branches:
      - __BRANCH__

  pull_request:
    types: [ opened, reopened, synchronize ]
    branches:
      - __BRANCH__

# Least-privilege token: this workflow only reads the repo and uploads artifacts.
permissions:
  contents: read

concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}

env:
  main_project_module: __APP_MODULE__
__SECRET_ENV__
jobs:
  linting:
    name: Run Lint Checks
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - name: Checkout the code
        uses: actions/checkout@v5

      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up

      - name: Run Android Lint
        run: ./gradlew lint__VARIANT__

      - name: Upload Lint Report
        if: always()  # upload even when lint fails - that's when it's most useful
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: ${{ github.workspace }}/**/build/reports/lint-results-*.html
          retention-days: 14

  unit_test:
    name: Unit Test
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - name: Checkout the code
        uses: actions/checkout@v5

      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up

      - name: Run Unit Tests
        run: ./gradlew test__VARIANT__UnitTest

      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-report
          path: ${{ github.workspace }}/**/build/reports/tests/
          retention-days: 14

  ui_test:
    name: UI Tests (Instrumented)
    needs: [ linting, unit_test ]
    runs-on: ubuntu-latest
    timeout-minutes: 45
    # Default branch only: instrumented UI tests need an emulator and are slower and
    # flakier than the JVM checks, so PRs stay gated on lint + unit tests and the
    # full on-device suite runs once the change has landed. Delete this `if:` to run
    # UI tests on every PR as well.
    if: github.ref == 'refs/heads/__BRANCH__'
    steps:
      - name: Checkout the code
        uses: actions/checkout@v5

      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up

      # The Android emulator needs KVM hardware acceleration to run at a usable
      # speed on the ubuntu-latest runner.
      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      # Boots an emulator, waits for it, then runs the connected (androidTest) suite.
      # __API_LEVEL__ must be >= the app's minSdk; pick a stable, widely cached image.
      - name: Run Instrumented (UI) Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: __API_LEVEL__
          arch: x86_64
          target: google_apis
          disable-animations: true
          script: ./gradlew connected__VARIANT__AndroidTest

      - name: Upload Instrumented Test Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: instrumented-test-report
          path: ${{ github.workspace }}/**/build/reports/androidTests/
          retention-days: 14

  build_and_upload:
    name: Build and Upload
    needs: [ linting, unit_test, ui_test ]
    runs-on: ubuntu-latest
    timeout-minutes: 30
    # ui_test is in needs so a failing UI test on the default branch blocks the APK.
    # But ui_test is skipped on PRs (default-branch only); a skipped dependency would
    # normally skip this job too. This guard keeps it running when ui_test was
    # skipped, while staying blocked if any dependency actually failed or was cancelled.
    if: ${{ !cancelled() && !failure() }}
    steps:
      - name: Checkout the code
        uses: actions/checkout@v5

      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up

      - name: Set current date as env variable
        run: echo "date_today=$(date +'%Y-%m-%d')" >> $GITHUB_ENV

      - name: Set repository name as env variable
        run: echo "repository_name=$(echo '${{ github.repository }}' | awk -F '/' '{print $2}')" >> $GITHUB_ENV

      - name: Build apk - ${{ env.main_project_module }} module
        run: ./gradlew assemble__VARIANT__

      # PRs still assemble above (the real gate); only the default branch keeps a
      # downloadable APK, so PRs don't generate throwaway artifacts.
      - name: Upload APK - ${{ env.repository_name }}
        if: github.ref == 'refs/heads/__BRANCH__'
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - ${{ env.repository_name }} - APK(s) __variant__ generated
          path: ${{ env.main_project_module }}/build/outputs/apk/__variant__/
          retention-days: 14
"""

ACTION = """\
name: Job set up
description: Sets up Java and Gradle

runs:
  using: "composite"
  steps:

    - name: Set up JDK __JDK__
      uses: actions/setup-java@v4
      with:
        java-version: '__JDK__'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      shell: bash   # required: every run step in a composite action must set a shell
      run: chmod +x gradlew

    # Configures Gradle and caches ~/.gradle + the configuration cache automatically
    # (read-only on non-default branches). No separate actions/cache step is needed -
    # that would duplicate and thrash this cache.
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 'wrapper'
        validate-wrappers: true
        gradle-home-cache-cleanup: true
"""


def detect_default_branch(repo: Path) -> str:
    """Best-effort detection of the repo's default branch; falls back to 'main'."""
    try:
        out = subprocess.run(
            ["git", "symbolic-ref", "--short", "refs/remotes/origin/HEAD"],
            cwd=repo, capture_output=True, text=True, timeout=10,
        )
        if out.returncode == 0 and "/" in out.stdout:
            return out.stdout.strip().split("/", 1)[1]
    except (OSError, subprocess.SubprocessError):
        pass
    return "main"


def render(args: argparse.Namespace) -> dict[str, str]:
    if args.secret:
        secret_env = (
            "  # A missing secret resolves to blank rather than failing - only rely on\n"
            "  # that if the build allows it.\n"
            f"  {args.secret}: ${{{{ secrets.{args.secret} }}}}\n"
        )
    else:
        secret_env = ""

    workflow = (
        WORKFLOW
        .replace("__BRANCH__", args.default_branch)
        .replace("__APP_MODULE__", args.app_module)
        .replace("__VARIANT__", args.variant)
        .replace("__variant__", args.variant.lower())
        .replace("__API_LEVEL__", str(args.api_level))
        .replace("__SECRET_ENV__", secret_env)
    )
    action = ACTION.replace("__JDK__", str(args.jdk))
    return {
        ".github/workflows/ci.yml": workflow,
        ".github/actions/job-set-up/action.yml": action,
    }


def main() -> int:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--output", default=".", help="Target repository root (default: current dir)")
    p.add_argument("--app-module", default="app", help="Application module that produces the APK (default: app)")
    p.add_argument("--default-branch", default=None, help="Branch to gate on (default: auto-detect, else 'main')")
    p.add_argument("--variant", default="Debug", help="Build variant, capitalized for Gradle tasks (default: Debug)")
    p.add_argument("--jdk", default="17", help="JDK version for setup-java (default: 17)")
    p.add_argument("--api-level", type=int, default=34, help="Emulator API level for UI tests; must be >= minSdk (default: 34)")
    p.add_argument("--secret", default=None, help="Name of a build-time secret to wire into env: (omit if none)")
    p.add_argument("--force", action="store_true", help="Overwrite files that already exist")
    p.add_argument("--dry-run", action="store_true", help="Print the files that would be written, then exit")
    args = p.parse_args()

    repo = Path(args.output).resolve()
    if args.default_branch is None:
        args.default_branch = detect_default_branch(repo)
        print(f"default branch: {args.default_branch} (auto-detected)")

    files = render(args)

    if args.dry_run:
        for rel in files:
            print(f"would write  {rel}")
        return 0

    wrote, skipped = [], []
    for rel, content in files.items():
        dest = repo / rel
        if dest.exists() and not args.force:
            skipped.append(rel)
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(content, encoding="utf-8", newline="\n")
        wrote.append(rel)

    for rel in wrote:
        print(f"wrote   {rel}")
    for rel in skipped:
        print(f"skip    {rel} (exists; pass --force to overwrite)")

    print(
        "\nNext: review the two files, then confirm the per-project values are right "
        "(app module, default branch, variant, JDK, emulator API level, secret names). "
        "See references/details.md for the regeneration checklist."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
