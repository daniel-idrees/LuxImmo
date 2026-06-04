
# Core Principles & Tech Stack

> Use when choosing libraries, pinning versions, or orienting on the overall philosophy.

## Core Principles

In short, the guiding principles are: Clean Architecture (`ui → domain ← data`), multi-module by layer & feature, offline-first, MVI one-way data flow, a passive Compose UI, everything injected via Hilt, convention over repetition, and test-first seams. The rest of this skill covers the **stack** those principles are built on.

## Tech Stack

| Concern | Choice |
|---|---|
| Language | Kotlin (latest stable, 2.x+) |
| UI | Jetpack Compose (BOM-managed) + Material 3 |
| Async | Coroutines & Flow |
| DI | Hilt (Dagger) |
| Networking | Retrofit + OkHttp + `kotlinx.serialization` |
| Persistence | Room (KSP) |
| Image loading | Coil 3 |
| Navigation | Jetpack Navigation 3 (type-safe `NavKey`s) |
| Build | Gradle Kotlin DSL + Version Catalog + custom convention plugins |
| Unit testing | JUnit4, `kotlinx-coroutines-test`, Turbine, Mockito (+ fakes) |
| UI testing | Compose UI Test (`createAndroidComposeRule`, test tags) |
| Performance | Macrobenchmark + Baseline Profiles |

> Pin every version in the version catalog. Use a Compose **BOM** so Compose artifacts stay aligned. Use **KSP** (not kapt) for Room/Hilt code generation where supported.

## Writing style

These habits keep the codebase consistent no matter who — or what — writes it:

- **Comments explain *why*, not *what*** — non-obvious flow behavior, ordering, or sync rules. Match the surrounding file's comment density.

> **Guardrail:** mirror the nearest existing example — an existing feature, repository, or DI module — for structure and naming before inventing a new shape. A new pattern should be a deliberate, documented decision, not an accident; consistency is what lets anyone (or any AI agent) extend the code without surprises.

## Single-module variant

"Multi-module by layer & feature" is the default, but the same principles work in a **single module** organized by packages (`domain/`, `data/`, `ui/`, `feature/`) — lighter to set up, with layer boundaries kept by convention instead of the build system. In that case the **convention-plugins** part of the build stack doesn't apply, but everything else (Compose, Hilt, Room, Retrofit, Navigation 3, the version catalog, the testing stack) is unchanged.
