package com.artscry.core.domain.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlackoutManager(
    private val scope: CoroutineScope
) {
    var isBlackedOut by mutableStateOf(false)
    var onBlackout by mutableStateOf(false)
    var blackoutTimer: Job? = null
    var blackoutTimeRemaining by mutableStateOf<Int?>(null)

    var blackoutPracticeTimer: Job? = null
    var blackoutPracticeTimeRemaining by mutableStateOf<Int?>(null)

    fun startBlackout(duration: Int) {
        blackoutTimeRemaining = duration
        blackoutTimer?.cancel()
        blackoutTimer = scope.launch {
            while (blackoutTimeRemaining!! > 0) {
                delay(1000L)
                blackoutTimeRemaining = blackoutTimeRemaining!! - 1
            }
            isBlackedOut = true
        }
    }

    fun startBlackoutPracticeTimer(duration: Int, onComplete: () -> Unit) {
        blackoutPracticeTimeRemaining = duration
        blackoutPracticeTimer?.cancel()
        blackoutPracticeTimer = scope.launch {
            onBlackout = true
            while (blackoutPracticeTimeRemaining!! > 0) {
                delay(1000L)
                blackoutPracticeTimeRemaining = blackoutPracticeTimeRemaining!! - 1

                if (blackoutPracticeTimeRemaining == 0) {
                    onComplete()
                    break
                }
            }
        }
    }

    fun clearBlackout() {
        isBlackedOut = false
        onBlackout = false
        blackoutTimer?.cancel()
        blackoutTimer = null
        blackoutPracticeTimer?.cancel()
        blackoutPracticeTimer = null
        blackoutTimeRemaining = null
    }
}