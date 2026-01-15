package com.example.domain

sealed interface Result {
    data object Success : Result

    sealed interface Error : Result {
        data object NoInternetConnection : Error
        data object Unknown : Error
    }
}