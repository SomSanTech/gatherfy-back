package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity(name = "events")
data class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    var eventId: Long?,
    @Column(name = "event_name")
    var eventName: String,
    @Column(name = "event_desc")
    var eventDesc: String,
    @Column(name = "event_detail")
    var eventDetail: String,
    @Column(name = "event_start_date")
    var eventStartDate: ZonedDateTime,
    @Column(name = "event_end_date")
    var eventEndDate: ZonedDateTime,
    @Column(name = "event_location")
    var eventLocation: String,
    @Column(name = "event_google_map")
    var eventGoogleMap: String,
    @Column(name = "event_capacity")
    var eventCapacity: Long,
    @Column(name = "event_status")
    var eventStatus: String,
    var event_slug: String,
    @Column(name = "event_image")
    var eventImage: String,
    @Column(name = "event_owner")
    var eventOwner: Long,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_tag",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: List<Tag> = mutableListOf()
)
