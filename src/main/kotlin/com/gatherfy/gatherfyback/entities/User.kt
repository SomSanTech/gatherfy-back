package com.gatherfy.gatherfyback.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var users_id: Long?,
    var users_firstname: String,
    var users_lastname: String,
    var username: String,
    var password: String?,
    var users_gender: String?,
    var users_email: String,
    var users_phone: String?,
    var users_image: String?,
    var users_role: String,
    var users_age: Long?,
    var users_birthday: LocalDateTime?,
    var otp: String?,
    var is_verified: Boolean,
    var otp_expires_at: LocalDateTime?,
    var auth_provider: String?,
    var email_new_events: Boolean,
    var email_reminders_day: Boolean,
    var email_reminders_hour: Boolean,
    var email_updated_events: Boolean,
    )
