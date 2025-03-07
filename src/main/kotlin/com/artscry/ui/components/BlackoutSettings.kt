package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.BlackoutConfig

@Composable
fun BlackoutSettings(
    onSettingsChange: (com.artscry.core.domain.model.BlackoutConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(com.artscry.core.domain.model.BlackoutConfig()) }
    var blackoutDurationStr by remember { mutableStateOf("60") }
    var practiceTimerDurationStr by remember { mutableStateOf("300") }
    var imageLimitStr by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Blackout Practice Settings", style = MaterialTheme.typography.h6)

            OutlinedTextField(
                value = blackoutDurationStr,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        blackoutDurationStr = it
                        it.toIntOrNull()?.let { duration ->
                            config = config.copy(duration = duration)
                            onSettingsChange(config)
                        }
                    }
                },
                label = { Text("Blackout After (seconds)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = config.practiceTimerEnabled,
                    onCheckedChange = {
                        config = config.copy(practiceTimerEnabled = it)
                        onSettingsChange(config)
                    }
                )
                Text("Enable Practice Timer")
            }

            if (config.practiceTimerEnabled) {
                OutlinedTextField(
                    value = practiceTimerDurationStr,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            practiceTimerDurationStr = it
                            it.toIntOrNull()?.let { duration ->
                                config = config.copy(practiceTimerDuration = duration)
                                onSettingsChange(config)
                            }
                        }
                    },
                    label = { Text("Practice Duration (seconds)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            RandomModeSettings(
                config = config,
                imageLimitStr = imageLimitStr,
                onImageLimitChange = { imageLimitStr = it },
                onConfigChange = {
                    config = it
                    onSettingsChange(it)
                },
                updateConfig = com.artscry.core.domain.model.BlackoutConfig::updateRandomMode
            )
        }
    }
}