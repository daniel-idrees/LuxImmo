---
name: architecture-layers
description: The domain, data, network, and database layers in detail — pure-Kotlin domain models and use cases, an offline-first repository pattern (or a network-only repository when offline-first is disabled), DTOs and Retrofit/OkHttp/kotlinx.serialization networking, DAOs and Room (KSP) persistence, mappers, and a typed Result — plus the MVI ViewModel pattern (immutable State, sealed Action, one-time Effect, and a reusable MviViewModel base) and the start-to-finish checklist for adding a brand-new feature end to end (module setup, domain/data wiring, MVI + Compose UI, navigation, UI models, and tests). Reference when writing domain models, use cases, repositories, DTOs, DAOs, or mappers, when building a ViewModel and its State/Action/Effect contract, or when adding a new feature. Also carries the cross-cutting writing conventions.
version: "1.4"
---

# Architecture Layers

This skill describes how the app is organized into layers — the screens people see, the core rules in the middle, and the parts that fetch and store information — and how those layers are kept separate so a change in one doesn't disturb the others. This follows the principles of **Clean Architecture**: the layers depend only inward, toward a pure-Kotlin domain core that knows nothing about Android, the network, or the database, so the business rules stay independent of any framework or delivery mechanism. It also covers the "offline-first" idea: the app trusts its own saved information first and only reaches out to the internet to refresh it, so it keeps working without a connection. Finally, it carries the **start-to-finish checklist for adding a brand-new feature**: following the same recipe every time means each feature is built consistently and nothing important is forgotten or left half-done.

> **Offline-first is a toggle.** Whether the app persists data locally (Room + offline-first repository) or fetches straight from the network with no local storage is a project-wide decision made once. When the app is **not** offline-first, skip the database layer and the offline-first repository and use the simpler **network-only variant** documented in [`references/layers.md`](references/layers.md) (*Network-only (no offline-first) variant*). Everything else in this skill is unchanged.

> **Compose UI is assumed.** The UI layer is Jetpack Compose throughout, driven by the MVI ViewModel loop documented here. This skill covers the domain, data, network, and database layers, **the MVI pattern** (a ViewModel's State / Action / Effect contract), and how a feature is assembled across them. The composables that render the state are pattern-agnostic — they work equally with MVVM or MVI — and follow the usual Compose conventions (a stateful entry point that collects state plus a stateless content composable); writing them is outside this skill's scope.

## Technical details

The detailed responsibilities of each layer live in [`references/layers.md`](references/layers.md); the **MVI ViewModel pattern** (a ViewModel's State / Action / Effect contract) is in [`references/mvi.md`](references/mvi.md); and the end-to-end "adding a feature" checklist is in [`references/adding-a-feature.md`](references/adding-a-feature.md). How the app is split into Gradle modules (multi-module) or organized as packages in one module (single-module) — the full module/package breakdown and dependency rules for whichever structure the project uses — is in [`references/modularization.md`](references/modularization.md). The copy-paste code skeletons (`Result`, use case, the network DTO + data source + Retrofit API/client, offline-first repository, plus the MVI contracts + `MviViewModel` base + a screen's State/Action/Effect + ViewModel) are in [`template.md`](template.md). Generic worked examples (an "Articles" data + domain slice, its MVI screen contract, plus the same feature added end to end in multi- and single-module form) are in [`examples/`](examples/).

## Fast scaffolding (run the script, don't hand-type the slice)

When **adding a new feature**, run [`scaffold_feature.py`](scaffold_feature.py) first instead of writing the ~15 boilerplate files by hand — it stamps out the whole vertical slice (domain model/repository/use case, network DTO/data source/Retrofit API+client/DI module, Room entity+DAO, data mappers/repository impl/`@Binds` module, MVI contract/ViewModel/UI model, the Compose screen, and the feature `build.gradle.kts`), then you fill in the real fields and logic. It honours both project toggles — `--layout multi|single` and `--mode offline-first|network-only` — and is a faithful encoding of the references and [`template.md`](template.md), which remain the source of truth.

```bash
# Preview the file plan, then generate (offline-first, multi-module shown):
python scaffold_feature.py --name listings --thing Property --package com.example --dry-run
python scaffold_feature.py --name listings --thing Property --screen Properties --package com.example --id-type String
# Network-only, single-module variant:
python scaffold_feature.py --name search --thing Listing --layout single --mode network-only --app-module app
```

Generated files carry `TODO` markers and a `// --- project-internal types` import block to fix to the project's actual core packages (`Result`/`AppError`, `runSuspendCatching`, `MviViewModel`, `ResourceProvider`, etc.). Pass `--force` to overwrite, `--help` for all flags. Existing files are never overwritten without `--force`. Use this for a brand-new feature; for a single extra screen in an existing feature, the `compose-ui` skill's `scaffold_screen.py` is lighter.
