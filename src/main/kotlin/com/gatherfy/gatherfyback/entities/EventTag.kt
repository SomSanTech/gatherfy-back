package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "event_tag")
@IdClass(EventTagId::class)
data class EventTag(
    @Id
    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    var event: Event,
    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id", nullable = false)
    var tag: Tag
)
