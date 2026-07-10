package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LiveScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Standard smooth screen routing
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(durationMillis = 400),
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel)
                            AppScreen.LIVE -> LiveScreen(viewModel = viewModel)
                            AppScreen.HISTORY -> HistoryScreen(viewModel = viewModel)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
