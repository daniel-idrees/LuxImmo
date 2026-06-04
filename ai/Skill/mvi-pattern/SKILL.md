---
name: mvi-pattern
description: The MVI (Model–View–Intent) pattern for Android ViewModels — immutable State, sealed Action, one-time Effect, and a reusable MviViewModel base. Reference when building a ViewModel and its State/Action/Effect contract.
version: "1.0"
---

# Screen Logic Pattern (MVI)

This skill is about a tidy, predictable way to manage what happens on a screen. Information flows in a single direction: the user does something, the screen's state is updated, and the screen redraws itself from that state. Because there is only one path for changes, screen behavior is easy to follow, reproduce, and fix.

## Technical details

How the pattern works lives in [`references/details.md`](references/details.md), and the copy-paste code skeletons are in [`template.md`](template.md). A generic worked example (an "Articles" screen) is in [`examples/`](examples/).
