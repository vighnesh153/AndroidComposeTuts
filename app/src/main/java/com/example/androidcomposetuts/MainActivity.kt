package com.example.androidcomposetuts

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidcomposetuts.components.NoteApp
import com.example.androidcomposetuts.ui.theme.AndroidComposeTutsTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidComposeTuts: Application() {}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun App() {
    AndroidComposeTutsTheme {
        Surface(color = MaterialTheme.colors.background) {
//            Intro()
//            TipCalculator()
//            Movie()
            NoteApp()
        }
    }
}
