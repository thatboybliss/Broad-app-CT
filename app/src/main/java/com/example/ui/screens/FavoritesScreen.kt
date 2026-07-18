package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PodcastEpisode
import com.example.ui.theme.BorderColor
import com.example.ui.theme.MutedText
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PureWhite

@Composable
fun FavoritesScreen(
    favoriteEpisodes: List<PodcastEpisode>,
    currentEpisode: PodcastEpisode?,
    isPlaying: Boolean,
    onEpisodeSelect: (PodcastEpisode) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (PodcastEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "My Saved Broadcasts",
                    fontSize = 24.sp,
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Bookmarks and generated AI scripts you have starred.",
                    fontSize = 13.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (favoriteEpisodes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No saved broadcasts",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Go to the Discover tab or AI Studio to explore or synthesize new episodes, then tap the heart icon to bookmark them.",
                            color = MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = favoriteEpisodes,
                key = { it.id }
            ) { episode ->
                PodcastCard(
                    episode = episode,
                    isActive = currentEpisode?.id == episode.id,
                    isPlaying = isPlaying && currentEpisode?.id == episode.id,
                    onSelect = { onEpisodeSelect(episode) },
                    onPlayClick = {
                        if (currentEpisode?.id == episode.id) {
                            onTogglePlayPause()
                        } else {
                            onEpisodeSelect(episode)
                        }
                    },
                    onFavoriteClick = { onToggleFavorite(episode) }
                )
            }
        }
    }
}
