package com.artscry.core.domain.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artscry.core.domain.model.TimerConfig
import kotlinx.coroutines.CoroutineScope

class TimerManager(
    private val scope: CoroutineScope
) {
    var timerConfig by mutableStateOf<TimerConfig?>(null)
    var isTimerActive by mutableStateOf(false)

    fun startTimer(config: TimerConfig) {
        timerConfig = config
        isTimerActive = true
    }

    fun stopTimer() {
        isTimerActive = false
    }
}