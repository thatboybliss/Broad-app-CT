package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.*
import com.example.ui.theme.GlowPurpleTranslucent
import com.example.ui.theme.GlowTealTranslucent
import com.example.ui.theme.ObsidianBg
import kotlin.math.sin

@Composable
fun AmbientDynamicBackground(
    isPlaying: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    // 1. Setup infinite transition for smooth background translations
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_bg_transition")
    
    // Smooth angle progression
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing offset for scale pulsing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Smooth amplitude transition to avoid visual jerks
    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isPlaying) amplitude else 0.05f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "amplitude"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 3f // Offset upward slightly to frame the player disk beautifully

            // --- LAYER 1: Electric Purple Radiant Bloom ---
            // Shifting position on a circle
            val purpleRad = rotationAngle * (Math.PI / 180f)
            val purpleOffset = Offset(
                x = centerX + (180.dp.toPx() * sin(purpleRad).toFloat() * (if (isPlaying) 1.2f else 1.0f)),
                y = centerY + (100.dp.toPx() * sin(purpleRad * 2f).toFloat())
            )
            val purpleRadius = (width * 0.75f) * pulseScale * (1f + animatedAmplitude * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlowPurpleTranslucent,
                        Color.Transparent
                    ),
                    center = purpleOffset,
                    radius = purpleRadius
                ),
                center = purpleOffset,
                radius = purpleRadius
            )

            // --- LAYER 2: Neon Teal Radiant Bloom ---
            val tealRad = (rotationAngle + 120f) * (Math.PI / 180f)
            val tealOffset = Offset(
                x = centerX + (200.dp.toPx() * sin(tealRad * 1.5f).toFloat()),
                y = (centerY * 2f) + (150.dp.toPx() * sin(tealRad).toFloat() * (if (isPlaying) 1.2f else 1.0f))
            )
            val tealRadius = (width * 0.6f) * pulseScale * (1f + animatedAmplitude * 0.4f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlowTealTranslucent,
                        Color.Transparent
                    ),
                    center = tealOffset,
                    radius = tealRadius
                ),
                center = tealOffset,
                radius = tealRadius
            )

            // --- LAYER 3: Ambient Wave Frequencies ---
            // We draw 2 elegant sine waves flowing across the bottom
            if (isPlaying) {
                val wavePath1 = Path()
                val wavePath2 = Path()
                
                wavePath1.moveTo(0f, height * 0.85f)
                wavePath2.moveTo(0f, height * 0.88f)
                
                val wavePhase = rotationAngle * 3f // Speed of flow
                val numPoints = 120
                for (i in 0..numPoints) {
                    val x = (i / numPoints.toFloat()) * width
                    // High-quality composite sine waves
                    val y1 = (height * 0.85f) + 
                             sin((i * 0.05f) + wavePhase * (Math.PI / 180f)).toFloat() * 12.dp.toPx() * (0.3f + animatedAmplitude * 0.8f) + 
                             sin((i * 0.12f) - wavePhase * 0.5f * (Math.PI / 180f)).toFloat() * 6.dp.toPx()
                    
                    val y2 = (height * 0.88f) + 
                             sin((i * 0.04f) - (wavePhase + 45f) * (Math.PI / 180f)).toFloat() * 16.dp.toPx() * (0.2f + animatedAmplitude * 0.9f) + 
                             sin((i * 0.08f) + wavePhase * 0.3f * (Math.PI / 180f)).toFloat() * 4.dp.toPx()
                    
                    wavePath1.lineTo(x, y1)
                    wavePath2.lineTo(x, y2)
                }

                drawPath(
                    path = wavePath1,
                    color = Color(0x338B5CF6),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawPath(
                    path = wavePath2,
                    color = Color(0x2200F5D4),
                    style = Stroke(width = 2.0.dp.toPx())
                )
            }
        }
    }
}
