# Example — CI for an Android app

A concrete instance of the template applied to an app whose application module is `app`, default branch is `main`, build variant is `Debug`, JDK is `17`, and which needs **no** build-time secret. This is the baseline; the variations afterwards show the common one-step changes.

## `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches:
      - main
  pull_request:
    types: [ opened, reopened, synchronize ]
    branches:
      - main

permissions:
  contents: read

concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}

env:
  main_project_module: app

jobs:
  linting:
    name: Run Lint Checks
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@v5
      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up
      - name: Run Android Lint
        run: ./gradlew lintDebug
      - name: Upload Lint Report
        if: always()
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
      - uses: actions/checkout@v5
      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-report
          path: ${{ github.workspace }}/**/build/reports/tests/
          retention-days: 14

  build_and_upload:
    name: Build and Upload
    needs: [ linting, unit_test ]
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v5
      - name: Setup Java and Gradle
        uses: ./.github/actions/job-set-up
      - name: Set current date as env variable
        run: echo "date_today=$(date +'%Y-%m-%d')" >> $GITHUB_ENV
      - name: Set repository name as env variable
        run: echo "repository_name=$(echo '${{ github.repository }}' | awk -F '/' '{print $2}')" >> $GITHUB_ENV
      - name: Build apk - ${{ env.main_project_module }} module
        run: ./gradlew assembleDebug
      - name: Upload APK - ${{ env.repository_name }}
        if: github.ref == 'refs/heads/main'
        uses: actions/upload-artifact@v4
        with:
          name: ${{ env.date_today }} - ${{ env.repository_name }} - APK(s) dev debug generated
          path: ${{ env.main_project_module }}/build/outputs/apk/debug/
          retention-days: 14
```

## `.github/actions/job-set-up/action.yml`

```yaml
name: Job set up
description: Sets up Java and Gradle

runs:
  using: "composite"
  steps:
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      shell: bash
      run: chmod +x gradlew
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 'wrapper'
        validate-wrappers: true
        gradle-home-cache-cleanup: true
```

---

## Variations

Each is an independent, drop-in change to the baseline above.

### A build-time secret (e.g. an API base URL)

Add to the top-level `env:` so every compiling job sees it, and tell the user to add it under **Settings → Secrets and variables → Actions**:

```yaml
env:
  main_project_module: app
  BASE_URL: ${{ secrets.BASE_URL }}
```

### A different default branch (e.g. `master`)

First confirm which branch the project ships from — detect the repo default (`git symbolic-ref --short refs/remotes/origin/HEAD`) or ask the user whether it's `main` or `master` — then replace the name in every place that hardcodes it. In **this** baseline (which has no `ui_test` job) that's **three** places — the two trigger `branches:` lists and the APK guard; in the full pipeline that includes the `ui_test` job there is a **fourth**, its `if:` guard. The conditions are otherwise identical — only the branch name changes:

```yaml
      - name: Upload APK - ${{ env.repository_name }}
        if: github.ref == 'refs/heads/master'
```

### A release variant instead of debug

Swap the Gradle tasks and the APK path (this assumes signing is configured in Gradle; otherwise wire signing secrets into the build step):

```yaml
      - run: ./gradlew lintRelease
      # ...
      - run: ./gradlew testReleaseUnitTest
      # ...
      - run: ./gradlew assembleRelease
      # ...
        with:
          path: ${{ env.main_project_module }}/build/outputs/apk/release/
```

### Test across multiple JDKs (matrix)

Turn a job into a matrix and pass the version through to the composite action — which requires adding an `inputs:` block to `action.yml` so the JDK is parameterized:

```yaml
  unit_test:
    name: Unit Test (JDK ${{ matrix.java }})
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [ '17', '21' ]
    steps:
      - uses: actions/checkout@v5
      - uses: ./.github/actions/job-set-up
        with:
          java-version: ${{ matrix.java }}
      - run: ./gradlew testDebugUnitTest
```

```yaml
# action.yml — make the JDK an input with a default
inputs:
  java-version:
    description: JDK major version
    required: false
    default: '17'
runs:
  using: "composite"
  steps:
    - uses: actions/setup-java@v4
      with:
        java-version: ${{ inputs.java-version }}
        distribution: 'temurin'
    # ... rest unchanged
```

### Instrumented (on-device) tests

These need an emulator and are slower/flakier, so keep them in a **separate** job that `needs: [unit_test]`. A common approach uses `reactivecircus/android-emulator-runner` to run `connectedDebugAndroidTest` on a KVM-enabled runner. Add it as an extra job rather than folding it into `unit_test`.
