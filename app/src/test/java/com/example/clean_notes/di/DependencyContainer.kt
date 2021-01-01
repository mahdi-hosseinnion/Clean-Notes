package com.example.clean_notes.di

import com.example.clean_notes.business.data.cache.FakeNoteNetworkDataSourceImpl
import com.example.clean_notes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.clean_notes.business.data.network.FakeNoteCacheDataSourceImpl
import com.example.clean_notes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.clean_notes.business.domain.model.NoteFactory
import com.example.clean_notes.business.domain.util.DateUtil
import com.example.clean_notes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


public class DependencyContainer {

    private var dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

    private val dateUtil = DateUtil(dateFormat)

    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory

    init {
        isUnitTest = true
    }

    public fun build() {
        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = HashMap(),
            deletedNotesData = HashMap()
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = HashMap(),
            dateUtil = dateUtil
        )
    }
}