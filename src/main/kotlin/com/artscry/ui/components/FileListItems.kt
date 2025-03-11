package com.artscry.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artscry.util.FileUtils
import java.io.File

@Composable
fun FileListItem(
    file: File,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 500) {
                    onDoubleClick()
                } else {
                    onClick()
                }
                lastClickTime = currentTime
            },
        color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f)
        else MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (file.isDirectory) "📁" else "📄",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = file.name,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!file.isDirectory) {
                Text(
                    text = FileUtils.formatFileSize(file.length()),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.width(80.dp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = FileUtils.formatLastModified(file.lastModified()),
                style = MaterialTheme.typography.caption,
                modifier = Modifier.width(120.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

