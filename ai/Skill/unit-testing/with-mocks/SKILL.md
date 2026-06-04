---
name: unit-testing-with-mocks
description: JVM unit testing for Android using Mockito mocks — mock()/whenever/verify, argument captors, and interaction verification, layered on the same runTest + MainDispatcherRule + Turbine mechanics. Reference when a test needs to verify interactions or stub a collaborator where a fake is overkill.
version: "1.0"
---

# Quick Logic Tests (with Mocks)

This skill is about the same kind of quick, no-phone logic testing, but using a tool that generates automatic stand-ins called "mocks." Mocks are mainly useful when a test needs to confirm that one part actually called another. Fakes are the preferred default; mocks are reached for deliberately.

## Technical details

When to choose mocks (and the notes) live in [`references/details.md`](references/details.md), and a copy-paste test skeleton is in [`template.md`](template.md). A generic worked example is in [`examples/`](examples/).
