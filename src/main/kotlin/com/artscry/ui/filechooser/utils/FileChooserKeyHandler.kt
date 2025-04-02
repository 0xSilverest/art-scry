package com.artscry.ui.filechooser.utils

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.*
import com.artscry.ui.filechooser.models.FileChooserState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FileChooserKeyHandler(
    private val state: FileChooserState,
    private val mainFocusRequester: FocusRequester,
    private val searchFocusRequester: FocusRequester,
    private val coroutineScope: CoroutineScope
) {
    fun handleGridKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        return when (event.key) {
            Key.DirectionDown -> handleDownNavigation()
            Key.DirectionUp -> handleUpNavigation()
            Key.DirectionRight -> handleRightNavigation()
            Key.DirectionLeft -> handleLeftNavigation()

            Key.J -> handleDownNavigation()
            Key.K -> handleUpNavigation()
            Key.L -> handleRightNavigation()
            Key.H -> handleLeftNavigation()

            Key.Enter -> openSelectedFolder()
            Key.Backspace -> navigateUp()
            Key.Spacebar -> selectCurrentFolder()

            Key.F -> {
                if (event.isCtrlPressed) activateSearch()
                else false
            }

            else -> false
        }
    }

    fun handleListKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        return when (event.key) {
            Key.DirectionDown, Key.J -> {
                if (state.selectedIndex.value < state.entries.value.size - 1) {
                    state.selectItem(state.selectedIndex.value + 1)
                }
                true
            }

            Key.DirectionUp, Key.K -> {
                if (state.selectedIndex.value > 0) {
                    state.selectItem(state.selectedIndex.value - 1)
                }
                true
            }

            Key.Enter -> openSelectedFolder()
            Key.Backspace -> navigateUp()
            Key.Spacebar -> selectCurrentFolder()

            Key.F -> {
                if (event.isCtrlPressed) activateSearch()
                else false
            }

            else -> false
        }
    }

    fun handleSearchKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false

        return when (event.key) {
            Key.Enter -> {
                if (state.searchQuery.value.isNotEmpty() && state.entries.value.isNotEmpty()) {
                    state.selectedIndex.value = 0

                    coroutineScope.launch {
                        kotlinx.coroutines.delay(50)
                        mainFocusRequester.requestFocus()
                    }
                } else {
                    mainFocusRequester.requestFocus()
                }
                state.isSearchFocused.value = false
                true
            }

            Key.Escape -> {
                if (state.searchQuery.value.isNotEmpty()) {
                    state.clearSearch()
                    true
                } else {
                    state.isSearchFocused.value = false
                    mainFocusRequester.requestFocus()
                    true
                }
            }

            Key.Tab -> {
                if (!event.isShiftPressed) {
                    state.isSearchFocused.value = false
                    mainFocusRequester.requestFocus()
                    true
                } else false
            }

            else -> false
        }
    }

    private fun activateSearch(): Boolean {
        state.isSearchFocused.value = true
        searchFocusRequester.requestFocus()
        return true
    }

    private fun handleDownNavigation(): Boolean {
        val newIndex = state.selectedIndex.value + state.columnsCount.value
        if (newIndex < state.entries.value.size) state.selectItem(newIndex)
        return true
    }

    private fun handleUpNavigation(): Boolean {
        val newIndex = state.selectedIndex.value - state.columnsCount.value
        if (newIndex >= 0) state.selectItem(newIndex)
        return true
    }

    private fun handleRightNavigation(): Boolean {
        if ((state.selectedIndex.value + 1) % state.columnsCount.value != 0
            && state.selectedIndex.value < state.entries.value.size - 1) {
            state.selectItem(state.selectedIndex.value + 1)
            return true
        }
        return false
    }

    private fun handleLeftNavigation(): Boolean {
        if (state.selectedIndex.value % state.columnsCount.value != 0
            && state.selectedIndex.value > 0) {
            state.selectItem(state.selectedIndex.value - 1)
            return true
        }
        return false
    }

    private fun openSelectedFolder(): Boolean {
        return state.openFolder(state.selectedIndex.value)
    }

    private fun navigateUp(): Boolean {
        return state.navigateUp()
    }

    private fun selectCurrentFolder(): Boolean {
        if (state.entries.value.isNotEmpty()) {
            state.entries.value.getOrNull(state.selectedIndex.value)?.let { file ->
                if (file.isDirectory) {
                    state.selectedFolder.value = file.toPath()
                }
            }
        }
        return true
    }
}
