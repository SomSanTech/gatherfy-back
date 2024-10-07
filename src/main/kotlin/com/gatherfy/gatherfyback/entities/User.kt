package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var user_id: Long,
    var user_firstname: String,
    var user_lastname: String,
    var user_gender: String,
    var user_email: String,
    var user_phone: String,
    var user_role: String,
    var user_organize_name: String,
)
