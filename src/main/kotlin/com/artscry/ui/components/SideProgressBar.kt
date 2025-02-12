package com.artscry.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SideProgressBar(
    timeLeft: Int,
    totalTime: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight(timeLeft.toFloat() / totalTime)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(61, 54, 89, 255))
                .align(Alignment.BottomCenter)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(10.dp)
        ) {
            Text(
                text = timeLeft.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}