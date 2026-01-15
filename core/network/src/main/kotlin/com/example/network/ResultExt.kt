package com.example.network

import kotlinx.coroutines.CancellationException

/**
 *
 * This function captures exceptions of type [Throwable],
 * returning them as a failure result. If a [CancellationException] is caught, it is rethrown
 * without wrapping it in a `Result`. This prevents unwanted interference with the cancellation
 * handling of Coroutines.
 * Reference: https://github.com/Kotlin/kotlinx.coroutines/issues/1814#issuecomment-1027931634
 *
 * @param block The suspending block of code to execute.
 * @return A [Result] containing the outcome of the block execution.
 *         If the block completes successfully, the result will be [Result.success].
 *         If an exception is thrown, it will be wrapped in [Result.failure]
 *         unless it is a [CancellationException], in which case it will be rethrown.
 */
inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
