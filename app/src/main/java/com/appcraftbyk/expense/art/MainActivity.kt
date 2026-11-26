package com.appcraftbyk.expense.art

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.appcraftbyk.expense.art.ui.ExpenseArtApp
import com.appcraftbyk.expense.art.ui.theme.ExpenseArtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseArtTheme {
                ExpenseArtApp()
            }
        }
    }
}
