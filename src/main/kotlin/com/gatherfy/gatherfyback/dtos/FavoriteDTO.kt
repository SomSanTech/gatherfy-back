package com.gatherfy.gatherfyback.dtos

import java.time.LocalDateTime

data class FavoriteDTO(
    val favoriteId: Long,
    val eventId: Long,
    val name: String,
    val slug: String,
    val image: String,
    val createdAt: LocalDateTime
)
