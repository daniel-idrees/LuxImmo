# LuxImmo: Modern Android Architecture Demo

A luxury real estate listing app which shows available list and then it's detail.

#### Description

LuxImmo is a sample Android application designed to showcase a modern, offline-first architecture using 100% Kotlin and Jetpack Compose. It serves as a technical demonstration of best practices in Android development, including clean architecture, a reactive MVI pattern, advanced Gradle configuration, and a robust testing strategy.

https://github.com/user-attachments/assets/abd905c4-365b-4345-b852-500928f62ed1


## Architecture

This project follows the principles of **Clean Architecture**, with a clear separation between the UI, Domain, and Data layers. The overall architecture is **offline-first** and reactive, built around a unidirectional data flow.

*   **UI Layer (`:feature:listings`)**: Built entirely with **Jetpack Compose**. The UI layer is stateless and observes a single state stream from the ViewModel. User interactions are sent as `Action` events, following a **Model-View-Intent (MVI)** pattern.

*   **Domain Layer (`:core:domain`)**: This is the core of the application, containing business logic, domain models, and use case interactors. It is a pure Kotlin module with no dependencies on the Android framework or any implementation details.

*   **Data Layer (`:core:data`, `:core:network`, `:core:database`)**: Implements the repository pattern. The `OfflineFirstListingRepository` is the single source of truth, coordinating between the network data source (Retrofit) and the local database (Room) to provide a seamless offline experience.

## Tech Stack & Key Libraries

This project uses a curated set of modern, best-practice libraries and tools:

*   **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for declarative UI.
*   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) for managing dependencies.
*   **Asynchronicity**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-guide.html) for managing background threads and reactive data streams.
*   **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/) for efficient and clean API requests.
*   **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) for parsing JSON from the network.
*   **Database**: [Room](https://developer.android.com/training/data-storage/room) for local, persistent storage.
*   **Image Loading**: [Coil](https://coil-kt.github.io/coil/) for image loading.
*   **Testing**:
    *   [JUnit 4](https://junit.org/junit4/) & [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/) for unit testing.
    *   `kotlinx-coroutines-test` (`TestScope`, `UnconfinedTestDispatcher`) for coroutine testing.
    *   [Turbine](https://github.com/cashapp/turbine) for robustly testing Kotlin Flows.
*   **Performance**: [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) & [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview) for measuring and optimizing startup and scroll performance.


## Key Features & Implementation Details

### Build Logic with Gradle Convention Plugins

The project features an advanced Gradle setup using a `build-logic` module with custom **Gradle Convention Plugins**. This approach centralizes and simplifies build configuration across all modules.

*   **DRY (Don't Repeat Yourself):** Common configurations (like `compileSdk`, Kotlin options, or standard dependencies for Hilt/Compose) are defined once.
*   **Type Safety & Maintainability:** Plugins are written in Kotlin, providing type safety and making it simple to update a library version across the entire project by changing a single line.

### Reactive MVI with a Single Source of Truth

The UI layer is fully reactive. The `ViewModel` exposes a single `StateFlow<UiState>` which the Composable UI observes. User interactions are sent to the `ViewModel` as `UiAction` events. The `ViewModel` contains a single, declarative stream that combines all data sources into one state object, eliminating race conditions and making the UI predictable and easy to debug.

### Offline-First Repository

The `OfflineFirstListingRepository` acts as the single source of truth for all property data. When a refresh is requested, it fetches data from the network and saves it to the local Room database. The UI layer always observes the data from the Room database, ensuring that the app is fully functional even when the device is offline.

### Comprehensive Testing Strategy

The project includes a robust suite of tests demonstrating best practices:

*   **ViewModel Tests:** Verify the business logic and state transformations in the ViewModels. Uses `TestScope` and Turbine to test `Flow`-based state management.
*   **Repository Tests:** Test the integration between the network and database layers.
*   **Mapper Tests:** Simple unit tests verify that data is correctly transformed between the network, database, and domain layers.

## Performance & Benchmarking

The project includes a dedicated `:benchmark` module (`com.android.test`) that uses **Jetpack Macrobenchmark** to measure real-world performance and generate a **Baseline Profile**. The benchmarks run against the `:app` module on a connected device or emulator.

### Baseline Profile

A [Baseline Profile](https://developer.android.com/topic/performance/baselineprofiles/overview) lists the classes and methods exercised during critical user journeys so that ART can pre-compile (AOT) that code at install time, instead of relying on the slower JIT during the first runs. This improves cold-start time and first-frame/scroll smoothness for end users.

*   `BaselineProfileGenerator` captures the journey of launching the app and scrolling the listings list, so the row composables, image-loading, and `LazyColumn` machinery get pre-compiled.
*   The `androidx.baselineprofile` plugin is applied in both `:benchmark` (the producer) and `:app` (the consumer), and `:app` ships `androidx.profileinstaller` to install the bundled profile at runtime.

Generate (and refresh) the profile with:

```bash
./gradlew :app:generateReleaseBaselineProfile
```

### Macrobenchmarks

Two macrobenchmarks measure the performance the user actually feels. Run them from Android Studio (the gutter run icon) or via Gradle, against a **release/benchmark build on a physical device** for representative numbers:

*   **`StartupBenchmark`** — measures cold-start time to first frame (`StartupTimingMetric`, 5 iterations, `StartupMode.COLD`). Requires `<profileable android:shell="true" />` in `:app`'s manifest.
*   **`ListingsScrollBenchmark`** — measures scroll/frame timing (`FrameTimingMetric`) while flinging the listings list. It runs the same flow under two compilation modes so you can confirm the baseline profile actually helps:
    *   `scrollNoCompilation` — JIT only (worst case).
    *   `scrollBaselineProfile` — uses the bundled baseline profile (what most users get).

> **Note:** Benchmarks should be run on a real device. Results from an emulator are not representative of real-world performance; the module sets `androidx.benchmark.suppressErrors = EMULATOR` so they can still be run locally for verification.

## How to Build and Run

1.  **Clone the Repository**
2.  **Configure the Base URL (optional)**

* **Demo mode:** The app shows built-in demo data without a Base Url.
* **Real backend:** Create a `local.properties` file in the project root and add:
  ```properties
  BASE_URL=https://your.api.base.url/ {You know the base url ;)}
  ```
  The app then loads live data instead of the demo data.

#### Possible Enhancements
- Better Place holder Image for listings with no image available. (https://github.com/daniel-idrees/LuxImmo/pull/1)
- Different Detail page design
- Better Screen for empty state where no listings are available.
- Improved view for landscape in the devices where only single detail screen is shown.
- Filtering listings result.
- Bookmarking listings.
