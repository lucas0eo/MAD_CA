package com.example.myapplication.data.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey



@Entity(
    tableName = "score",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
)
data class ScoreEntity (
    @PrimaryKey(autoGenerate = true) val Id: Long = 0,
    val userId: Long,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)