package com.gatherfy.gatherfyback.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail-sender")
data class MailSenderProperties (
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val protocol: String,
    val auth: Boolean,
    val starttlsEnable: Boolean,
    val debug: Boolean
)