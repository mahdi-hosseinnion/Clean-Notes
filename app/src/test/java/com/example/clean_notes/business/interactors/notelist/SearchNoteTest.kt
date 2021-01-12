package com.example.clean_notes.business.interactors.notelist

import com.example.clean_notes.business.data.cache.CacheErrors
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.FORCE_SEARCH_NOTES_EXCEPTION
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.state.MessageType
import com.example.clean_notes.business.interaction.notelist.SearchNotes
import com.example.clean_notes.di.DependencyContainer
import com.example.clean_notes.framework.datasource.database.ORDER_BY_ASC_DATE_UPDATED
import com.example.clean_notes.framework.presentation.notelist.state.NoteListStateEvent
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. blankQuery_success_confirmNotesRetrieved()
    a) query with some default search options
    b) listen for SEARCH_NOTES_SUCCESS emitted from flow
    c) confirm notes were retrieved
    d) confirm notes in cache match with notes that were retrieved
2. randomQuery_success_confirmNoResults()
    a) query with something that will yield no results
    b) listen for SEARCH_NOTES_NO_MATCHING_RESULTS emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
3. searchNotes_fail_confirmNoResults()
    a) force an exception to be thrown
    b) listen for CACHE_ERROR_UNKNOWN emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is notes in the cache
 */
@InternalCoroutinesApi

class SearchNoteTest {
    //system under test
    val searchNotes: SearchNotes

    val dependencyContainer = DependencyContainer()

    val noteCacheDataSource: NoteCacheDataSource
    init {
        dependencyContainer.build()

        noteCacheDataSource = dependencyContainer.noteCacheDataSource

        searchNotes = SearchNotes(noteCacheDataSource = noteCacheDataSource)
    }


    @Test
    fun blankQuery_success_confirmNotesRetrieved() = runBlocking {
        val query = ""
        var results: ArrayList<Note>? = null

        searchNotes.searchNote(
            query,
            ORDER_BY_ASC_DATE_UPDATED,
            1, NoteListStateEvent.SearchNotesEvent()
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.messageType,
                MessageType.Success
            )
            it?.data?.noteList?.let {
                results = ArrayList(it)

            }
        }
        assertTrue { results != null && results?.isNotEmpty() ?: false }

        // confirm notes in cache match with notes that were retrieved
        val notesInCache = noteCacheDataSource.searchNotes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { results?.containsAll(notesInCache) ?: false }
    }

    @Test
    fun randomQuery_success_confirmNoResults() = runBlocking {
        val query = "asdfsadf3242h342sdaljfhs3223423"
        var results: ArrayList<Note>? = null

        searchNotes.searchNote(
            query,
            ORDER_BY_ASC_DATE_UPDATED,
            1, NoteListStateEvent.SearchNotesEvent()
        ).collect {
            assertEquals(
                it?.stateMessage?.response?.messageType,
                MessageType.Success
            )
            it?.data?.noteList?.let {
                results = ArrayList(it)

            }
        }

        assertTrue(results?.run { size == 0 } ?: false)

        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { notesInCache.isNotEmpty() }
    }

    @Test
    fun searchNotes_fail_confirmNoResults() = runBlocking {

        val query = FORCE_SEARCH_NOTES_EXCEPTION
        var results: ArrayList<Note>? = null

        searchNotes.searchNote(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = NoteListStateEvent.SearchNotesEvent()
        ).collect { value ->
            assert(
                value?.stateMessage?.response?.message
                    ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
            )
            value?.data?.noteList?.let { list ->
                results = ArrayList(list)
            }
            println("results: ${results}")
        }


        // confirm nothing was retrieved
        assertTrue { results?.run { size == 0 } ?: true }

        // confirm there is notes in the cache
        val notesInCache = noteCacheDataSource.searchNotes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { notesInCache.isNotEmpty() }
    }
}