package com.example.testing.util

import com.example.domain.util.NetworkMonitor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestNetworkMonitor : NetworkMonitor {

    private val isOnlineFlow: MutableSharedFlow<Boolean> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val isOnline: Flow<Boolean> = isOnlineFlow

    suspend fun setIsOnline(isOnline: Boolean) {
        isOnlineFlow.emit(isOnline)
    }
}