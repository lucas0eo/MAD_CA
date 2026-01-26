package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.dao.ScoreDAO
import com.example.myapplication.data.dao.UserDAO
import com.example.myapplication.data.entity.ScoreEntity
import com.example.myapplication.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ScoreEntity::class,
    ],
    version = 3,
    exportSchema = true
)
abstract  class AppDatabase: RoomDatabase(){

    abstract fun userDao(): UserDAO
    abstract fun scoreDao(): ScoreDAO
    companion object{
        @Volatile private  var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mad_ca.db"
                ).build().also { INSTANCE=it }
            }
    }
}