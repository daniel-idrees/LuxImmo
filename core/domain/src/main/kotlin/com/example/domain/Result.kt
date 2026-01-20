package com.example.domain

/**
 * A sealed interface representing the result of an operation.
 *
 * @param T The type of data associated with the result.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

sealed interface AppError {
    data object NoInternetConnection : AppError
    data object Unknown : AppError
}