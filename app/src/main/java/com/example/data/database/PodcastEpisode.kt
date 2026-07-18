package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "podcast_episodes")
data class PodcastEpisode(
    @PrimaryKey val id: String,
    val title: String,
    val hostName: String,
    val topic: String,
    val description: String,
    val duration: String,
    val audioUrl: String,
    val isFavorite: Boolean = false,
    val isUserGenerated: Boolean = false,
    val scriptJson: String = "", // Holds structured conversation JSON
    val timestamp: Long = System.currentTimeMillis()
)
