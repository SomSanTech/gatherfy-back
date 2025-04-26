package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@Entity(name="registrations")
data class Registration(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "registration_id", nullable = false)
    var registrationId: Long = 0,
    @Column(name = "event_id", insertable = false, updatable = false)
    var eventId: Long = 0,
    @Column(name = "user_id", insertable = false, updatable = false)
    var userId: Long = 0,
    @ManyToOne
    @JoinColumn(name = "event_id")
    var event: Event,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,
    var status: String,
    @Column(columnDefinition = "created_at")
    var createdAt: ZonedDateTime= ZonedDateTime.now(),

    @Column(columnDefinition = "regis_date")
    var regisDate: LocalDate
)