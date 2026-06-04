# Wiring the App Together — Template

Fill-in skeleton. Provide these qualifiers (and an `@ApplicationScope` `CoroutineScope(SupervisorJob() + defaultDispatcher)`) in one DI module in `:core:common`.

## Coroutine dispatcher + scope qualifiers

```kotlin
@Qualifier @Retention(RUNTIME) annotation class IoDispatcher
@Qualifier @Retention(RUNTIME) annotation class DefaultDispatcher
@Qualifier @Retention(RUNTIME) annotation class MainDispatcher
@Qualifier @Retention(RUNTIME) annotation class ApplicationScope
```
