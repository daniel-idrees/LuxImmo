---
name: single-module-structure
description: The single-module approach to the same Clean Architecture — one Gradle module organized by packages (domain/data/ui/feature) with layer boundaries kept by convention (and optional lint/Konsist rules) instead of the build system. Reference when structuring a small app, prototype, or sample without multi-module overhead.
version: "1.0"
---

# Single-Module Structure

This skill describes the **single-module** approach: the same layered, clean separation, but organized as packages inside one Gradle module instead of many modules. It is lighter to set up and a good fit for small apps, prototypes, and samples. The trade-off is that the boundaries between layers are kept by convention (and optional lint rules) rather than enforced by the build system.

## Technical details

The package layout, the dependency rules, and the trade-offs live in [`references/details.md`](references/details.md).
