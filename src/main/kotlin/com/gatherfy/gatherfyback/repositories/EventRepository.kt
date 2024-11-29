package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface EventRepository: JpaRepository<Event, Long> {
    @Query("from events where event_slug = :slug")
    fun findEventBySlug(@Param("slug") slug: String) : Event

    @Query("from events where event_id = :id")
    fun findEventByEventId(@Param("id") id: Long) : Event

    @Query("from events where event_name LIKE %:keyword%")
    fun findEventByKeyword(@Param("keyword") keyword: String) : List<Event>

    @Query("select e from events e join event_tag et on et.event.event_id=e.event_id join tags t on et.tag.tag_id=t.tag_id where t.tag_title in :tags")
    fun findEventsByTags(@Param("tags") tags: List<String>) : List<Event>

    @Query("from events where :date between date(event_start_date) and date(event_end_date)")
    fun findEventsByDate(@Param("date") date: LocalDate) : List<Event>

    @Query("select e from events e join event_tag et on et.event.event_id=e.event_id join tags t on et.tag.tag_id=t.tag_id where e.event_name LIKE %:keyword%  AND t.tag_title in :tags")
    fun findEventsByKeywordAndTags(@Param("keyword") keyword: String, @Param("tags") tags: List<String>): List<Event>

    @Query("select e from events e where e.event_name LIKE %:keyword% and :date between date(e.event_start_date) and date(e.event_end_date)")
    fun findEventsByKeywordAndDate(@Param("keyword") keyword: String, @Param("date") date: LocalDate): List<Event>

    @Query("select e from events e join event_tag et on et.event.event_id=e.event_id join tags t on et.tag.tag_id=t.tag_id where t.tag_title in :tags and :date between date(e.event_start_date) and date(e.event_end_date)")
    fun findEventsByTagsAndDate(@Param("tags") tags: List<String>, @Param("date") date: LocalDate): List<Event>

    @Query("select e from events e join event_tag et on et.event.event_id=e.event_id join tags t on et.tag.tag_id=t.tag_id where e.event_name LIKE %:keyword% and t.tag_title in :tags and :date between date(e.event_start_date) and date(e.event_end_date)")
    fun findEventsByKeywordAndTagsAndDate(@Param("keyword") keyword: String, @Param("tags") tags: List<String>, @Param("date") date: LocalDate): List<Event>

    @Query("from events where event_owner = :ownerId")
    fun findEventsByEventOwner(@Param("ownerId") ownerId: Long?) : List<Event>
}