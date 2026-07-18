package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PodcastDao {
    @Query("SELECT * FROM podcast_episodes ORDER BY timestamp DESC")
    fun getAllEpisodes(): Flow<List<PodcastEpisode>>

    @Query("SELECT * FROM podcast_episodes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteEpisodes(): Flow<List<PodcastEpisode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: PodcastEpisode)

    @Query("UPDATE podcast_episodes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Delete
    suspend fun deleteEpisode(episode: PodcastEpisode)

    @Query("SELECT * FROM podcast_episodes WHERE id = :id LIMIT 1")
    suspend fun getEpisodeById(id: String): PodcastEpisode?
}
