package com.example.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile for the app.
 *
 * Run this with the `:benchmark` module's generate task (e.g.
 * `./gradlew :app:generateReleaseBaselineProfile`). The androidx.baselineprofile plugin runs
 * this test on a device/emulator, captures the classes and methods exercised in the
 * [collect] block, and bundles the resulting profile into `:app` so ART can pre-compile that
 * code at install time — improving cold-start and first-frame performance.
 *
 * The flow below covers app startup plus scrolling the listings list, so the row
 * composables, image-loading, and lazy-list machinery on the main screen get pre-compiled.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        // Also emit a startup profile, which optimizes the most startup-critical code paths.
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        scrollListings()
    }
}
