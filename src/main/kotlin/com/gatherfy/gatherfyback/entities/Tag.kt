package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "tags")
data class Tag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var tag_id: Long?,
    var tag_title: String,
    var tag_code: String,
)
