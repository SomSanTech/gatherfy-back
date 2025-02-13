package com.gatherfy.gatherfyback.entities

import jakarta.persistence.*

@Entity(name = "subscriptions")
data class Subscription(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    var subscriptionId: Long?,
    @Column(name = "user_id")
    var userId: Long,
    @Column(name = "tag_id")
    var tagId: Long,
)
