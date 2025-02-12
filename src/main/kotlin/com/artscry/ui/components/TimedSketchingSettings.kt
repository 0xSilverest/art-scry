package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artscry.model.TimedSketchingConfig

@Composable
fun TimedSketchingSettings(
    onSettingsChange: (TimedSketchingConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(TimedSketchingConfig()) }
    var durationStr by remember { mutableStateOf("60") }
    var imageLimitStr by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Timed Sketching Settings", style = MaterialTheme.typography.h6)

            OutlinedTextField(
                value = durationStr,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        durationStr = it
                        it.toIntOrNull()?.let { duration ->
                            config = config.copy(duration = duration)
                            onSettingsChange(config)
                        }
                    }
                },
                label = { Text("Duration (seconds)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    durationStr = "60"
                    config = config.copy(duration = 60)
                    onSettingsChange(config)
                }) { Text("1m") }
                Button(onClick = {
                    durationStr = "300"
                    config = config.copy(duration = 300)
                    onSettingsChange(config)
                }) { Text("5m") }
                Button(onClick = {
                    durationStr = "600"
                    config = config.copy(duration = 600)
                    onSettingsChange(config)
                }) { Text("10m") }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = config.hideArrows,
                    onCheckedChange = {
                        config = config.copy(hideArrows = it)
                        onSettingsChange(config)
                    }
                )
                Text("Hide Navigation")
            }

            RandomModeSettings(
                config = config,
                imageLimitStr = imageLimitStr,
                onImageLimitChange = { imageLimitStr = it },
                onConfigChange = {
                    config = it
                    onSettingsChange(it)
                },
                updateConfig = TimedSketchingConfig::updateRandomMode
            )

        }
    }
}