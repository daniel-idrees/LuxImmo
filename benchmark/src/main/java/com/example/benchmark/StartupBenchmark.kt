package com.example.benchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures cold-start time to first frame for the app's launcher activity.
 *
 * Run from Android Studio (gutter icon) or via Gradle to see startup measurements and the
 * captured system traces for investigating regressions.
 *
 * Prerequisites:
 * 1) `<profileable android:shell="true" />` is declared in `:app`'s manifest `<application>` tag.
 * 2) Run against a release/benchmark build for representative numbers.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupCold() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
