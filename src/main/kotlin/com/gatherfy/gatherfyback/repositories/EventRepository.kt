package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventRepository: JpaRepository<Event, Long> {
    @Query("from events where event_slug = :slug")
    fun findEventBySlug(@Param("slug") slug: String) : Event

    @Query("from events where event_name LIKE %:keyword%")
    fun findEventByKeyword(@Param("keyword") keyword: String) : List<Event>

    @Query("select e from events e join event_tag et on et.event.event_id=e.event_id join tags t on et.tag.tag_id=t.tag_id where t.tag_title in :tags")
    fun findEventsByTags(@Param("tags") tags: List<String>) : List<Event>
}