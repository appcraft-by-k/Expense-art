package com.appcraftbyk.expense.art

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appcraftbyk.expense.art.data.prefs.ThemeMode
import com.appcraftbyk.expense.art.ui.ExpenseArtApp
import com.appcraftbyk.expense.art.ui.main.MainViewModel
import com.appcraftbyk.expense.art.ui.theme.ExpenseArtTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by mainViewModel.userPreferences.collectAsStateWithLifecycle()
            val darkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Keep system-bar icons legible when the app theme overrides the system setting.
            LaunchedEffect(darkTheme) {
                val transparent = android.graphics.Color.TRANSPARENT
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(transparent, transparent) { darkTheme }
                )
            }
            ExpenseArtTheme(darkTheme = darkTheme, dynamicColor = false) {
                ExpenseArtApp(mainViewModel = mainViewModel)
            }
        }
    }
}
