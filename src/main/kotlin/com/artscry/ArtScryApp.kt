package com.artscry

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.artscry.data.database.ArtScryDatabase
import com.artscry.core.common.AppState
import com.artscry.ui.components.AppImageLoader
import com.artscry.ui.screens.MainScreen
import com.artscry.ui.theme.ArtScryTheme
import kotlinx.coroutines.runBlocking

fun main() {
    ArtScryDatabase.initialize()

    setupAppExitHandler()

    application {
        val state = remember { AppState() }

        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition(0.dp, 0.dp),
            width = 800.dp,
            height = 600.dp
        )

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "ArtScry",
            undecorated = false,
            transparent = false,
            alwaysOnTop = state.isAlwaysOnTop,
            onPreviewKeyEvent = {
                when {
                    (setOf(Key.DirectionRight, Key.DirectionUp, Key.L)
                        .contains(it.key) && it.type == KeyEventType.KeyDown) -> {
                        if (!state.isBlackedOut) {
                            state.onNextImage()
                        }
                        true
                    }

                    (setOf(
                        Key.DirectionLeft, Key.DirectionDown, Key.H
                    ).contains(it.key) && it.type == KeyEventType.KeyDown && !state.isTimerActive) -> {
                        state.onPreviousImage()
                        true
                    }

                    else -> false
                }
            }
        ) {
            ArtScryTheme {
                MainScreen(state)
            }
        }
    }

}

fun onAppClose() {
    AppImageLoader.clearMemoryCache()

}

fun setupAppExitHandler() {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            onAppClose()
        }
    })
}

