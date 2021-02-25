package com.example.clean_notes.business.interactors.splash

import com.example.clean_notes.business.data.cache.CacheResponseHandler
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.ApiResponseHandler
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.data.util.safeApiCall
import com.example.clean_notes.business.data.util.safeCacheCall
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.state.DataState
import com.example.clean_notes.util.printLogD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
    Query all notes in the cache. It will then search firestore for
    each corresponding note but with an extra filter: It will only return notes where
    cached_note.updated_at < network_note.updated_at. It will update the cached notes
    where that condition is met. If the note does not exist in Firestore (maybe due to
    network being down at time of insertion), insert it
    (**This must be done AFTER
    checking for deleted notes and performing that sync**).
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
class SyncNotes(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
){

    suspend fun syncNotes() {

        val cachedNotesList = getCachedNotes()

        syncNetworkNotesWithCachedNotes(ArrayList(cachedNotesList))
    }

    private suspend fun getCachedNotes(): List<Note> {
        val cacheResult = safeCacheCall(Dispatchers.IO){
            noteCacheDataSource.getAllNotes()
        }

        val response = object: CacheResponseHandler<List<Note>, List<Note>>(
            response = cacheResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }

        }.getResult()

        return response?.data ?: ArrayList()
    }

    // get all notes from network
    // if they do not exist in cache, insert them
    // if they do exist in cache, make sure they are up to date
    // while looping, remove notes from the cachedNotes list. If any remain, it means they
    // should be in the network but aren't. So insert them.
    private suspend fun syncNetworkNotesWithCachedNotes(
        cachedNotes: ArrayList<Note>
    ) = withContext(Dispatchers.IO){

        val networkResult = safeApiCall(Dispatchers.IO){
            noteNetworkDataSource.getAllNotes()
        }

        val response = object: ApiResponseHandler<List<Note>, List<Note>>(
            response = networkResult,
            stateEvent = null
        ){
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<List<Note>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

        val noteList = response?.data ?: ArrayList()

        val job = launch {
            for(note in noteList){
                noteCacheDataSource.searchNoteById(note.id)?.let { cachedNote ->
                    cachedNotes.remove(cachedNote)
                    checkIfCachedNoteRequiresUpdate(cachedNote, note)
                }?: noteCacheDataSource.insertNote(note)
            }
        }
        job.join()

        // insert remaining into network
        for(cachedNote in cachedNotes){
            noteNetworkDataSource.insertOrUpdateNote(cachedNote)
        }
    }

    private suspend fun checkIfCachedNoteRequiresUpdate(
        cachedNote: Note,
        networkNote: Note
    ){
        val cacheUpdatedAt = cachedNote.updated_at
        val networkUpdatedAt = networkNote.updated_at

        // update cache (network has newest data)
        if(networkUpdatedAt > cacheUpdatedAt){
            printLogD("SyncNotes",
                "cacheUpdatedAt: ${cacheUpdatedAt}, " +
                        "networkUpdatedAt: ${networkUpdatedAt}, " +
                        "note: ${cachedNote.title}")
            safeCacheCall(Dispatchers.IO){
                noteCacheDataSource.updateNote(
                    networkNote.id,
                    networkNote.title,
                    networkNote.body
                )
            }
        }
        // update network (cache has newest data)
        else{
            safeApiCall(Dispatchers.IO){
                noteNetworkDataSource.insertOrUpdateNote(cachedNote)
            }
        }
    }

    // for debugging
//    private fun printCacheLongTimestamps(notes: List<Note>){
//        for(note in notes){
//            printLogD("SyncNotes",
//                "date: ${dateUtil.convertServerStringDateToLong(note.updated_at)}")
//        }
//    }

}
