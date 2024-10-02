package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "events")
data class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var event_id: Long?,
    var event_name: String,
    var event_date: String,
    var event_location: String,
    var event_description: String,
    var event_slug: String,
    var event_image: String
)
