package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.FeedbackCountDTO
import com.gatherfy.gatherfyback.dtos.FeedbackDTO
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.services.FeedbackService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/", "http://localhost:3000/"])
class FeedbackController(val feedbackService: FeedbackService) {
    @GetMapping("/v1/feedbacks")
    fun getAllFeedback(): List<Feedback> {
        return feedbackService.getAllFeedback()
    }

    @GetMapping("/v1/feedbacks/event/{eventId}")
    fun getFeedbackByEventId(@PathVariable eventId: Long): List<FeedbackDTO> {
        return feedbackService.getAllFeedbackByEventId(eventId)
    }

    @GetMapping("/v2/feedbacks/event/{eventId}")
    fun getFeedbackAndCountByEventId(@PathVariable eventId: Long): FeedbackCountDTO {
        return feedbackService.getFeedbackAndCountByEventId(eventId)
    }

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

    @DeleteMapping("/v1/feedbacks/{feedbackId}")
    fun deleteFeedback(@PathVariable feedbackId: Long) {
        feedbackService.deleteFeedback(feedbackId)
    }

}