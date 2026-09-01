---
name: unit-testing-with-fakes
description: JVM unit testing for Android using fakes (the preferred doubling style) — shared test infrastructure in :core:testing (Test* fakes, MainDispatcherRule, test data), runTest, Turbine state/effect assertions, and the ViewModel/use-case/mapper/repository test matrix. Reference when writing unit tests with fakes or building shared test infrastructure.
version: "1.0"
---

# Quick Logic Tests (with Fakes)

This skill is about checking the app's logic quickly and reliably on an ordinary computer — no phone required — using simple, hand-written stand-in versions of the app's parts, called "fakes," that the test fully controls. Fakes are the preferred way to do this kind of testing here.

## Technical details

The shared test setup and patterns live in [`references/details.md`](references/details.md), and a copy-paste test skeleton is in [`template.md`](template.md). A generic worked example is in [`examples/`](examples/).
