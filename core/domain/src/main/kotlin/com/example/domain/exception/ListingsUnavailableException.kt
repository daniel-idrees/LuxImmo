package com.example.domain.exception

import com.example.domain.Result

class ListingsUnavailableException(val errorResult: Result.Error) : Exception()