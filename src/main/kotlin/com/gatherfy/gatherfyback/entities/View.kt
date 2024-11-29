package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.util.*

@Entity(name = "views")
data class View(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var view_id: Long?,
    var view_date: Date?,
    var view_count: Long?,
    @Column(name = "event_id", insertable = false, updatable = false)
    var eventId: Long = 0,
//    @ManyToOne
//    @JoinColumn(name = "event_id")
//    var event: Event,
)
