---
name: compose-ui
description: Jetpack Compose UI conventions — stateful vs stateless composables, collecting state with lifecycle, one-time effects, design-system tokens, previews, and test tags. Reference when writing screens, composables, previews, or UI test tags.
version: "1.0"
---

# Building Screens

This skill covers the house rules for building the visual parts of the app — the screens and the elements on them. The guiding idea is that a screen simply shows whatever information it is handed and reports what the user taps, without making decisions itself. That keeps screens simple to preview while designing and easy to test.

## Technical details

The specific conventions for writing screens, previews, and styling live in [`references/details.md`](references/details.md), and a copy-paste screen skeleton is in [`template.md`](template.md). A generic worked example (an "Articles" screen) is in [`examples/`](examples/).
