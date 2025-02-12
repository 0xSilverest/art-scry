package com.artscry.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artscry.util.PreferencesManager
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Font
import java.io.File
import javax.swing.*
import javax.swing.plaf.ColorUIResource
import javax.swing.plaf.FontUIResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderSelector(onFolderSelected: (String) -> Unit) {
    var showChooser by remember { mutableStateOf(false) }
    var showRecentMenu by remember { mutableStateOf(false) }
    val recentFolders = remember { mutableStateOf(PreferencesManager.getRecentFolders()) }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = { showChooser = true }
            ) {
                Text("Select Folder")
            }


            Box {
                IconButton(
                    onClick = { showRecentMenu = true },
                    enabled = recentFolders.value.isNotEmpty()
                ) {
                    Text("↓")
                }

                DropdownMenu(
                    expanded = showRecentMenu,
                    onDismissRequest = { showRecentMenu = false },
                    modifier = Modifier.width(300.dp)
                ) {
                    recentFolders.value.forEach { path ->
                        TooltipArea(
                            tooltip = {
                                Surface(
                                    modifier = Modifier.padding(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    elevation = 4.dp
                                ) {
                                    Text(
                                        text = path,
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            delayMillis = 600
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showRecentMenu = false
                                    onFolderSelected(path)
                                }
                            ) {
                                Text(
                                    text = File(path).name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (recentFolders.value.isNotEmpty()) {
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                PreferencesManager.clearPreferences()
                                recentFolders.value = emptyList()
                                showRecentMenu = false
                            }
                        ) {
                            Text("Clear History", color = MaterialTheme.colors.error)
                        }
                    }
                }
            }
        }
    }

    if (showChooser) {
        CustomFileChooser(
            initialDirectory = PreferencesManager.getLastFolder(),
            onFolderSelected = { path ->
                PreferencesManager.setLastFolder(path)
                recentFolders.value = PreferencesManager.getRecentFolders()
                onFolderSelected(path)
            },
            onDismiss = { showChooser = false }
        )
    }
}

private fun setupFileChooserTheme(isDarkMode: Boolean) {
    val colors = if (isDarkMode) {
        mapOf(
            "background" to Color(0xFF1E1E1E.toInt()),
            "foreground" to Color(0xFFE0E0E0.toInt()),
            "text" to Color(0xFFE0E0E0.toInt()),
            "textBackground" to Color(0xFF2D2D2D.toInt()),
            "selectionBackground" to Color(0xFF404040.toInt()),
            "selectionForeground" to Color(0xFFFFFFFF.toInt()),
            "controlBackground" to Color(0xFF2D2D2D.toInt()),
            "controlText" to Color(0xFFE0E0E0.toInt()),
            "controlHighlight" to Color(0xFF404040.toInt())
        )
    } else {
        mapOf(
            "background" to Color(0xFFFAFAFA.toInt()),
            "foreground" to Color(0xFF000000.toInt()),
            "text" to Color(0xFF000000.toInt()),
            "textBackground" to Color(0xFFFFFFFF.toInt()),
            "selectionBackground" to Color(0xFF6B5B95.toInt()),
            "selectionForeground" to Color(0xFFFFFFFF.toInt()),
            "controlBackground" to Color(0xFFFFFFFF.toInt()),
            "controlText" to Color(0xFF000000.toInt()),
            "controlHighlight" to Color(0xFF6B5B95.toInt())
        )
    }

    colors.forEach { (key, color) ->
        UIManager.put(key, ColorUIResource(color))
        UIManager.put("FileChooser.$key", ColorUIResource(color))
        UIManager.put("Panel.$key", ColorUIResource(color))
        UIManager.put("Button.$key", ColorUIResource(color))
    }

    val defaultFont = FontUIResource("Segoe UI", Font.PLAIN, 12)
    UIManager.put("FileChooser.font", defaultFont)
    UIManager.put("Button.font", defaultFont)
}

private fun styleFileChooserComponents(chooser: JFileChooser, isDarkMode: Boolean) {
    val backgroundColor = if (isDarkMode) Color(0xFF1E1E1E.toInt()) else Color(0xFFFAFAFA.toInt())
    val foregroundColor = if (isDarkMode) Color(0xFFE0E0E0.toInt()) else Color(0xFF000000.toInt())
    val buttonColor = if (isDarkMode) Color(0xFF2D2D2D.toInt()) else Color(0xFF6B5B95.toInt())
    val buttonTextColor = if (isDarkMode) Color(0xFFE0E0E0.toInt()) else Color(0xFFFFFFFF.toInt())

    fun styleComponent(component: Component) {
        when (component) {
            is JPanel -> {
                component.background = backgroundColor
                component.foreground = foregroundColor
            }

            is JButton -> {
                component.background = buttonColor
                component.foreground = buttonTextColor
                component.isBorderPainted = false
                component.isOpaque = true
            }

            is JLabel -> {
                component.foreground = foregroundColor
            }

            is JTextField -> {
                component.background = if (isDarkMode) Color(0xFF2D2D2D.toInt()) else Color.WHITE
                component.foreground = foregroundColor
                component.caretColor = foregroundColor
            }

            is JList<*> -> {
                component.background = backgroundColor
                component.foreground = foregroundColor
            }

            is JScrollPane -> {
                component.background = backgroundColor
                component.viewport.background = backgroundColor
            }
        }

        if (component is Container) {
            component.components.forEach { styleComponent(it) }
        }
    }

    styleComponent(chooser)
}