package com.example.listings.models

import com.example.domain.AppError

internal sealed class SyncStatus {
    data object Finished : SyncStatus()
    data object NotStarted : SyncStatus()
    data object Running : SyncStatus()
    data class Error(val error: AppError) : SyncStatus()
}
