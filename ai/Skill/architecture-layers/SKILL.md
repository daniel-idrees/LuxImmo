---
name: architecture-layers
description: The domain, data, network, and database layers in detail — pure-Kotlin domain models and use cases, an offline-first repository pattern, DTOs, DAOs, mappers, and a typed Result — plus the MVI ViewModel pattern (immutable State, sealed Action, one-time Effect, and a reusable MviViewModel base) and the start-to-finish checklist for adding a brand-new feature end to end (module setup, domain/data wiring, MVI + Compose UI, navigation, UI models, and tests). Reference when writing domain models, use cases, repositories, DTOs, DAOs, or mappers, when building a ViewModel and its State/Action/Effect contract, or when adding a new feature.
version: "1.3"
---

# Architecture Layers

This skill describes how the app is organized into layers — the screens people see, the core rules in the middle, and the parts that fetch and store information — and how those layers are kept separate so a change in one doesn't disturb the others. It also covers the "offline-first" idea: the app trusts its own saved information first and only reaches out to the internet to refresh it, so it keeps working without a connection. Finally, it carries the **start-to-finish checklist for adding a brand-new feature**: following the same recipe every time means each feature is built consistently and nothing important is forgotten or left half-done.

> **Compose UI is assumed.** The UI layer is Jetpack Compose throughout, driven by the MVI ViewModel loop documented here. This skill covers the domain, data, network, and database layers, **the MVI pattern** (a ViewModel's State / Action / Effect contract), and how a feature is assembled across them. The composables that render the state are pattern-agnostic — they work equally with MVVM or MVI — and live in the `compose-ui` skill.

## Technical details

The detailed responsibilities of each layer, the **MVI ViewModel pattern**, and the end-to-end "adding a feature" checklist live in [`references/details.md`](references/details.md), and the copy-paste code skeletons (`Result`, use case, offline-first repository, plus the MVI contracts + `MviViewModel` base + a screen's State/Action/Effect + ViewModel) are in [`template.md`](template.md). Generic worked examples (an "Articles" data + domain slice, its MVI screen contract, plus the same feature added end to end in multi- and single-module form) are in [`examples/`](examples/).
