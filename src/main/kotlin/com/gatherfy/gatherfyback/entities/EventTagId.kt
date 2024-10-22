package com.gatherfy.gatherfyback.entities

import java.io.Serializable

data class EventTagId(
    var event: Long = 0,
    var tag: Long = 0
) : Serializable
