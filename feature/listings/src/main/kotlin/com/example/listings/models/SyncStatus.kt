package com.example.listings.models

import com.example.domain.AppError

/**
 * Sealed class to represent the sync status of the data
 */
internal sealed class SyncStatus {
    data object Finished : SyncStatus()
    data object NotStarted : SyncStatus()
    data object Running : SyncStatus()
    data class Error(val error: AppError) : SyncStatus()
}
