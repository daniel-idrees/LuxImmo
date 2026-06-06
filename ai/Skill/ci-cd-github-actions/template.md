# GitHub Actions CI — Template

Copy-paste skeleton. Replace the placeholders:

- `<app-module>` — the application module that produces the APK (e.g. `app`).
- `<default-branch>` — the branch to gate on. **Determine it first:** detect the repo's default (`git symbolic-ref --short refs/remotes/origin/HEAD` → `origin/main` or `origin/master`), or ask the user whether it's `main` or `master`. Put that one name in all **four** places that hardcode it (the two trigger `branches:` lists, the `ui_test` `if:`, and the APK-upload `if:`); the conditions are identical regardless of the name.
- `<Variant>` — the build variant, capitalized for Gradle tasks (e.g. `Debug`); `<variant>` is its lowercase form for paths (e.g. `debug`).
- `<JDK_VERSION>` — the project's Java target (e.g. `17`).
- `<api-level>` — emulator API level for the UI-test job; must be `>=` the app's `minSdk` (e.g. `34`).
- `<SECRET_NAME>` — any project-specific secret, or delete the `env:`/secret lines if none.

> **Branch scope.** The `ui_test` (emulator) job and the APK artifact from `build_and_upload` are scoped to the **default branch only** (`if: github.ref == 'refs/heads/<default-branch>'` and the APK upload's `if`). Pull requests still run lint + unit tests and still compile/assemble as the gate, but they do **not** run UI tests or publish an APK. To run UI tests on every PR too, delete the `if:` on `ui_test` (and then drop `ui_test` from `build_and_upload`'s `needs`, or keep the guard below).

Place the files at `.github/workflows/ci.yml` and `.github/actions/job-set-up/action.yml`.

## `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches:
      - <default-branch>

  pull_request:
    types: [ opened, reopened, synchronize ]
    branches:
      - <default-branch>

# Least-privilege token: this workflow only reads the repo and uploads artifacts.
permissions:
  contents: read

concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}

env:
  main_project_module: <app-module>
  # Delete this line if the project has no build-time secret. A missing secret
  # resolves to blank rather than failing — only rely on that if the build allows it.
  <SECRET_NAME>: ${{ secrets.<SECRET_NAME> }}

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
        run: ./gradlew lint<Variant>

      - name: Upload Lint Report
        if: always()  # upload even when lint fails — that's when it's most useful
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
        run: ./gradlew test<Variant>UnitTest

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
    if: github.ref == 'refs/heads/<default-branch>'
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
      # <api-level> must be >= the app's minSdk; pick a stable, widely cached image.
      - name: Run Instrumented (UI) Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: <api-level>
          arch: x86_64
          target: google_apis
          disable-animations: true
          script: ./gradlew connected<Variant>AndroidTest

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
        run: ./gradlew assemble<Variant>

      # PRs still assemble above (the real gate); only the default branch keeps a
      # downloadable APK, so PRs don't generate throwaway artifacts.
      - name: Upload APK - ${{ env.repository_name }}
        if: github.ref == 'refs/heads/<default-branch>'
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - ${{ env.repository_name }} - APK(s) <variant> generated
          path: ${{ env.main_project_module }}/build/outputs/apk/<variant>/
          retention-days: 14
```

## `.github/actions/job-set-up/action.yml`

```yaml
name: Job set up
description: Sets up Java and Gradle

runs:
  using: "composite"
  steps:

    - name: Set up JDK <JDK_VERSION>
      uses: actions/setup-java@v4
      with:
        java-version: '<JDK_VERSION>'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      shell: bash   # required: every run step in a composite action must set a shell
      run: chmod +x gradlew

    # Configures Gradle and caches ~/.gradle + the configuration cache automatically
    # (read-only on non-default branches). No separate actions/cache step is needed —
    # that would duplicate and thrash this cache.
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 'wrapper'
        validate-wrappers: true
        gradle-home-cache-cleanup: true
```
