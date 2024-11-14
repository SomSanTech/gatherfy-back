package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.entities.Tag
import com.gatherfy.gatherfyback.services.TagService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class TagController(private val tagService: TagService) {

    @GetMapping("/v1/tags")
    fun getEvent() : List<Tag> {
        return tagService.getAllTags()
    }
}