---
name: multi-module-structure
description: The multi-module-by-layer-and-feature approach — many small Gradle modules (:core:*, :feature:*) with build-enforced dependency boundaries (ui → domain ← data; features never depend on each other or on data/network/database). Reference when adding a module, wiring dependencies, or checking the allowed dependency direction.
version: "1.0"
---

# Multi-Module Structure

This skill describes the **multi-module** approach: the app is split into many small Gradle modules — by layer (`:core:*`) and by feature (`:feature:*`) — with the build system itself enforcing which module may depend on which. The boundaries are strong and compiler-checked, and each part can be built, tested, and reasoned about on its own. This is the default for apps expected to grow.

## Technical details

The full module breakdown and the exact dependency rules live in [`references/details.md`](references/details.md).
