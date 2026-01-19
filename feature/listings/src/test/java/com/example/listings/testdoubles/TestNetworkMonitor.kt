package com.example.listings.testdoubles

import com.example.domain.util.NetworkMonitor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow


// TODO: to move to a common test module so that other features can also use it

class TestNetworkMonitor : NetworkMonitor {

    private val isOnlineFlow: MutableSharedFlow<Boolean> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override val isOnline: Flow<Boolean> = isOnlineFlow

    suspend fun setIsOnline(isOnline: Boolean) {
        isOnlineFlow.emit(isOnline)
    }
}

