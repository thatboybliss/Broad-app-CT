package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PodcastEpisode
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PodcastPlayerOverlay(
    episode: PodcastEpisode?,
    isPlaying: Boolean,
    currentTime: Long,
    totalDuration: Long,
    playbackSpeed: Float,
    activeDialogueIndex: Int,
    visualizerAmplitude: Float,
    onTogglePlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onChangeSpeed: () -> Unit,
    onToggleFavorite: (PodcastEpisode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (episode == null) return

    var isExpanded by remember { mutableStateOf(false) }

    val hostColor = when (episode.hostName) {
        "Cosmic Gemini" -> ElectricPurple
        "DJ Nebula" -> NeonTeal
        else -> Color(0xFF00B4D8)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // --- 1. COLLAPSED MINI-PLAYER ---
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable { isExpanded = true }
                    .testTag("mini_player"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, hostColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small rotating disk
                    val infiniteTransition = rememberInfiniteTransition(label = "mini_rotation")
                    val rotationAngle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(if (isPlaying) 6000 else 0, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "mini_disk_angle"
                    )

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(hostColor.copy(alpha = 0.2f))
                            .border(1.dp, hostColor.copy(alpha = 0.5f), CircleShape)
                            .rotate(rotationAngle),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(ObsidianBg)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = episode.title,
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = episode.hostName,
                            color = hostColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Compact visualizer inside mini-player
                    CustomVisualizer(
                        amplitude = visualizerAmplitude,
                        isPlaying = isPlaying,
                        mode = VisualizerMode.BARS,
                        modifier = Modifier
                            .width(48.dp)
                            .height(18.dp)
                            .padding(end = 8.dp)
                    )

                    // Play/Pause Action
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .background(BorderColor, CircleShape)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- 2. EXPANDED FULL-SCREEN PLAYER ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.96f))
                    .clickable(enabled = false) {} // block click throughs
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Collapse",
                                tint = PureWhite,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Text(
                            text = "NOW BROADCASTING",
                            fontSize = 12.sp,
                            color = hostColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )

                        IconButton(
                            onClick = { onToggleFavorite(episode) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (episode.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (episode.isFavorite) Color.Red else PureWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Rotating Holographic Disc with Circular Visualizer Overlay
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular Audio Visualizer around the rotating disc
                        CustomVisualizer(
                            amplitude = visualizerAmplitude,
                            isPlaying = isPlaying,
                            mode = VisualizerMode.CIRCULAR,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Glowing Spinning Disc
                        val infiniteTransition = rememberInfiniteTransition(label = "expanded_rotation")
                        val discAngle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(if (isPlaying) 8000 else 0, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "disc_angle"
                        )

                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            hostColor.copy(alpha = 0.8f),
                                            ObsidianSurface,
                                            ObsidianBg
                                        )
                                    )
                                )
                                .border(2.dp, hostColor.copy(alpha = 0.5f), CircleShape)
                                .rotate(discAngle),
                            contentAlignment = Alignment.Center
                        ) {
                            // Sound tracks patterns (grooves) on disc
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .border(1.dp, hostColor.copy(alpha = 0.2f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .border(1.dp, hostColor.copy(alpha = 0.15f), CircleShape)
                            )

                            // Inner Core spindle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianBg)
                                    .border(1.5.dp, hostColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(hostColor)
                                )
                            }
                        }
                    }

                    // Episode Information Card
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = episode.title,
                            fontSize = 20.sp,
                            color = PureWhite,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Hosted by ${episode.hostName}",
                            fontSize = 14.sp,
                            color = hostColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Transcript Overlay Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (episode.isUserGenerated && activeDialogueIndex >= 0) {
                                // Display speaking dialog
                                val dialogueParts = remember(episode) {
                                    try {
                                        val arr = JSONArray(episode.scriptJson)
                                        val list = mutableListOf<DialoguePart>()
                                        for (i in 0 until arr.length()) {
                                            val obj = arr.getJSONObject(i)
                                            list.add(DialoguePart(obj.getString("speaker"), obj.getString("text")))
                                        }
                                        list
                                    } catch (e: Exception) {
                                        emptyList()
                                    }
                                }

                                val currentPart = dialogueParts.getOrNull(activeDialogueIndex)
                                if (currentPart != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentPart.speaker.uppercase(),
                                            color = if (currentPart.speaker == "Cosmic Gemini") ElectricPurple else NeonTeal,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${currentPart.text}\"",
                                            color = PureWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                // Default description static show
                                Text(
                                    text = episode.description,
                                    color = MutedText,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Slidable Time Controller Slider
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Slider(
                            value = currentTime.toFloat().coerceIn(0f, totalDuration.toFloat()),
                            onValueChange = { onSeek(it.toLong()) },
                            valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = hostColor,
                                activeTrackColor = hostColor,
                                inactiveTrackColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentTime),
                                fontSize = 11.sp,
                                color = MutedText
                            )
                            Text(
                                text = formatTime(totalDuration),
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    // Media Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playback Speed Action
                        TextButton(
                            onClick = onChangeSpeed,
                            modifier = Modifier.size(48.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = NeonTeal)
                        ) {
                            Text(
                                text = "${playbackSpeed}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Skip Prev
                        IconButton(
                            onClick = { onSeek((currentTime - 10000L).coerceAtLeast(0L)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = PureWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Glowing primary Play/Pause Center Trigger
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(hostColor)
                                .clickable { onTogglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = PureWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Skip Next
                        IconButton(
                            onClick = { onSeek((currentTime + 10000L).coerceAtMost(totalDuration)) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Forward10,
                                contentDescription = "Forward 10s",
                                tint = PureWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Dismiss button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MutedText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helpers for formatted seconds duration
fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

data class DialoguePart(
    val speaker: String,
    val text: String
)
