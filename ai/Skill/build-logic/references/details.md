# Build Logic & Dependency Management

> Use when writing convention plugins or editing `build.gradle.kts` / the version catalog.

## Convention Plugins

All shared Gradle configuration lives in an included build at `build-logic/convention`. Modules apply tiny, named plugins instead of repeating config.

**Plugins to provide** (ids prefixed with `<conv>`):

| Plugin id | Responsibility |
|---|---|
| `<conv>.android.application` | Applies app + Kotlin + compose config, SDK versions. |
| `<conv>.android.library` | Applies library + Kotlin, shared `compileSdk`/`minSdk`/Java + Kotlin target. |
| `<conv>.android.compose` | Enables Compose, adds BOM + standard Compose deps (debug tooling, test manifest). |
| `<conv>.android.hilt` | Applies Hilt + KSP, adds Hilt deps. |
| `<conv>.android.room` | Applies Room plugin + KSP, schema config. |
| `<conv>.android.feature` | Composes `library` + `hilt`, wires the common feature dependencies (`:core:domain`, `:core:ui`, `:core:common`, lifecycle, hilt-navigation-compose). |

**Patterns:**

- Centralize `compileSdk`, `minSdk`, `sourceCompatibility`/`targetCompatibility`, and Kotlin `jvmTarget` in **one** helper (`Project.configureKotlinAndroid`). Set them once.
- Use **KSP** (not kapt) for Room/Hilt code generation wherever supported — the `<conv>.android.room` / `<conv>.android.hilt` plugins apply it.
- Access the version catalog from plugins via an extension helper:
  ```kotlin
  internal val Project.libs get(): VersionCatalog =
      extensions.getByType<VersionCatalogsExtension>().named("libs")
  ```
- The feature convention plugin should add the feature's universal dependencies so feature `build.gradle.kts` files stay nearly empty. A copy-paste feature `build.gradle.kts` skeleton is in [`../template.md`](../template.md).
- **Adding a new feature module:** create `feature/<name>/build.gradle.kts` applying `<conv>.android.feature` + `<conv>.android.compose` and set its `namespace`. The convention plugins supply the shared dependencies, so the file stays tiny (skeleton in [`../template.md`](../template.md)). If the project has **no** convention plugins, apply the underlying plugins directly instead. In a single-module project there is no per-feature build file — see the single-module variant below.

## Dependency Management — Version Catalog

- **All** versions, libraries, and plugins live in `gradle/libs.versions.toml`. No hardcoded versions in any `build.gradle.kts`.
- Group related libs and reference them by alias (`libs.androidx.compose.material3`).
- Declare project convention plugins in the `[plugins]` block with their local ids so modules use `alias(libs.plugins.<conv>...)`.
- Use a Compose BOM entry and add Compose artifacts **without** versions (the BOM aligns them).

> **Guardrail:** if you're copy-pasting Gradle config between modules, move it into `build-logic`. Add a version to the catalog, not to a module's build file.

## Single-module variant

A single-module project has **one** `build.gradle.kts`, so there are no convention plugins to write — the Convention Plugins section above applies only to multi-module builds. The single module applies the plugins it needs directly (Android application, Kotlin, Compose, Hilt, Room, serialization); a skeleton is in [`../template.md`](../template.md). The **version-catalog** discipline is unchanged: all versions/libraries/plugins still live in `gradle/libs.versions.toml`, referenced by alias, with a Compose BOM.
