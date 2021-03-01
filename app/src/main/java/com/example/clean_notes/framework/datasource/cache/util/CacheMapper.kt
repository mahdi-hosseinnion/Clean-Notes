package com.example.clean_notes.framework.datasource.cache.util

import com.example.clean_notes.business.domain.model.Note
import com.example.clean_notes.business.domain.util.EntityMapper
import com.example.clean_notes.framework.datasource.cache.model.NoteCacheEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheMapper
@Inject
constructor() : EntityMapper<NoteCacheEntity, Note> {

    override fun mapFromEntity(entity: NoteCacheEntity): Note = Note(
        id = entity.id,
        title = entity.title,
        body = entity.body,
        updated_at = entity.updated_at,
        created_at = entity.created_at
    )

    override fun mapToEntity(domainModel: Note): NoteCacheEntity = NoteCacheEntity(
        id = domainModel.id,
        title = domainModel.title,
        body = domainModel.body,
        updated_at = domainModel.updated_at,
        created_at = domainModel.created_at
    )

    fun entityListToNoteList(entities: List<NoteCacheEntity>): List<Note> =
        entities.map { mapFromEntity(it) }

    fun noteListToEntityList(notes: List<Note>): List<NoteCacheEntity> =
        notes.map { mapToEntity(it) }

}