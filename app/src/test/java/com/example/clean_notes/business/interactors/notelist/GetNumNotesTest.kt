package com.example.clean_notes.business.interactors.notelist

import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.domain.model.NoteFactory
import com.example.clean_notes.business.domain.state.DataState
import com.example.clean_notes.business.interaction.notelist.GetNumNotes
import com.example.clean_notes.business.interaction.notelist.GetNumNotes.Companion.GET_NUM_NOTES_SUCCESS
import com.example.clean_notes.di.DependencyContainer
import com.example.clean_notes.framework.presentation.notelist.state.NoteListStateEvent
import com.example.clean_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. getNumNotes_success_confirmCorrect()
    a) get the number of notes in cache
    b) listen for GET_NUM_NOTES_SUCCESS from flow emission
    c) compare with the number of notes in the fake data set
*/
@InternalCoroutinesApi
class GetNumNotesTest {

    // system in test
    private val getNumNotes: GetNumNotes

    // dependencies
    private val dependencyContainer: DependencyContainer = DependencyContainer()
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory
        getNumNotes = GetNumNotes(
            noteCacheDataSource = noteCacheDataSource
        )
    }


    @Test
    fun getNumNotes_success_confirmCorrect() = runBlocking {

        var numNotes = 0
        getNumNotes.getNumNotes(
            stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    GET_NUM_NOTES_SUCCESS
                )
                numNotes = value?.data?.numNotesInCache?: 0
            }
        })

        val actualNumNotesInCache = noteCacheDataSource.getNumNotes()
        assertTrue { actualNumNotesInCache == numNotes }
    }


}