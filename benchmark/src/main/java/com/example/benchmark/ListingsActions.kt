package com.example.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** Application id of the `:app` module that the benchmarks run against. */
const val PACKAGE_NAME = "com.example.luximmo"

private const val LIST_LOAD_TIMEOUT_MS = 10_000L
private const val SCROLL_ITERATIONS = 3

/**
 * Waits for the listings list to load, then flings through it.
 *
 * Shared by [BaselineProfileGenerator] (to capture the list/row/image-loading code for the
 * baseline profile) and [ListingsScrollBenchmark] (to measure frame timing while scrolling).
 *
 * The list is a Compose `LazyColumn` with no test tag, and the app does not expose Compose
 * test tags as resource ids, so we locate it via its scrollable semantics ([By.scrollable])
 * rather than by id. The list loads from the network behind a loader, hence the [Until.wait].
 *
 * @return `true` if the list appeared and was scrolled, `false` if it never loaded (e.g. offline).
 */
fun MacrobenchmarkScope.scrollListings(): Boolean {
    // Give the network-backed list time to replace the loading spinner.
    if (!device.wait(Until.hasObject(By.scrollable(true)), LIST_LOAD_TIMEOUT_MS)) {
        return false
    }

    val list = device.findObject(By.scrollable(true)) ?: return false
    // Keep gestures away from the edges so they don't trigger pull-to-refresh (top) or the
    // system navigation area (bottom).
    list.setGestureMargin(device.displayHeight / 5)

    repeat(SCROLL_ITERATIONS) {
        list.fling(Direction.DOWN)
        device.waitForIdle()
    }
    list.fling(Direction.UP)
    device.waitForIdle()
    return true
}
