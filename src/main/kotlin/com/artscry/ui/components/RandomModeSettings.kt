package com.artscry.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.artscry.model.RandomModeConfig

@Composable
fun <T : RandomModeConfig> RandomModeSettings(
    config: T,
    imageLimitStr: String,
    onImageLimitChange: (String) -> Unit,
    onConfigChange: (T) -> Unit,
    updateConfig: T.(Boolean, Int?) -> T
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = config.randomMode,
            onCheckedChange = {
                val newConfig = config.updateConfig(
                    it,
                    if (it) imageLimitStr.toIntOrNull() ?: 10 else null
                )
                onConfigChange(newConfig)
            }
        )
        Text("Random Mode")
    }

    if (config.randomMode) {
        OutlinedTextField(
            value = imageLimitStr,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { char -> char.isDigit() }) {
                    onImageLimitChange(newValue)
                    val newConfig = config.updateConfig(
                        true,
                        newValue.toIntOrNull() ?: 10
                    )
                    onConfigChange(newConfig)
                }
            },
            label = { Text("Number of Images (default to 10)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}