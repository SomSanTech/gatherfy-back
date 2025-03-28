package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.services.EventService
import com.gatherfy.gatherfyback.services.ReportService
import com.gatherfy.gatherfyback.services.TokenService
import org.apache.coyote.BadRequestException
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class ReportController(
    private val reportService: ReportService,
    private val eventService: EventService,
    private val tokenService: TokenService
) {

    @PostMapping("/v1/report/{eventId}")
    fun generateReport(
        @RequestHeader("Authorization") token: String,
        @PathVariable eventId: String)
    : ResponseEntity<ByteArrayResource> {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid contact ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        val reportBytes = reportService.generateReport(userId.toLong(),id)
        val customFileName  = eventService.getEventById(id).name
        val resource = ByteArrayResource(reportBytes)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$customFileName Feedback Report.xlsx\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(reportBytes.size.toLong())
            .body(resource)
    }
}