package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonTeal
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerMode {
    BARS, CIRCULAR
}

@Composable
fun CustomVisualizer(
    amplitude: Float,
    isPlaying: Boolean,
    mode: VisualizerMode,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_infinite")
    
    // Animate individual bands slightly differently to simulate real frequency bins
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frequency_offset"
    )

    // Base scale state
    val scaleMultiplier by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.05f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        when (mode) {
            VisualizerMode.BARS -> {
                // Draw 16 beautiful rounded vertical equalizer bars
                val barCount = 16
                val spacing = 4.dp.toPx()
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = (width - totalSpacing) / barCount

                for (i in 0 until barCount) {
                    // Generate a beautiful organic wave heights based on sine-cohesion + current amplitude
                    val frequencyModifier = sin((i * 0.4f) + waveOffset).toFloat()
                    val randomJitter = (0.7f + 0.3f * sin((i * 1.3f) - waveOffset * 1.5f).toFloat())
                    
                    // Combine into peak height
                    val peakFraction = ((amplitude * 0.8f) + (frequencyModifier * 0.15f + 0.15f)) * scaleMultiplier * randomJitter
                    val clampedFraction = peakFraction.coerceIn(0.02f, 1.0f)
                    val barHeight = height * clampedFraction

                    val left = i * (barWidth + spacing)
                    val top = height - barHeight

                    // Premium gradient reflecting Electric Purple near base and Neon Teal near peak
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            NeonTeal,
                            ElectricPurple
                        ),
                        startY = top,
                        endY = height
                    )

                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
            VisualizerMode.CIRCULAR -> {
                // Radial visualizer surrounding the spinning player CD disc
                val centerX = width / 2f
                val centerY = height / 2f
                val baseRadius = (width.coerceAtMost(height) / 2.3f) // Leave room for outward bars
                val spikeCount = 60

                for (i in 0 until spikeCount) {
                    val angleDeg = (360f / spikeCount) * i
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    
                    // Wave simulation for radial spikes
                    val waveValue = sin((i * 0.25f) + waveOffset).toFloat()
                    val jitter = (0.8f + 0.4f * cos((i * 0.8f) - waveOffset * 2f).toFloat())
                    val spikeLengthFraction = ((amplitude * 0.7f) + (waveValue * 0.15f + 0.15f)) * scaleMultiplier * jitter
                    val spikeLength = (40.dp.toPx() * spikeLengthFraction).coerceAtLeast(3.dp.toPx())

                    // Spikes start at baseRadius and go outward
                    val startX = centerX + (baseRadius * cos(angleRad)).toFloat()
                    val startY = centerY + (baseRadius * sin(angleRad)).toFloat()

                    val endX = centerX + ((baseRadius + spikeLength) * cos(angleRad)).toFloat()
                    val endY = centerY + ((baseRadius + spikeLength) * sin(angleRad)).toFloat()

                    // Alternating colors or multi-color glow based on angle
                    val color = if (i % 2 == 0) NeonTeal else ElectricPurple

                    drawLine(
                        color = color,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
