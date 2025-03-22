package com.gatherfy.gatherfyback.dtos

data class CreateSocialDTO(
    var socialLinks: List<SocialLinkData>
)

data class SocialLinkData(
    var socialId: Long? = null,
    var socialPlatform: String,
    var socialLink: String
)
