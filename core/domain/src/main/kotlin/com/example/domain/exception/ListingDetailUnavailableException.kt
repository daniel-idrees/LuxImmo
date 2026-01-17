package com.example.domain.exception

import com.example.domain.Result

class ListingDetailUnavailableException(val error: Result.Error) : Exception()