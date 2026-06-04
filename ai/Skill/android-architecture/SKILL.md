---
name: android-architecture
description: Overview and index for a project-agnostic blueprint for building modern Android apps the way this codebase is built — Clean Architecture (ui → domain ← data), multi-module by layer & feature, MVI, offline-first, Hilt, Jetpack Compose + Material 3, Navigation 3. Start here to understand the purpose, then open the focused sub-skill (module structure, build logic, architecture layers, MVI, Compose UI, dependency injection, navigation, coding standards, testing, or adding a feature) for the task at hand. Each sub-skill carries its own must-not-break guardrails inline.
version: "2.0"
---

# Modern Android App Architecture & Conventions

## What this is

A reusable **blueprint** for building Android apps in a consistent, proven way — think of it as a detailed recipe. It captures *how this team builds apps* so the same structure, tools, and habits can be reproduced in **any new project**, whether the work is done by a developer or by an AI assistant (Claude Code, Cursor, Copilot, …).

It is **project-agnostic**: nothing here is tied to one specific app. You take the blueprint, fill in the project's own details, and follow the patterns.

This is a **collection of focused skills**. This file is the front door — it explains the purpose and lists the sub-skills. Each sub-skill lives in its own folder and stands on its own, so you can **reuse just the ones a project needs** (for example, take only the `dependency-injection` skill into another app) without dragging everything along.

## Why it exists

Software projects tend to drift. Different people — and different AI tools — solve the same problem in different ways, and over time the app becomes inconsistent, hard to test, and hard to change. This blueprint prevents that by writing down the agreed way of doing things, so that:

- **Everything is built the same way** — the structure is predictable no matter who, or what, writes the code.
- **Quality stays high** — the patterns here make the app easier to test, maintain, and extend.
- **Getting started is fast** — a new developer or AI agent can read this and immediately work in the established style.
- **Settled decisions stay settled** — the proven choices are captured once, not re-argued on every change.

## How to use this collection

You only read the sub-skill relevant to the task in front of you — there's no need to absorb everything at once. This file gives the big picture and the guiding principles; each principle points to the sub-skill that explains it in full.

Each project is also expected to keep a few companion documents:

| Document | What it answers |
|---|---|
| `Skill/` (this collection) | **How** the code should be built — the stable house rules. |
| `ai/Agent.md` | **Why** specific choices were made in *this particular* project. |
| `ai/requirements.md` (optional) | **What** the project is meant to do. |
| `ai/specs/*.md` (optional) | The planned work, broken into ordered steps. |

**If two of these ever disagree, the project's own `Agent.md` wins** — it records the real decisions taken for that app.

## The big picture

In plain terms, the app is built in **layers**:

- The **core rules** of the app live in the middle. They don't care about screens, the internet, or where data is stored.
- The **screens** sit on top and simply show information and report what the user taps.
- The **data access** sits underneath, fetching information from the internet and saving it on the device.

The app is also **offline-first**: it trusts its own saved data first and only reaches out to the internet to refresh it, so it keeps working even without a connection. (The detailed, technical version of this picture lives in the `architecture-layers` skill.)

## The eleven principles

Each principle is one plain idea, paired with the sub-skill that explains it in depth.

1. **Use one proven set of tools** *(→ `principles-and-stack`)* — Pick a single, well-known tool for each job and stick with it, instead of mixing many tools that do the same thing.
2. **Build the app in small, independent pieces** *(→ `multi-module-structure` / `single-module-structure`)* — Split the app into small parts with clear boundaries, so each part can be built and understood on its own.
3. **Set the rules once and reuse them** *(→ `build-logic`)* — Define shared setup in one place rather than copying it around, so the project stays consistent and is easy to change later.
4. **Keep the core rules separate from screens and data** *(→ `architecture-layers`)* — The heart of the app stays independent of the screen, the internet, and the device's storage — and it keeps working offline.
5. **Let information flow in one direction** *(→ `mvi-pattern`)* — Changes follow a single, predictable path (something happens → the screen updates), which makes the app easy to follow and fix.
6. **Screens just show what they're given** *(→ `compose-ui`)* — The visual layer only displays information and reports taps; it holds no decision-making logic, which keeps it simple to preview and test.
7. **Let the parts be assembled automatically** *(→ `dependency-injection`)* — Components are wired together by a tool rather than by hand, making them easy to swap out and test.
8. **Keep moving between screens simple and safe** *(→ `navigation`)* — Navigation is defined in a structured, mistake-resistant way and kept separate from the rest of the logic.
9. **Write code in a consistent style** *(→ `principles-and-stack`, plus the conventions inline in each skill)* — Shared naming and structure mean anyone — or any AI agent — can read and extend the code without surprises.
10. **Design for testing from the start** *(→ `unit-testing-with-fakes`, `unit-testing-with-mocks`, `compose-ui-testing`, `performance-testing`)* — The app is built so its logic can be checked quickly and reliably, without needing a real phone.
11. **Add every new feature the same way** *(→ `adding-a-feature`)* — Each new feature follows the same start-to-finish checklist, so nothing is forgotten or left half-finished.

## The sub-skills

Each folder below is a self-contained skill — copy the ones you need into another project. (These are written for developers and AI agents; the technical detail lives in them, not here.)

| Skill folder | Open it when you are… |
|---|---|
| `principles-and-stack` | Choosing libraries, pinning versions, or orienting on the overall philosophy. |
| `module-structure/multi-module` | Splitting into many Gradle modules with build-enforced boundaries (the default). |
| `module-structure/single-module` | Structuring a small app, prototype, or sample as one module with packages. |
| `build-logic` | Writing convention plugins or editing `build.gradle.kts` / the version catalog. |
| `architecture-layers` | Writing domain models, use cases, repositories, DTOs, DAOs, or mappers. |
| `mvi-pattern` | Building a ViewModel and its State / Action / Effect contract. |
| `compose-ui` | Writing screens, composables, previews, or UI test tags. |
| `dependency-injection` | Adding Hilt modules, bindings, dispatchers, or scopes. |
| `navigation` | Adding destinations, nav keys, or feature entry providers (Navigation 3). |
| `unit-testing/with-fakes` | Writing JVM unit tests with fakes (the preferred style) + the shared test infrastructure (`:core:testing`). |
| `unit-testing/with-mocks` | Writing JVM unit tests with Mockito when you need to verify interactions. |
| `compose-ui-testing` | Writing Compose UI tests (`androidTest`) driven by test tags. |
| `performance-testing` | Setting up Macrobenchmark / Baseline Profiles for startup and runtime performance. |
| `adding-a-feature` | Adding a brand-new feature end to end (start-to-finish checklist). |

## Before you ship

There is no separate guardrails skill — each skill carries its own **must-not-break rules** inline as `> **Guardrail:**` callouts, right beside the conventions they protect. When reviewing a change, check it against the guardrails in whichever skills it touches:

- **dependency direction** and **one result type** → `architecture-layers`
- **no `Context` / `Dispatchers` / strings in ViewModels**, reuse the `MviViewModel` base → `mvi-pattern`
- **Hilt only, no ad-hoc singletons** → `dependency-injection`
- **per-screen requirements** (composables, previews, test tags, tests, typed errors) → `compose-ui`
- **convention plugins** and **versions in the catalog** → `build-logic`
- **mirror the nearest example** → `principles-and-stack`
- **a feature never reaches across features or into the data layer** → `adding-a-feature`
