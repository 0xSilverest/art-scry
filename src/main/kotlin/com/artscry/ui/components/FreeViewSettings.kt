package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.FreeViewConfig

@Composable
fun FreeViewSettings(
    onSettingsChange: (FreeViewConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(FreeViewConfig()) }
    var imageLimitStr by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Free View Settings", style = MaterialTheme.typography.h6)

            RandomModeSettings(
                config = config,
                imageLimitStr = imageLimitStr,
                onImageLimitChange = { imageLimitStr = it },
                onConfigChange = {
                    config = it
                    onSettingsChange(it)
                },
                updateConfig = FreeViewConfig::updateRandomMode
            )
        }
    }
}