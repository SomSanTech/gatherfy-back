package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "attendees")
data class Attendee(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var att_id: Long,
    var att_firstname: String,
    var att_lastname: String,
    var att_gender: String,
    var att_email: String,
    var att_phone: String,
)
