package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.services.AnswerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class AnswerController(val answerService: AnswerService) {
    @GetMapping("/v1/answers/feedback/{feedbackId}")
    fun getAllAnswerByFeedbackId(@PathVariable("feedbackId") feedbackId: Long): List<AnswerDTO> {
        return answerService.getAnswerByFeedBackId(feedbackId)
    }

    @GetMapping("/v1/answers/owner/{ownerId}")
    fun getFeedbackByOwner(@PathVariable ownerId: Long): List<AnswerDTO> {
        return answerService.getAllAnswerByOwner(ownerId)
    }

    @PostMapping("/v1/answers")
    fun createAnswer(
        @RequestBody createAnswerDTO: CreateAnswerDTO
    ): ResponseEntity<AnswerDTO> {
        val createdAnswer = answerService.createAnswer(createAnswerDTO)
        return ResponseEntity.ok(createdAnswer)
    }

}