package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.EventTag
import com.gatherfy.gatherfyback.entities.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TagRepository: JpaRepository<Tag, Long> {
    @Query("SELECT t FROM tags t  WHERE t.tag_id IN :tags")
    fun findAllById(@Param("tags") tags: List<Long>): List<Tag>
}