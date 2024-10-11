package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "administrators")
data class Administrator(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var admin_id: Long,
    var admin_username: String,
    var admin_email: String,
    var admin_phone: String,
    var admin_role: String,
)
