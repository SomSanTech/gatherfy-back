package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.Tag
import com.gatherfy.gatherfyback.repositories.TagRepository
import org.springframework.stereotype.Service

@Service
class TagService(private val tagRepository: TagRepository) {

    fun getAllTags(): List<Tag>{
        return tagRepository.findAll()
    }
}