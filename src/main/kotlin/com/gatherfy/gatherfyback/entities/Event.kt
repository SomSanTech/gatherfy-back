package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity(name = "events")
data class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var event_id: Long?,
    var event_name: String,
    var event_desc: String,
    var event_detail: String,
    var event_start_date: ZonedDateTime,
    var event_end_date: ZonedDateTime,
    var event_location: String,
    var event_google_map: String,
    var event_capacity: Long,
    var event_status: String,
    var event_slug: String,
    var event_image: String,
    var event_owner: Long,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_tag",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: List<Tag> = mutableListOf()
)
