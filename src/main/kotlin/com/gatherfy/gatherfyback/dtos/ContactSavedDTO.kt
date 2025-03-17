package com.gatherfy.gatherfyback.dtos

import com.gatherfy.gatherfyback.entities.User

data class ProfileDTO(
    var userProfile: User,
    var userSocials: List<Social>
)

data class ContactSavedDTO(
    var contactId: Long,
    var userProfile: User,
    var userSocials: List<Social>,
    var mutualEvents: List<MutualEvent>
)

data class Social(
    var socialPlatform: String,
    var socialLink: String
)

data class MutualEvent(
    var eventName: String,
    var eventSlug: String
)


