---
name: hilt-dependency-injection
description: Hilt Dependency Injection conventions — @Binds vs @Provides modules, components/scopes, qualified coroutine dispatchers, and injected seams like ResourceProvider. Reference when adding Hilt modules, bindings, dispatchers, or scopes.
version: "1.0"
---

# Wiring the App Together

This skill is about letting a tool assemble the app's many small pieces automatically and hand each part whatever it needs, instead of wiring everything together by hand. This makes the parts easy to swap out and easy to test on their own.

## Technical details

The conventions for how pieces are registered and supplied live in [`references/details.md`](references/details.md), and the dispatcher/scope qualifier skeleton is in [`template.md`](template.md). Generic worked examples (Hilt modules) are in [`examples/`](examples/).
