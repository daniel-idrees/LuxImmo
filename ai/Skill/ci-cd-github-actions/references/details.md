# GitHub Actions CI — Anatomy & Regeneration

> Use when creating or editing `.github/workflows/ci.yml` or the `.github/actions/job-set-up` composite action, or when porting this CI to another Gradle / Android repository.

## Files involved

```
.github/
├── workflows/
│   └── ci.yml                     # the pipeline: triggers + the three jobs
└── actions/
    └── job-set-up/
        └── action.yml             # composite action: JDK + Gradle, reused by every job
```

The split is deliberate: the **workflow** says *what* runs and *when*; the **composite action** holds the *how to prepare a runner* so it is written once and every job calls it with one line (`uses: ./.github/actions/job-set-up`). When the setup changes (a new JDK, a different cache), you edit one file.

## The workflow (`ci.yml`)

### Triggers (`on`)

- `push` to the **default branch** only — verifies what actually landed.
- `pull_request` with `types: [opened, reopened, synchronize]` targeting the default branch — `synchronize` re-runs on every new commit pushed to an open PR. These two cover "before merge" (PR) and "after merge" (push) without running on every branch push.

### Permissions (least privilege)

```yaml
permissions:
  contents: read
```

Set the token to the **minimum** the workflow needs. This pipeline only reads the repo and uploads artifacts, so `contents: read` is enough. Declaring it explicitly at the top drops every other permission the default token would otherwise carry. Add a scope only when a job needs it (e.g. `packages: write` to publish, `pull-requests: write` to comment).

### Concurrency

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```

Groups runs per-PR (or per-ref for pushes). `cancel-in-progress` is **true only for pull requests**: pushing a new commit to a PR cancels the now-outdated run to save minutes, but pushes to the default branch are *not* cancelled, so every merged commit gets a complete, non-aborted result.

### Env and secrets

```yaml
env:
  main_project_module: app          # the app module name — reused in build/artifact steps
  BASE_URL: ${{ secrets.BASE_URL }}  # project-specific; optional, resolves blank if unset
```

- `main_project_module` is the one place the app module name is written; build and upload steps reference `${{ env.main_project_module }}`.
- Secrets are read from `secrets.*` and exposed as env vars. `BASE_URL` here is **project-specific** (baked into a generated `BuildConfig` at configuration time, so every compiling job needs it). A missing secret resolves to an empty string rather than failing — only depend on that if the build tolerates a blank value. **When regenerating, drop or rename secrets that don't apply to the target project.**

### Jobs

All three jobs run on `ubuntu-latest`, set a `timeout-minutes`, check out the code, and call the composite action before doing their real work.

| Job | `needs` | Gradle task | Artifact | Notes |
|---|---|---|---|---|
| `linting` | — | `./gradlew lintDebug` | `lint-report` (HTML) | Every push + PR. Uploads with `if: always()` so the report survives a lint failure — when it's most useful. |
| `unit_test` | — | `./gradlew testDebugUnitTest` | `unit-test-report` | Every push + PR. Also `if: always()`. Runs in parallel with `linting`. |
| `ui_test` | `[linting, unit_test]` | `./gradlew connectedDebugAndroidTest` | `instrumented-test-report` | **Default branch only** (`if: github.ref == 'refs/heads/<default-branch>'`). Boots an emulator (`reactivecircus/android-emulator-runner`) after enabling KVM. Slower + flakier than the JVM checks, so PRs are gated on lint + unit tests instead. |
| `build_and_upload` | `[linting, unit_test, ui_test]` | `./gradlew assembleDebug` | APK, **only on the default branch** | The PR still assembles (the real gate); only the default branch keeps a downloadable APK so PRs don't litter throwaway artifacts. Carries a guard (below) so the skipped `ui_test` on PRs doesn't cascade-skip it. |

> **Branch scope.** Lint + unit tests run on every push and PR. The `ui_test` job and the **APK artifact** are **default-branch (main) only** — a PR still compiles/assembles as the gate but runs neither UI tests nor an APK upload. Run them on every PR branch only when explicitly asked: delete the `if:` on `ui_test` (and reconsider the `build_and_upload` guard — see below).

Key patterns:

- **Fan-out then gate:** `linting` and `unit_test` have no `needs`, so they run in parallel; both `ui_test` and `build_and_upload` declare them in `needs` and only start once they pass.
- **`ui_test` is main-only** via `if: github.ref == 'refs/heads/<default-branch>'`; on a PR it is **skipped**.
- **`build_and_upload` depends on `ui_test`** (`needs: [linting, unit_test, ui_test]`) so a failing UI test on the default branch blocks the APK. Because a *skipped* dependency would normally cascade and skip the dependent job, `build_and_upload` adds **`if: ${{ !cancelled() && !failure() }}`** — it still runs when `ui_test` was skipped (PRs), but stays blocked if any dependency actually failed or the run was cancelled. Without this guard, PRs would silently skip the build gate.
- **`if: always()` on report uploads** so a failing check still publishes its report; **`if: github.ref == 'refs/heads/<default-branch>'`** on the APK upload so the assemble runs everywhere but the artifact is only retained for the branch you ship from.
- **`retention-days`** is set on every upload to bound storage.
- Small shell steps derive run metadata (`date_today`, `repository_name`) into `$GITHUB_ENV` for use in the artifact name.

### The `ui_test` job (instrumented / on-device)

Runs the `androidTest` suite (Compose UI / Espresso tests) on an emulator. Two requirements make it work on CI:

- **Enable KVM** — a small `udev` step grants access to `/dev/kvm` so the emulator gets hardware acceleration; without it the emulator is unusably slow.
- **`reactivecircus/android-emulator-runner@v2`** — boots an AVD, waits for boot, then runs the `script:` (`./gradlew connected<Variant>AndroidTest`). `api-level` must be `>=` the app's `minSdk`; pick a stable, widely cached system image (`target: google_apis`, `arch: x86_64`) and raise it toward `targetSdk` over time. `disable-animations: true` cuts flakiness. Give the job a generous `timeout-minutes` (the emulator boot is slow).

To run UI tests on **every PR** as well, remove the `if:` on `ui_test`. If you do, decide what `build_and_upload` should depend on: keep the guard and `ui_test` in `needs` to also block the PR build on UI failures, or drop `ui_test` from `needs` to keep the build independent of it.

## The composite action (`job-set-up/action.yml`)

```yaml
name: Job set up
description: Sets up Java and Gradle
runs:
  using: "composite"
  steps:
    - uses: actions/setup-java@v4          # JDK 17, temurin
    - run: chmod +x gradlew                 # shell: bash — required in composite steps
    - uses: gradle/actions/setup-gradle@v4  # wrapper + caching, validate-wrappers: true
```

What to keep in mind:

- **`using: "composite"`** is what makes this a reusable local action. Every `run` step inside a composite action **must** declare `shell:` (here `shell: bash`) — that's not optional as it is in a workflow job.
- `gradle/actions/setup-gradle@v4` handles caching of `~/.gradle` and the Gradle configuration cache automatically (read-only on non-default branches). **Do not add a separate `actions/cache` step** — it would duplicate and thrash this cache.
- `validate-wrappers: true` checks the `gradlew` jar against known-good checksums — a supply-chain guard; keep it.
- The JDK version must match the project's `sourceCompatibility` / `jvmTarget`.

## Regeneration checklist — values to change per project

When copying this CI into another repository, change only these:

1. **App module** — set `env.main_project_module` to the target's application module (e.g. `app`). In a **single-module** project this is the one module; in **multi-module** it's the module that produces the APK.
2. **Default branch** — first determine the target's default branch: detect it (`git symbolic-ref --short refs/remotes/origin/HEAD` → `origin/main` or `origin/master`), or ask the user whether it's `main` or `master`. Then put that name in the **four** places that hardcode it: the `push` trigger `branches:`, the `pull_request` trigger `branches:`, the `ui_test` `if:`, and the APK-upload `if:` (`if: github.ref == 'refs/heads/<default-branch>'`). The concurrency block uses `github.ref` dynamically, so it needs **no** change. The triggers and conditions are identical regardless of the name — only the branch string changes. (To avoid hardcoding entirely, the two `if:` guards can instead compare against the repo's default at runtime — `if: github.ref == format('refs/heads/{0}', github.event.repository.default_branch)` — which then works for `main` or `master` with no edit; the trigger `branches:` lists still need literal names, where `[ main, master ]` safely covers both.)
3. **Build variant** — if the app is not built as `Debug`, swap the Gradle tasks accordingly (`lint<Variant>`, `test<Variant>UnitTest`, `connected<Variant>AndroidTest`, `assemble<Variant>`) and the APK output path (`<module>/build/outputs/apk/<variant>/`).
4. **JDK version** — match the target project's Java/Kotlin target in `setup-java`.
5. **Emulator API level** — set `ui_test`'s `api-level` to a stable image that is `>=` the app's `minSdk`. Omit the whole `ui_test` job if the project has no instrumented tests.
6. **Branch scope** — by default `ui_test` and the APK artifact are default-branch only. If the user wants UI tests on every PR, remove the `ui_test` `if:` (and reconsider `build_and_upload`'s `needs`/guard, per the UI-test section above).
7. **Secrets** — remove `BASE_URL` (and any other project-specific secret) or rename it; add only the secrets the target build actually needs, and tell the user to add them under **Settings → Secrets and variables → Actions**.
8. **Artifact paths** — the report globs use `**/build/...`, which already work for both single- and multi-module layouts; leave them unless the project relocates its build dir.

Everything else (permissions, concurrency, job graph, the composite action) transfers unchanged.

## Single-module vs multi-module

The pipeline is the **same** for both. The report-upload globs (`${{ github.workspace }}/**/build/reports/...`) match nested module build directories *and* a single root build dir, so no change is needed. The only module-aware value is `env.main_project_module`, used to locate the APK output (`<module>/build/outputs/apk/<variant>/`).

## Action version pinning

These files pin actions by **major tag** (`@v4`, `@v5`) — easy to read and auto-patched. For a stricter supply-chain posture, pin to a **full commit SHA** instead (`uses: actions/checkout@<40-char-sha>`) and let Dependabot bump them. Pick one policy and apply it consistently; don't mix `@main`/floating refs into a workflow that otherwise pins.

## Validating a regenerated workflow

1. **Syntax** — the file must be valid YAML and use real action inputs. `actionlint` (`actionlint .github/workflows/ci.yml`) catches most mistakes locally; many editors lint workflow files inline.
2. **Run it** — open a draft PR, or push to a branch with a temporary trigger, and confirm all three jobs go green. Reading the live run is the only real proof.
3. **Check the gates** — verify lint/test reports upload even on failure (`always()`), and that the APK artifact appears for a default-branch run but **not** for a PR run.
4. **Confirm caching** — the second run of a job should show a Gradle cache hit in `setup-gradle`'s log.
