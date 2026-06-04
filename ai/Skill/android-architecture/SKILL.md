---
name: android-architecture
description: Overview and guiding principles for a project-agnostic blueprint for building modern Android apps the way this codebase is built — Clean Architecture (ui → domain ← data), multi-module by layer & feature, MVI, offline-first, Hilt, Jetpack Compose + Material 3, Navigation 3. Start here to understand the purpose and the principles before diving into the work.
version: "2.0"
---

# Modern Android App Architecture & Conventions

## What this is

A reusable **blueprint** for building Android apps in a consistent, proven way — think of it as a detailed recipe. It captures *how this team builds apps* so the same structure, tools, and habits can be reproduced in **any new project**, whether the work is done by a developer or by an AI assistant (Claude Code, Cursor, Copilot, …).

It is **project-agnostic**: nothing here is tied to one specific app. You take the blueprint, fill in the project's own details, and follow the patterns.

This file is the **front door** — it explains the purpose and the guiding principles. The detailed guidance is organised into focused, self-contained pieces, so you can **reuse just the parts a project needs** without dragging everything along.

## Why it exists

Software projects tend to drift. Different people — and different AI tools — solve the same problem in different ways, and over time the app becomes inconsistent, hard to test, and hard to change. This blueprint prevents that by writing down the agreed way of doing things, so that:

- **Everything is built the same way** — the structure is predictable no matter who, or what, writes the code.
- **Quality stays high** — the patterns here make the app easier to test, maintain, and extend.
- **Getting started is fast** — a new developer or AI agent can read this and immediately work in the established style.
- **Settled decisions stay settled** — the proven choices are captured once, not re-argued on every change.

## How to use this blueprint

You only read what's relevant to the task in front of you — there's no need to absorb everything at once. This file gives the big picture and the guiding principles.

Each project is also expected to keep a few companion documents:

| Document | What it answers |
|---|---|
| this blueprint | **How** the code should be built — the stable house rules. |
| `ai/Agent.md` | **Why** specific choices were made in *this particular* project. |
| `ai/requirements.md` (optional) | **What** the project is meant to do. |
| `ai/specs/*.md` (optional) | The planned work, broken into ordered steps. |

**If two of these ever disagree, the project's own `Agent.md` wins** — it records the real decisions taken for that app.

## The big picture

In plain terms, the app is built in **layers**:

- The **core rules** of the app live in the middle. They don't care about screens, the internet, or where data is stored.
- The **screens** sit on top and simply show information and report what the user taps.
- The **data access** sits underneath, fetching information from the internet and saving it on the device.

The app is also **offline-first**: it trusts its own saved data first and only reaches out to the internet to refresh it, so it keeps working even without a connection.

## The eleven principles

Each principle is one plain idea.

1. **Use one proven set of tools** — Pick a single, well-known tool for each job and stick with it, instead of mixing many tools that do the same thing.
2. **Build the app in small, independent pieces** — Split the app into small parts with clear boundaries, so each part can be built and understood on its own.
3. **Set the rules once and reuse them** — Define shared setup in one place rather than copying it around, so the project stays consistent and is easy to change later.
4. **Keep the core rules separate from screens and data** — The heart of the app stays independent of the screen, the internet, and the device's storage — and it keeps working offline.
5. **Let information flow in one direction** — Changes follow a single, predictable path (something happens → the screen updates), which makes the app easy to follow and fix.
6. **Screens just show what they're given** — The visual layer only displays information and reports taps; it holds no decision-making logic, which keeps it simple to preview and test.
7. **Let the parts be assembled automatically** — Components are wired together by a tool rather than by hand, making them easy to swap out and test.
8. **Keep moving between screens simple and safe** — Navigation is defined in a structured, mistake-resistant way and kept separate from the rest of the logic.
9. **Write code in a consistent style** — Shared naming and structure mean anyone — or any AI agent — can read and extend the code without surprises.
10. **Design for testing from the start** — The app is built so its logic can be checked quickly and reliably, without needing a real phone.
11. **Add every new feature the same way** — Each new feature follows the same start-to-finish checklist, so nothing is forgotten or left half-finished.
