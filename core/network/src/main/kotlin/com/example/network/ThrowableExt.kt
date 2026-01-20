package com.example.network

import com.example.domain.AppError
import com.example.domain.Result
import java.io.IOException

/**
 * Converts a [Throwable] to an [Result.Error]
 */
fun Throwable.toErrorResult(): Result.Error =
    when (this) {
        is IOException -> Result.Error(error = AppError.NoInternetConnection)
        else -> Result.Error(error = AppError.Unknown)
    }