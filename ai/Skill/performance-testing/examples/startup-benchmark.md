# Example — a startup Macrobenchmark and Baseline Profile

Generic, filled-in examples for the `:benchmark` module. Replace `com.example.myapp` with your app's id.

## Startup timing benchmark

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = "com.example.myapp",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

## Baseline Profile generator

```kotlin
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = "com.example.myapp") {
        pressHome()
        startActivityAndWait()
        // scroll the first screen so its hot paths are captured
        device.findObject(By.res("article_1"))?.let { device.waitForIdle() }
    }
}
```

The generated profile ships with the app so hot code paths are compiled ahead of time, improving cold start and scrolling.
