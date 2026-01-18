package com.example.luximmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.util.NetworkMonitor
import com.example.luximmo.ui.LuxImmoApp
import com.example.designsystem.theme.LuxImmoTheme
import com.example.luximmo.ui.rememberLuxImmoAppState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberLuxImmoAppState(
                networkMonitor = networkMonitor,
            )
            LuxImmoTheme {
                LuxImmoApp(appState)
            }
        }
    }
}