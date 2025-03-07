package com.artscry.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.Tag
import com.artscry.util.StringExtensions.toColorInt

@Composable
fun TagChip(
    tag: com.artscry.core.domain.model.Tag,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    val tagColorValue = tag.color?.let { Color(it.toColorInt()) } ?: MaterialTheme.colors.primary
    val tagColor = remember(tag.color) { tagColorValue }

    val backgroundColor by animateColorAsState(
        if (selected) tagColor.copy(alpha = 0.2f) else Color.Transparent
    )

    val borderColor by animateColorAsState(
        if (selected) tagColor else MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (tag.category != null) {
                Text(
                    text = tag.category,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            Text(
                text = tag.name,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface
            )

            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove tag",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}