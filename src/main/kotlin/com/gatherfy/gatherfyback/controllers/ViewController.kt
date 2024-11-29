package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.services.RegistrationService
import com.gatherfy.gatherfyback.services.ViewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class ViewController(val viewService: ViewService) {
    @GetMapping("/v1/views")
    fun getViewsByEventIds(@RequestParam("eventIds") eventIds: List<Long>): List<EventViewsDTO> {
        return viewService.getViewsByEventIds(eventIds)
    }

}