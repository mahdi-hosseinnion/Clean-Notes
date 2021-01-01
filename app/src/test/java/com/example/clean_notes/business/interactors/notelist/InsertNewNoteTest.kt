package com.example.clean_notes.business.interactors.notelist


import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.domain.model.NoteFactory
import com.example.clean_notes.business.interaction.notelist.InsertNewNote
import com.example.clean_notes.di.DependencyContainer
class InsertNewNoteTest {

    // system in test
    private val insertNewNote: InsertNewNote

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }
}