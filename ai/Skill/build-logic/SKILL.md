---
name: build-logic
description: Android Gradle convention plugins and version-catalog conventions that keep module build files tiny and shared configuration in one place. Reference when writing convention plugins (multi-module projects) or editing build.gradle.kts / the version catalog (any Android project).
version: "1.0"
---

# Build Logic

This skill is about keeping the project's build setup organized instead of copied and pasted across every building block. Shared build instructions and the single approved list of tool versions are kept in one central place, which keeps the whole project consistent and easy to update.

## Technical details

The reusable build pieces and version-management rules live in [`references/details.md`](references/details.md); a copy-paste feature `build.gradle.kts` skeleton is in [`template.md`](template.md). Two worked examples cover both sides: the consumer side — a feature module applying the plugins plus a matching version-catalog excerpt — in [`examples/feature-articles-build.md`](examples/feature-articles-build.md), and the authoring side — writing and registering a convention plugin — in [`examples/android-feature-convention-plugin.md`](examples/android-feature-convention-plugin.md).
