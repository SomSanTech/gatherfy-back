package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.RegistrationCreateDTO
import com.gatherfy.gatherfyback.dtos.RegistrationDTO
import com.gatherfy.gatherfyback.dtos.RegistrationUpdateStatusDTO
import com.gatherfy.gatherfyback.dtos.ViewDTO
import com.gatherfy.gatherfyback.services.RegistrationService
import com.gatherfy.gatherfyback.services.ViewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class ViewController(val viewService: ViewService) {
    @GetMapping("/v1/views/{eventId}")
    fun getViewByEventId(@PathVariable("eventId") eventId: Long):List<ViewDTO> {
        return viewService.getViewByEventId(eventId)
    }

}