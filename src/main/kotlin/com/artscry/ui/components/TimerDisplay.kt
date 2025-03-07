package com.artscry.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.artscry.core.domain.model.TimerConfig
import kotlinx.coroutines.delay

@Composable
fun TimerDisplay(
    config: TimerConfig,
    imageChangeCount: Int,
    onTimerComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableStateOf(config.duration) }
    var isBlackedOut by remember { mutableStateOf(false) }

    LaunchedEffect(imageChangeCount) {
        timeLeft = config.duration
        isBlackedOut = false
    }

    LaunchedEffect(config) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--

            if (config.blackoutEnabled) {
                val blackoutTime = config.blackoutDuration ?: config.duration
                isBlackedOut = timeLeft <= blackoutTime
            }

            if (timeLeft == 0) {
                onTimerComplete()
                timeLeft = config.duration
                isBlackedOut = false
            }
        }
    }

    Box {
        SideProgressBar(
            timeLeft = timeLeft,
            totalTime = config.duration,
            modifier = modifier
        )

        if (isBlackedOut) {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$timeLeft",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}