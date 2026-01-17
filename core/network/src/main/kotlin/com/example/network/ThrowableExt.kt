package com.example.network

import com.example.domain.Result
import java.io.IOException

fun Throwable.toErrorResult(): Result.Error =
    when (this) {
        is IOException -> Result.Error.NoInternetConnection
        else -> Result.Error.Unknown
    }