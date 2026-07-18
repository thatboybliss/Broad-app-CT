package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Headset
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.PodcastEpisode
import com.example.data.api.DialoguePart
import com.example.ui.components.CustomVisualizer
import com.example.ui.components.VisualizerMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.GenerationUiState
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    generationState: GenerationUiState,
    isPlaying: Boolean,
    currentEpisode: PodcastEpisode?,
    activeDialogueIndex: Int,
    visualizerAmplitude: Float,
    onGenerate: (String) -> Unit,
    onTogglePlay: () -> Unit,
    onClearState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var topicText by remember { mutableStateOf("") }
    val presetChips = listOf("Space Colony 2088", "Quantum Supercomputing", "History of Cryptography", "Cybernetic Humanity")
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title Banner
        item {
            Column {
                Text(
                    text = stringResource(id = R.string.studio_title),
                    fontSize = 24.sp,
                    color = PureWhite,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = stringResource(id = R.string.studio_subtitle),
                    fontSize = 13.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 2. Main workflow switcher based on current generation UI states
        when (generationState) {
            is GenerationUiState.Idle -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "What is your broadcast topic?",
                                fontSize = 16.sp,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )

                            // Prompt text input with glowing border states
                            OutlinedTextField(
                                value = topicText,
                                onValueChange = { topicText = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.prompt_placeholder),
                                        fontSize = 13.sp,
                                        color = MutedText
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = if (topicText.isNotEmpty()) {
                                                listOf(ElectricPurple, NeonTeal)
                                            } else {
                                                listOf(BorderColor, BorderColor)
                                            }
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = PureWhite,
                                    unfocusedTextColor = PureWhite,
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Preset chips row
                            Column {
                                Text(
                                    text = "SUGGESTED TECH TOPICS",
                                    fontSize = 10.sp,
                                    color = MutedText,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    presetChips.forEach { chip ->
                                        SuggestionChip(
                                            onClick = { topicText = chip },
                                            label = {
                                                Text(
                                                    text = chip,
                                                    fontSize = 11.sp,
                                                    color = if (topicText == chip) NeonTeal else PureWhite
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (topicText == chip) {
                                                    ElectricPurple.copy(alpha = 0.2f)
                                                } else {
                                                    ObsidianBg
                                                }
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (topicText == chip) NeonTeal else BorderColor
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Glowing action trigger
                            Button(
                                onClick = { onGenerate(topicText) },
                                enabled = topicText.trim().isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricPurple,
                                    disabledContainerColor = BorderColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (topicText.trim().isNotEmpty()) NeonTeal else MutedText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.btn_generate),
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is GenerationUiState.Generating -> {
                item {
                    GeneratingScreen()
                }
            }

            is GenerationUiState.Success -> {
                item {
                    val ep = generationState.episode
                    LiveScriptTeleprompter(
                        episode = ep,
                        isPlaying = isPlaying && currentEpisode?.id == ep.id,
                        activeDialogueIndex = activeDialogueIndex,
                        visualizerAmplitude = visualizerAmplitude,
                        onTogglePlay = onTogglePlay,
                        onNewRequest = onClearState
                    )
                }
            }

            is GenerationUiState.Error -> {
                item {
                    ErrorScreen(
                        message = generationState.message,
                        onRetry = onClearState
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        modifier = Modifier.fillMaxWidth(),
        content = { content() }
    )
}

@Composable
fun GeneratingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "generating_infinite")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated visualizer pulse representing loading/synthesis
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(ElectricPurple.copy(alpha = 0.1f))
                    .border(1.dp, ElectricPurple.copy(alpha = pulseAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonTeal,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.generating_status),
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Gemini is writing the script dialogue and synthesizing distinct voice pitch models for Cosmic Gemini and DJ Nebula. This takes a few seconds.",
                color = MutedText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            LinearProgressIndicator(
                color = NeonTeal,
                trackColor = ObsidianBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun LiveScriptTeleprompter(
    episode: PodcastEpisode,
    isPlaying: Boolean,
    activeDialogueIndex: Int,
    visualizerAmplitude: Float,
    onTogglePlay: () -> Unit,
    onNewRequest: () -> Unit
) {
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

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Smoothly scroll to the active speaking dialogue index
    LaunchedEffect(activeDialogueIndex) {
        if (activeDialogueIndex >= 0) {
            scope.launch {
                listState.animateScrollToItem(activeDialogueIndex)
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Broadcast Title Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
            border = BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x2200F5D4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LIVE AI BROADCAST",
                        color = NeonTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = episode.title,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onTogglePlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color.Red else ElectricPurple
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "STOP" else "VOICE PLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Animated Micro Visualizer during speech
        AnimatedVisibility(visible = isPlaying) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Voice Modulator Stream",
                        fontSize = 11.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )

                    CustomVisualizer(
                        amplitude = visualizerAmplitude,
                        isPlaying = isPlaying,
                        mode = VisualizerMode.BARS,
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp)
                    )
                }
            }
        }

        // Scrollable Script Teleprompter Rows
        Text(
            text = "TELEPROMPTER TRANSCRIPT",
            fontSize = 10.sp,
            color = MutedText,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianSurface)
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(dialogueParts) { index, part ->
                val isSpeaking = activeDialogueIndex == index
                val speakerColor = if (part.speaker == "Cosmic Gemini") ElectricPurple else NeonTeal
                val speakerIcon = if (part.speaker == "Cosmic Gemini") Icons.Rounded.Mic else Icons.Rounded.Headset

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSpeaking) ObsidianCard else Color.Transparent
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSpeaking) speakerColor else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(speakerColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = speakerIcon,
                                contentDescription = null,
                                tint = speakerColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = part.speaker,
                                color = speakerColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = part.text,
                                color = if (isSpeaking) PureWhite else MutedText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = if (isSpeaking) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Generate Another
        OutlinedButton(
            onClick = onNewRequest,
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                text = "Generate Another Topic",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.dp, Color(0xFFEF5350))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFEF5350),
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Broadcast Synthesis Failed",
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                color = MutedText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text(
                    text = "Retry Topic Prompt",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

data class DialoguePart(val speaker: String, val text: String)

