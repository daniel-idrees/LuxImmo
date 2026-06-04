---
name: architecture-layers
description: The domain, data, network, and database layers in detail — pure-Kotlin domain models and use cases, an offline-first repository pattern, DTOs, DAOs, mappers, and a typed Result. Reference when writing domain models, use cases, repositories, DTOs, DAOs, or mappers.
version: "1.0"
---

# Architecture Layers

This skill describes how the app is organized into layers — the screens people see, the core rules in the middle, and the parts that fetch and store information — and how those layers are kept separate so a change in one doesn't disturb the others. It also covers the "offline-first" idea: the app trusts its own saved information first and only reaches out to the internet to refresh it, so it keeps working without a connection.

## Technical details

The detailed responsibilities of each layer live in [`references/details.md`](references/details.md), and the copy-paste code skeletons (`Result`, use case, offline-first repository) are in [`template.md`](template.md). A generic worked example (an "Articles" slice) is in [`examples/`](examples/).
