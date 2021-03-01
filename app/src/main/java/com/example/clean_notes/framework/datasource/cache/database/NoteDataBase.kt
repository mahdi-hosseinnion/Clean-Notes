package com.example.clean_notes.framework.datasource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.clean_notes.framework.datasource.cache.model.NoteCacheEntity

@Database(entities = [NoteCacheEntity::class ], version = 1)
abstract class NoteDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object{
        val DATABASE_NAME: String = "note_db"
    }


}