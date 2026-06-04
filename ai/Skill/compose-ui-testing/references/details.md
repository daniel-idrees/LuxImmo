
# Compose UI Testing

> Use when writing UI tests (`androidTest`) for Compose screens.

- `createAndroidComposeRule`; drive the **stateless** composable or screen via test tags; assert visibility/among nodes.
- Enable `testOptions.unitTests.isIncludeAndroidResources = true` for resource-dependent tests.
- Add a `testTag` to every element a test needs (`testTag("item_${id}")`).

## Single-module variant

No structural change: UI tests run from the app module's `androidTest` source set using the same `createAndroidComposeRule` and test tags. Drive the stateless composable directly, exactly as in a multi-module project.
