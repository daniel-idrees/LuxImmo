package com.example.domain.exception

import com.example.domain.Result

class ListingDetailUnavailableException(val errorResult: Result.Error) : Exception()