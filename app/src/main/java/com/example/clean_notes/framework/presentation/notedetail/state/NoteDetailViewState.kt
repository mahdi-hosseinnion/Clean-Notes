package com.example.clean_notes.framework.presentation.notedetail.state


import android.os.Parcelable
import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoteDetailViewState(

    var note: Note? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState

