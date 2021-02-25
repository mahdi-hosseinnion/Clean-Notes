package com.example.clean_notes.business.interactors.common

import com.example.clean_notes.business.data.cache.CacheResponseHandler
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.data.util.safeApiCall
import com.example.clean_notes.business.data.util.safeCacheCall
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.state.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall {
            noteCacheDataSource.deleteNote(note.id)
        }

        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState>? {
                var message = DELETE_NOTE_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None
                var messageType: MessageType = MessageType.Success
                if (resultObj <= 0) {
                    message = DELETE_NOTE_FAILED
                    uiComponentType = UIComponentType.Toast
                    messageType = MessageType.Error
                }
                return DataState.data(
                    response = Response(
                        message,
                        uiComponentType,
                        messageType
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)

        if (response?.stateMessage?.response?.messageType != MessageType.Error) {
            updateNetwork(note)
        }
    }

    private suspend fun updateNetwork(note: Note) {
        // delete from 'notes' node
        safeApiCall {
            noteNetworkDataSource.deleteNote(note.id)
        }

        // insert into 'deletes' node
        safeApiCall {
            noteNetworkDataSource.insertDeletedNote(note)
        }

    }

    companion object {
        val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        val DELETE_NOTE_PENDING = "Delete pending..."
        val DELETE_NOTE_FAILED = "Failed to delete note."
        val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"
    }
}