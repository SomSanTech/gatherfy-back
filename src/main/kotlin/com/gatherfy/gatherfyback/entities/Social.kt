package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "socials")
data class Social(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    var socialId: Long? = null,
    @Column(name = "user_id")
    var userId: Long,
    @Column(name = "social_platform")
    var socialPlatform: String,
    @Column(name = "social_link")
    var socialLink: String
)
