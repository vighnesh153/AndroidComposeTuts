package com.example.androidcomposetuts.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
internal fun IntroCircle() {
    Card(
        modifier = Modifier
            .padding(3.dp)
            .size(145.dp)
            .clickable(onClick = {
                Log.d("IntroCircle:Tap", "Tapped")
            }),
        shape = CircleShape,
        elevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "Tap")
        }
    }
}

@Composable
fun Intro() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.secondary
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "I am Ironman",
                style = TextStyle(fontSize = 35.sp, fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            IntroCircle()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IntroPreview() {
    Intro()
}
