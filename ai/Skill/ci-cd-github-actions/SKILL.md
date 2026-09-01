---
name: ci-cd-github-actions
description: Regenerate this project's GitHub Actions CI for another Gradle / Android project — the `.github/workflows/ci.yml` pipeline (lint + unit-test jobs, an instrumented UI-test job on an emulator, and a build-and-upload job) and the reusable `.github/actions/job-set-up` composite action (JDK + Gradle setup with caching). IMPORTANT: the instrumented UI-test job and the build-and-upload APK artifact run on the default branch (main, or master — whichever the project ships from) ONLY — pull requests still run lint + unit tests and still compile/assemble as the gate, but they do NOT run UI tests or publish an APK — unless you are explicitly asked to also run them on every PR branch. Covers the triggers, least-privilege permissions, concurrency / cancel-in-progress, secrets and env wiring, the android-emulator-runner (KVM) setup, the skipped-dependency guard on build-and-upload, artifact uploads, action version pinning, and the per-project values to change (app module, default branch, build variant, emulator API level, secret names). Reference when creating or updating a CI workflow, a GitHub Actions YAML file, or a composite action for an Android / Gradle repository.
version: "1.0"
---

# GitHub Actions CI

This skill describes the project's continuous-integration setup — the automated checks that run on every change before it is allowed into the main branch — and how to recreate the same setup in **another** project. Continuous integration means the machine builds the app, runs the linter, and runs the tests for you on every push and pull request, so a broken change is caught early instead of after it merges. The setup here is deliberately small: one pipeline file that defines four jobs (check style, run the JVM unit tests, run the on-device UI tests, build the app) and one shared "setup" step that every job reuses so the Java and Gradle preparation is written once instead of copied into each job.

> **Branch scope.** Lint and unit tests run on **every** push and pull request. The **UI-test** job (which boots an emulator) and the **APK artifact** from build-and-upload are scoped to the **default branch (main) only** — a PR still compiles/assembles as the gate but does not run UI tests or publish an APK. This keeps PR feedback fast and cheap; the slower on-device suite runs once a change lands on the default branch. Run them on every PR branch only when explicitly asked.
>
> **Which default branch — `main` or `master`?** This project's is `main`, but a target repo may ship from `master`. Determine it *before* generating: detect the repo's default (e.g. `git symbolic-ref --short refs/remotes/origin/HEAD`, which prints `origin/main` or `origin/master`) and, if that's unavailable or ambiguous, ask the user whether it's `main` or `master`. Then use that one name everywhere. The triggers and the `if:` scoping conditions are **identical** regardless of the name — only the branch string changes.

The point of this skill is portability. The same pipeline shape works for any Gradle-based Android project; moving it to a new repository is a matter of swapping a handful of values — the name of the app's main module, the default branch, the build variant, and any secrets — rather than rewriting the workflow. The skill captures both the pipeline and the reasoning behind each part (why permissions are locked down, why pull requests cancel their own superseded runs, why the APK is only kept for the main branch) so the regenerated copy keeps those properties instead of losing them.

> **Tool-agnostic.** This is a plain-language reference, not tied to any one assistant — any agentic coding tool can follow it to regenerate the workflow. It assumes the target project uses the **Gradle wrapper** (`./gradlew`). If the target is not a Gradle/Android project, only the structure (composite action, job graph, permissions, concurrency, artifact handling) transfers — the actual Gradle commands and the JDK setup do not.

## Technical details

The full anatomy of the workflow and the composite action — triggers, least-privilege permissions, concurrency, the four jobs (including the main-only UI-test job and the skipped-dependency guard on build-and-upload), artifact uploads, action version pinning, and the design decision behind each — lives in [`references/details.md`](references/details.md), together with the **regeneration checklist** of the exact values to change when copying it to another project and how to validate the result. A copy-paste skeleton for both files (`.github/workflows/ci.yml` and `.github/actions/job-set-up/action.yml`) with placeholders is in [`template.md`](template.md). A filled-in worked example — the same pipeline applied to a concrete app, plus the common variations (matrix builds, signed release, instrumented tests) — is in [`examples/android-app-ci.md`](examples/android-app-ci.md).

## Fast generation (run the script, don't hand-write the YAML)

To **regenerate the pipeline for a project**, run [`generate_ci.py`](generate_ci.py) instead of retyping ~200 lines of YAML — it stamps out both files with the per-project placeholders already substituted. It auto-detects the default branch (falling back to `main`), and `details.md` / `template.md` remain the source of truth for the reasoning.

```bash
python generate_ci.py --dry-run                                  # preview the two files
python generate_ci.py --app-module app --variant Debug --jdk 17 --api-level 34 --secret BASE_URL
python generate_ci.py --output ../other-repo --default-branch master   # target another repo
```

Flags: `--app-module`, `--default-branch` (auto-detected if omitted), `--variant`, `--jdk`, `--api-level` (must be ≥ the app's `minSdk`), `--secret` (omit if none), `--output`, `--force`, `--dry-run`. After running, review the result against the regeneration checklist in [`references/details.md`](references/details.md). Existing files are never overwritten without `--force`.
