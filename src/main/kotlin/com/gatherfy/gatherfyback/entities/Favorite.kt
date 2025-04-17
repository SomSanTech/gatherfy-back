package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "favorites")
data class Favorite(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    var favoriteId: Long? = null,
    @Column(name = "user_id")
    var userId: Long,
    @Column(name = "event_id",insertable=false, updatable=false)
    var eventId: Long,
    @ManyToOne
    @JoinColumn(name = "event_id")
    var event: Event,
    @Column(name = "created_at")
    var createdAt: LocalDateTime
)
