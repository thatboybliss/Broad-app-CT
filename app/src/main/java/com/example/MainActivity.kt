package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AmbientDynamicBackground
import com.example.ui.components.PodcastPlayerOverlay
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.PodcastViewModel

enum class AppState {
    ONBOARDING, SIGN_IN, MAIN_APP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var appState by remember { mutableStateOf(AppState.ONBOARDING) }
                
                when (appState) {
                    AppState.ONBOARDING -> com.example.ui.screens.OnboardingScreen(onFinish = { appState = AppState.SIGN_IN })
                    AppState.SIGN_IN -> com.example.ui.screens.SignInScreen(onSignIn = { appState = AppState.MAIN_APP })
                    AppState.MAIN_APP -> MainAppLayout()
                }
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val viewModel: PodcastViewModel = viewModel()

    // ViewModel State bindings
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val currentEpisode by viewModel.currentEpisode.collectAsState()
    val activeDialogueIndex by viewModel.activeDialogueIndex.collectAsState()
    val visualizerAmplitude by viewModel.visualizerAmplitude.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    
    val allEpisodes by viewModel.allEpisodes.collectAsState()
    val favoriteEpisodes by viewModel.favoriteEpisodes.collectAsState()

    var currentTab by remember { mutableStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0F))
    ) {
        val isWideScreen = maxWidth >= 600.dp

        // 1. Hardware Accelerated Dynamic Ambient Background (Vibrant Glow translation)
        AmbientDynamicBackground(
            isPlaying = isPlaying,
            amplitude = visualizerAmplitude,
            modifier = Modifier.fillMaxSize()
        )

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                // --- Left Web-style Navigation Rail ---
                NavigationRail(
                    containerColor = ObsidianSurface.copy(alpha = 0.85f),
                    header = {
                        Spacer(modifier = Modifier.height(24.dp))
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Logo",
                            tint = NeonTeal,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Studio Hub",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = stringResource(id = R.string.tab_discover))
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = PureWhite,
                            unselectedIconColor = Color(0xFF9E9EAF),
                            unselectedTextColor = Color(0xFF9E9EAF),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("discover_rail_tab")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationRailItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = stringResource(id = R.string.tab_studio))
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = PureWhite,
                            unselectedIconColor = Color(0xFF9E9EAF),
                            unselectedTextColor = Color(0xFF9E9EAF),
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("studio_rail_tab")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationRailItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = stringResource(id = R.string.tab_favorites))
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color.Red,
                            selectedTextColor = PureWhite,
                            unselectedIconColor = Color(0xFF9E9EAF),
                            unselectedTextColor = Color(0xFF9E9EAF),
                            indicatorColor = Color.Red.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("favorites_rail_tab")
                    )
                }
            }

            // --- Translucent Core Scaffold ---
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent, // Transparent allows background flow
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = ObsidianSurface.copy(alpha = 0.85f),
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .background(Color.Transparent)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = R.string.tab_discover))
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = PureWhite,
                                    unselectedIconColor = Color(0xFF9E9EAF),
                                    unselectedTextColor = Color(0xFF9E9EAF),
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("discover_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = R.string.tab_studio))
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                                    selectedTextColor = PureWhite,
                                    unselectedIconColor = Color(0xFF9E9EAF),
                                    unselectedTextColor = Color(0xFF9E9EAF),
                                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("studio_tab")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(text = stringResource(id = R.string.tab_favorites))
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Red,
                                    selectedTextColor = PureWhite,
                                    unselectedIconColor = Color(0xFF9E9EAF),
                                    unselectedTextColor = Color(0xFF9E9EAF),
                                    indicatorColor = Color.Red.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("favorites_tab")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Tab Navigation Swapper
                    when (currentTab) {
                        0 -> DiscoverScreen(
                            episodes = allEpisodes,
                            currentEpisode = currentEpisode,
                            isPlaying = isPlaying,
                            onEpisodeSelect = { viewModel.selectEpisode(it) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                        1 -> StudioScreen(
                            generationState = generationState,
                            isPlaying = isPlaying,
                            currentEpisode = currentEpisode,
                            activeDialogueIndex = activeDialogueIndex,
                            visualizerAmplitude = visualizerAmplitude,
                            onGenerate = { viewModel.generateNewEpisode(it) },
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onClearState = { viewModel.clearGenerationState() }
                        )
                        2 -> FavoritesScreen(
                            favoriteEpisodes = favoriteEpisodes,
                            currentEpisode = currentEpisode,
                            isPlaying = isPlaying,
                            onEpisodeSelect = { viewModel.selectEpisode(it) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }

        // 3. Sliding / Expanding Player Sheet Overlay (mounted on top of scaffold layers)
        PodcastPlayerOverlay(
            episode = currentEpisode,
            isPlaying = isPlaying,
            currentTime = currentTime,
            totalDuration = totalDuration,
            playbackSpeed = playbackSpeed,
            activeDialogueIndex = activeDialogueIndex,
            visualizerAmplitude = visualizerAmplitude,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onChangeSpeed = { viewModel.changePlaybackSpeed() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDismiss = { viewModel.stopPlayback() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isWideScreen) 16.dp else 80.dp) // Lift slightly higher on mobile to clear bottom tab bar, otherwise place elegant floating player
        )
    }
}
