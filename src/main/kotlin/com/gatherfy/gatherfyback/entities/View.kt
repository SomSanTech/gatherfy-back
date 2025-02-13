package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity(name = "views")
data class View(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    var viewId: Long? = null,
    @Column(name = "view_date")
    var viewDate: LocalDate,
    @Column(name = "view_count")
    var viewCount: Long,
    @Column(name = "event_id")
    var eventId: Long? = null,
)
