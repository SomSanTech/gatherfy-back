package com.gatherfy.gatherfyback.entities

data class AuthResponse(
    var accessToken: String,
    var refreshToken: String
)
