package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var users_id: Long,
    var users_firstname: String,
    var users_lastname: String,
    var username: String,
    var users_gender: String,
    var users_email: String,
    var users_phone: String,
)
