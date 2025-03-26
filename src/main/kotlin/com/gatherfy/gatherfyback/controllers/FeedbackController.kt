package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateFeedbackDTO
import com.gatherfy.gatherfyback.dtos.FeedbackCountDTO
import com.gatherfy.gatherfyback.dtos.FeedbackDTO
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.services.FeedbackService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/", "http://localhost:3000/"])
class FeedbackController(
    val feedbackService: FeedbackService,
    private val tokenService: TokenService
) {
    // Nowhere to use
    @GetMapping("/v1/feedbacks")
    fun getAllFeedback(): List<Feedback> {
        return feedbackService.getAllFeedback()
    }

    @GetMapping("/v1/feedbacks/event/{eventId}")
    fun getFeedbackByEventId(@RequestHeader("Authorization")token: String,@PathVariable eventId: String): List<FeedbackDTO> {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return feedbackService.getAllFeedbackByEventId(userId.toInt(), id)
    }

    @GetMapping("/v2/feedbacks/event/{eventId}")
    fun getFeedbackAndCountByEventId(@RequestHeader("Authorization")token: String,@PathVariable eventId: String): FeedbackCountDTO {
        val id = eventId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return feedbackService.getFeedbackAndCountByEventId(userId.toInt(), id)
    }

    // Nowhere to use
    @GetMapping("/v1/feedbacks/owner/{ownerId}")
    fun getFeedbackByOwner(@PathVariable ownerId: Long): List<FeedbackDTO> {
        return feedbackService.getAllFeedbackByOwner(ownerId)
    }

    @PostMapping("/v1/feedbacks")
    fun createFeedback(
        @RequestBody feedback: Feedback
    ): ResponseEntity<Feedback> {
        val createdFeedback = feedbackService.createFeedback(feedback)
        return ResponseEntity.ok(createdFeedback)
    }

    @PostMapping("/v2/feedbacks")
    fun createFeedbackWithAuth(
        @RequestHeader("Authorization")token: String,
        @RequestBody @Valid feedback: CreateFeedbackDTO
    ): Feedback {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return feedbackService.createFeedbackWithAuth(userId.toLong(),feedback)
    }

    // Nowhere to use
    @DeleteMapping("/v1/feedbacks/{feedbackId}")
    fun deleteFeedback(@PathVariable feedbackId: Long) {
        feedbackService.deleteFeedback(feedbackId)
    }

    @GetMapping("/v1/feedbacked")
    fun getEventAlreadyFeedbacked(
        @RequestHeader("Authorization")token: String,
    ): Map<String, List<Long>>{
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return feedbackService.getEventAlreadyFeedbacked(userId.toLong())
    }

}