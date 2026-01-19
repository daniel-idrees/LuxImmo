# LuxImmo: Modern Android Architecture Demo

A luxury real estate listing app which shows available list and then it's detail.

#### Description

LuxImmo is a sample Android application designed to showcase a modern, offline-first architecture using 100% Kotlin and Jetpack Compose. It serves as a technical demonstration of best practices in Android development, including clean architecture, a reactive MVI pattern, advanced Gradle configuration, and a robust testing strategy.

<br>

<!-- TODO: Add a GIF or screenshot of the app in action -->
<!-- 
<p align="center">
  <img src="art/app_demo.gif" width="300"/>
</p> 
-->

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

## How to Build and Run

1.  **Clone the Repository**
2.  **Configure the Base URL**
   
This project requires a base URL for the network API to be specified.
1. In the root directory of the project, create a file named local.properties if it doesn't already exist.
2. Add the following line to the local.properties file:
Properties
BASE_URL=https://your.api.base.url/ {You know the base url ;)}

   
#### Possible Enhancements
- Better Place holder Image for listings with no image available.
- Better Screen for empty state where no listings are available.
- Improved view for landscape in the devices where only single detail screen is shown.
- Filtering listings result.
- Bookmarking listings.