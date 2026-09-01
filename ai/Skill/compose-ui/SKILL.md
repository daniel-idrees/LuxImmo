---
name: compose-ui
description: Jetpack Compose UI conventions — stateful vs stateless composables, collecting state with lifecycle, one-time effects, design-system tokens, image loading (Coil 3), previews, and test tags. Reference when writing screens, composables, previews, or UI test tags.
version: "1.0"
---

# Building Screens

This skill covers the house rules for building the visual parts of the app — the screens and the elements on them. The guiding idea is that a screen simply shows whatever information it is handed and reports what the user taps, without making decisions itself. That keeps screens simple to preview while designing and easy to test.

## Technical details

The specific conventions for writing screens, previews, and styling live in [`references/details.md`](references/details.md), and a copy-paste screen skeleton is in [`template.md`](template.md). A generic worked example (an "Articles" screen) is in [`examples/`](examples/).

## Fast scaffolding (run the script for a new screen)

To add **one new screen** to an existing feature, run [`scaffold_screen.py`](scaffold_screen.py) rather than hand-typing the stateful/stateless pair — it emits a `<Screen>Screen.kt` with the full guardrail set (stateful entry point collecting state + effects, a `@VisibleForTesting` stateless body, a `LazyColumn` keyed by a stable id, a test-tags object, and `@Preview`s per state). It encodes [`template.md`](template.md) + [`references/details.md`](references/details.md), which remain the source of truth.

```bash
python scaffold_screen.py --screen Properties --thing Property \
    --package com.example.listings --feature-root feature/listings
# Add --with-contract to also emit a minimal State/Action/Effect + UI-model stub.
```

The screen references a `<Screen>UiState`/`<Screen>UiAction` contract and a `<Thing>Ui` model; for a brand-new feature, generate the whole slice (contract, ViewModel, data layers, and this screen) in one shot with the **architecture-layers** skill's `scaffold_feature.py` instead. Fill in the `TODO` row composable, spacing tokens, and effect handlers afterward. Existing files are never overwritten without `--force`.
