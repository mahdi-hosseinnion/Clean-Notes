package com.example.clean_notes.business.data.cache.abstraction

import com.example.clean_notes.business.domain.model.Note

interface NoteCacheDataSource{

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String?): Int

    suspend fun getAllNotes(): List<Note>

    suspend fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>

    suspend fun searchNoteById(id: String): Note?

    suspend fun getNumNotes(): Int

    suspend fun insertNotes(notes: List<Note>): LongArray
}