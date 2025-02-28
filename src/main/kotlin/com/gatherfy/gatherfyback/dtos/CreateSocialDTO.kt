package com.gatherfy.gatherfyback.dtos

data class CreateSocialDTO(
    val socialLinks: List<SocialLinkData>
)

data class SocialLinkData(
    val socialPlatform: String,
    val socialLink: String
)
