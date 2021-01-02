package com.example.clean_notes.business.interactors.notelist


import com.example.clean_notes.business.data.cache.CacheErrors
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.FORCE_GENERAL_FAILURE
import com.example.clean_notes.business.data.network.FORCE_NEW_NOTE_EXCEPTION
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.model.NoteFactory
import com.example.clean_notes.business.domain.state.DataState
import com.example.clean_notes.business.domain.state.MessageType
import com.example.clean_notes.business.interaction.notelist.InsertNewNote
import com.example.clean_notes.di.DependencyContainer
import com.example.clean_notes.framework.presentation.notelist.state.NoteListStateEvent
import com.example.clean_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/*
Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */

@InternalCoroutinesApi
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

    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val note = noteFactory.createSingleNote(title = UUID.randomUUID().toString())
        insertNewNote.insertNote(
            id = note.id,
            title = note.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(
                UUID.randomUUID()
                    .toString()
            )
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    MessageType.Success,
                    value?.stateMessage?.response?.messageType
                )
            }

        })
        //confirm network was updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(note)
        assertEquals(note, networkNoteThatWasInserted)

        //confirm cache was updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(note.id)
        assertEquals(note, cacheNoteThatWasInserted)
    }

    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val note = noteFactory.createSingleNote(
            id = FORCE_NEW_NOTE_EXCEPTION,
            title = "fake"
        )

        insertNewNote.insertNote(
            id = note.id,
            title = note.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(UUID.randomUUID().toString())
        ).collect {
            assertEquals(MessageType.Error, it?.stateMessage?.response?.messageType)
        }
        //confirm network wasn't updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(note)
        assertNull(networkNoteThatWasInserted)

        //confirm cache wasn't updated
        val cacheNoteThatWasInserted = noteNetworkDataSource.searchNote(note)
        assertNull(cacheNoteThatWasInserted)
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val note = noteFactory.createSingleNote(
            id = FORCE_GENERAL_FAILURE,
            title = "fake"
        )

        insertNewNote.insertNote(
            id = note.id,
            title = note.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(UUID.randomUUID().toString())
        ).collect {
            assertEquals(MessageType.Error, it?.stateMessage?.response?.messageType)
            assertEquals(InsertNewNote.INSERT_NOTE_FAILURE, it?.stateMessage?.response?.message)
        }
        //confirm network wasn't updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(note)
        assertNull(networkNoteThatWasInserted)

        //confirm cache wasn't updated
        val cacheNoteThatWasInserted = noteNetworkDataSource.searchNote(note)
        assertNull(cacheNoteThatWasInserted)
    }
}






















