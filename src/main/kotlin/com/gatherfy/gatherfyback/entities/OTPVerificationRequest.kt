package com.gatherfy.gatherfyback.entities

data class OTPVerificationRequest(
    var email: String,
    var otp: String
)
