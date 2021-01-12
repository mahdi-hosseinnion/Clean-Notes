package com.example.clean_notes.business.interaction.notelist

import com.example.clean_notes.business.data.cache.CacheResponseHandler
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.util.safeCacheCall
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.state.*
import com.example.clean_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun searchNote(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {
        var updatedPage = page
        if (page < 1) {
            updatedPage = 1
        }

        val cacheResult = safeCacheCall {
            noteCacheDataSource.searchNotes(query, filterAndOrder, updatedPage)
        }

        val response = object : CacheResponseHandler<NoteListViewState, List<Note>>(
            cacheResult,
            stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState>? {
                var message = SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None
                if (resultObj.isEmpty()) {
                    message = SEARCH_NOTES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast
                }
                return DataState.data(
                    response = Response(
                        message,
                        uiComponentType,
                        MessageType.Success
                    ),
                    data = NoteListViewState(noteList = ArrayList(resultObj)),
                    stateEvent = stateEvent
                )
            }
        }.getResult()
        emit(response)
    }

    companion object {
        val SEARCH_NOTES_SUCCESS = "Successfully retrieved list of notes."
        val SEARCH_NOTES_NO_MATCHING_RESULTS = "There are no notes that match that query."
        val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes."

    }
}