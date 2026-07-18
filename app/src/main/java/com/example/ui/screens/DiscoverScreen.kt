package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.PodcastEpisode
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    episodes: List<PodcastEpisode>,
    currentEpisode: PodcastEpisode?,
    isPlaying: Boolean,
    onEpisodeSelect: (PodcastEpisode) -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleFavorite: (PodcastEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    // State to track active host filter (null means show all)
    var selectedHostFilter by remember { mutableStateOf<String?>(null) }

    val filteredEpisodes = remember(episodes, selectedHostFilter) {
        if (selectedHostFilter == null) {
            episodes
        } else {
            episodes.filter { it.hostName == selectedHostFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp), // extra padding for player
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome Banner with premium Gradient border
        item {
            WelcomeBanner()
        }

        // 2. Host Selection Section
        item {
            Column {
                Text(
                    text = "AI Superstar Hosts",
                    fontSize = 18.sp,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        HostChip(
                            name = "All Hosts",
                            description = "View all content",
                            icon = Icons.AutoMirrored.Rounded.QueueMusic,
                            isSelected = selectedHostFilter == null,
                            accentColor = ElectricPurple,
                            onClick = { selectedHostFilter = null }
                        )
                    }
                    item {
                        HostChip(
                            name = "Cosmic Gemini",
                            description = "Astrophysics & Future",
                            icon = Icons.Rounded.Mic,
                            isSelected = selectedHostFilter == "Cosmic Gemini",
                            accentColor = ElectricPurple,
                            onClick = { selectedHostFilter = "Cosmic Gemini" }
                        )
                    }
                    item {
                        HostChip(
                            name = "DJ Nebula",
                            description = "Synthesizer Soundscapes",
                            icon = Icons.Rounded.Headset,
                            isSelected = selectedHostFilter == "DJ Nebula",
                            accentColor = NeonTeal,
                            onClick = { selectedHostFilter = "DJ Nebula" }
                        )
                    }
                    item {
                        HostChip(
                            name = "Detective Cipher",
                            description = "Cryptic Crime & Secrets",
                            icon = Icons.Rounded.Mic,
                            isSelected = selectedHostFilter == "Detective Cipher",
                            accentColor = Color(0xFF00B4D8),
                            onClick = { selectedHostFilter = "Detective Cipher" }
                        )
                    }
                }
            }
        }

        // 3. Featured Episodes Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedHostFilter == null) "Trending Broadcasts" else "$selectedHostFilter Episodes",
                    fontSize = 18.sp,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${filteredEpisodes.size} available",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }
        }

        // 4. List of episodes
        if (filteredEpisodes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No broadcasts found for this host.",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(
                items = filteredEpisodes,
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
                    onFavoriteClick = { onToggleFavorite(episode) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun WelcomeBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(ElectricPurple, NeonTeal))),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0x2200F5D4), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BETA BROADCAST",
                    color = NeonTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(id = R.string.welcome_headline),
                fontSize = 22.sp,
                color = PureWhite,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.welcome_subline),
                fontSize = 13.sp,
                color = MutedText,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HostChip(
    name: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ObsidianCard else ObsidianSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) accentColor else BorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.2f) else BorderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = if (isSelected) accentColor else PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = name,
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                color = MutedText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PodcastCard(
    episode: PodcastEpisode,
    isActive: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hostColor = when (episode.hostName) {
        "Cosmic Gemini" -> ElectricPurple
        "DJ Nebula" -> NeonTeal
        else -> Color(0xFF00B4D8)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) ObsidianCard else ObsidianSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) hostColor.copy(alpha = 0.5f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glow Disc avatar placeholder representing host/branding
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                hostColor.copy(alpha = 0.4f),
                                hostColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(1.dp, hostColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (episode.isUserGenerated) Icons.Rounded.Mic else Icons.Rounded.Headset,
                    contentDescription = null,
                    tint = hostColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = episode.hostName.uppercase(),
                        color = hostColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (episode.isUserGenerated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(ElectricPurple, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "AI SYNTH",
                                color = PureWhite,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = episode.title,
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = episode.description,
                    color = MutedText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fast Play Inline Action
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(BorderColor, CircleShape)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.FavoriteBorder else Icons.Filled.PlayArrow, // Toggle indicator or Play icon
                    contentDescription = "Quick Play",
                    tint = if (isActive) hostColor else PureWhite,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite Toggle
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("favorite_toggle")
            ) {
                Icon(
                    imageVector = if (episode.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (episode.isFavorite) Color.Red else MutedText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
