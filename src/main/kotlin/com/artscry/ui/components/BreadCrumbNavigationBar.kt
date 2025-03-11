package com.artscry.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.nio.file.Path

@Composable
fun BreadcrumbNavigationBar(
    currentPath: Path,
    onNavigate: (Path) -> Unit,
    modifier: Modifier = Modifier
) {
    val parts = mutableListOf<Pair<String, Path>>()
    var current: Path? = currentPath

    while (current != null) {
        val name = current.fileName?.toString() ?: current.toString()
        parts.add(0, name to current)
        current = current.parent
    }

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for ((index, part) in parts.withIndex()) {
            val (name, path) = part

            if (index > 0) {
                Icon(
                    painter = painterResource("icons/chevronRight.png"),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp).padding(horizontal = 2.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Surface(
                modifier = Modifier.clickable { onNavigate(path) },
                color = if (index == parts.size - 1)
                    MaterialTheme.colors.primary.copy(alpha = 0.1f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    fontWeight = if (index == parts.size - 1) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
