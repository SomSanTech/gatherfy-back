package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "event_tag")
@IdClass(EventTagId::class)
data class EventTag(
    @Id
    @ManyToOne
    @JoinColumn(name = "event_id")
    var event: Event,
    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    var tag: Tag
)
