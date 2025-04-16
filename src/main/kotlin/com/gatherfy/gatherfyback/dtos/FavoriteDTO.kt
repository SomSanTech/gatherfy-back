package com.gatherfy.gatherfyback.dtos

data class FavoriteDTO(
    val favoriteId: Long,
    val eventId: Long,
    val eventName: String,
    val eventSlug: String,
    val eventImage: String
)
