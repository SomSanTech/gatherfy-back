package com.gatherfy.gatherfyback.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.apache.coyote.BadRequestException

data class CreateSocialDTO(
    @field:Valid
    var socialLinks: List<@Valid SocialLinkData>
)

data class SocialLinkData(
    var socialId: Long? = null,
    @field:NotNull(message = "Social platform is required")
    @field:NotBlank(message = "Social platform must not be blank")
    var socialPlatform: String?,
    @field:NotNull(message = "Social link is required")
    @field:NotBlank(message = "Social link must not be blank")
    var socialLink: String?
){
    fun validatePlatform() {
        if (socialPlatform != null && !SocialPlatform.entries.map { it.name }.contains(socialPlatform)) {
            throw BadRequestException("Invalid social platform: $socialPlatform. Valid values are: ${SocialPlatform.entries.joinToString()}")
        }
    }
}

enum class SocialPlatform{
    Instagram, Facebook, X, LinkedIn, Website
}


