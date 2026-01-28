package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.entity.ScoreEntity

data class LeaderboardItem(
    val username: String,
    val score: Int
)@Dao
interface ScoreDAO {
    @Query("SELECT * FROM score WHERE userId = :userId ORDER BY score DESC")
    suspend fun getHistoryForUser(userId: Long): List<ScoreEntity>

    @Insert
    suspend fun insertScore(score: ScoreEntity)

        @Query("""
        SELECT users.username, score.score 
        FROM score 
        INNER JOIN users ON score.userId = users.userId 
        ORDER BY score.score DESC
    """)
        fun getLeaderboard(): kotlinx.coroutines.flow.Flow<List<LeaderboardItem>>
}
