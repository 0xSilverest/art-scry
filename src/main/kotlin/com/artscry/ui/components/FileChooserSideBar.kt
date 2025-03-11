package com.artscry.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.FavoriteLocation
import java.io.File

@Composable
fun FileChooserSidebar(
    recentFolders: List<String>,
    favorites: List<FavoriteLocation>,
    onFolderClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onRemoveFavorite: (FavoriteLocation) -> Unit,
    onAddCurrentLocation: () -> Unit,
    onClearRecentFolders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.surface.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (favorites.isNotEmpty()) {
                FavoritesSection(
                    favorites = favorites,
                    onFavoriteClick = onFavoriteClick,
                    onRemoveFavorite = onRemoveFavorite,
                    onAddCurrentLocation = onAddCurrentLocation,
                    modifier = Modifier.weight(0.4f)
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = onAddCurrentLocation) {
                        Text("+ Add Current Path to Favorites")
                    }
                }

                Divider(modifier = Modifier.padding(bottom = 8.dp))
            }

            Text(
                "Recent Folders",
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(if (favorites.isEmpty()) 1f else 0.6f)) {
                items(recentFolders) { path ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFolderClick(path) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            File(path).name,
                            style = MaterialTheme.typography.body2,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            path,
                            style = MaterialTheme.typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                }

                item {
                    if (recentFolders.isNotEmpty()) {
                        TextButton(
                            onClick = onClearRecentFolders,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Recent", color = MaterialTheme.colors.error)
                        }
                    }
                }
            }
        }
    }
}