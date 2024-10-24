package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Event
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EventRepository: JpaRepository<Event, Long> {
    @Query("from events where event_slug = :slug")
    fun findEventBySlug(@Param("slug") slug: String) : Event

    @Query("from events where eventName LIKE %:keyword%")
    fun findEventByKeyword(@Param("keyword") keyword: String , sort : Sort) : List<Event>
}