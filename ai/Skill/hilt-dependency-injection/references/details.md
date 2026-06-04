# Dependency Injection (Hilt)

> Use when adding Hilt modules, bindings, dispatchers, or scopes.

- `@HiltAndroidApp` on the `Application`; `@AndroidEntryPoint` on the `Activity`; `@HiltViewModel` on ViewModels.
- **`@Binds`** in `abstract` modules to bind an interface to its `internal` implementation.
- **`@Provides`** in `object` modules for types you construct (Retrofit, OkHttp, Json, Room DB/DAOs).
- Install in the correct component (`SingletonComponent` for app-wide singletons).
- **Inject dispatchers** behind qualifiers; never reference `Dispatchers.IO/Default/Main` directly in business code. Provide them (and a `CoroutineScope(SupervisorJob() + defaultDispatcher)` for `@ApplicationScope`) in one DI module in `:core:common`. *(Qualifier skeleton in [`../template.md`](../template.md).)*

**Why qualified dispatchers + `ResourceProvider`:** every ViewModel and repository becomes unit-testable with a `TestDispatcher` and a fake resource provider — no Robolectric, no instrumentation.

> **Guardrail:** Hilt is the only DI mechanism. Don't switch frameworks or hand-roll ad-hoc singletons / `object` service locators — bind app-wide collaborators through Hilt in the correct component, and inject dispatchers behind qualifiers rather than referencing `Dispatchers.X` directly.

## Single-module variant

Hilt works the same. The dispatcher/scope qualifiers and the module that provides them live in a `di/` package instead of `:core:common`; `@Binds` / `@Provides` modules live beside the packages they wire. There is a single `SingletonComponent` graph either way.
