package com.artscry.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressNumber(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(8.dp)
    ) {
        val outlineColor = Color.Black
        val positions = listOf(
            Offset(-1f, -1f), Offset(1f, -1f),
            Offset(-1f, 1f), Offset(1f, 1f),
            Offset(0f, -1f), Offset(0f, 1f),
            Offset(-1f, 0f), Offset(1f, 0f)
        )


        positions.forEach { offset ->
            Text(
                text = "$current/$total",
                color = outlineColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.offset(
                    x = offset.x.dp,
                    y = offset.y.dp
                )
            )
        }


        Text(
            text = "$current/$total",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium
        )
    }
}