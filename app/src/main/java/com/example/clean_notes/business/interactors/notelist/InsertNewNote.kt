package com.example.clean_notes.business.interactors.notelist

import com.example.clean_notes.business.data.cache.CacheResponseHandler
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.data.util.safeApiCall
import com.example.clean_notes.business.data.util.safeCacheCall
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.model.NoteFactory
import com.example.clean_notes.business.domain.state.*
import com.example.clean_notes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertNewNote
@Inject
constructor(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory
) {

    fun insertNote(
        id: String? = null,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val newNote = noteFactory.createSingleNote(
            id = id,
            title = title,
            body = ""
        )

        val cacheResult = safeCacheCall {
            noteCacheDataSource.insertNote(newNote)
        }

        val cacheResponse = object : CacheResponseHandler<NoteListViewState, Long>(
            cacheResult, stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {
                return if (resultObj > 0) {
                    val viewState = NoteListViewState(newNote = newNote)

                    DataState.data(
                        response = Response(
                            message = INSERT_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.error(
                        response = Response(
                            message = INSERT_NOTE_FAILURE,
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Error
                        ),
                        stateEvent = stateEvent
                    )
                }
            }

        }.getResult()

        emit(cacheResponse)
        updateNetwork(cacheResponse?.stateMessage?.response?.messageType, newNote)
    }

    private suspend fun updateNetwork(messageType: MessageType?, newNote: Note) {
        if (messageType == MessageType.Success) {
            safeApiCall {
                noteNetworkDataSource.insertOrUpdateNote(newNote)
            }
        }
        //we don,t care about network failure here
    }

    companion object {
        const val INSERT_NOTE_SUCCESS = "Successfully inserted new note."
        const val INSERT_NOTE_FAILURE = "Failed to insert new note."
    }


}