package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.Feedback
import com.gatherfy.gatherfyback.services.AnswerService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class AnswerController(val answerService: AnswerService, val tokenService: TokenService) {
    // Nowhere to use
    @GetMapping("/v1/answers/feedback/{feedbackId}")
    fun getAllAnswerByFeedbackId(@PathVariable("feedbackId") feedbackId: Long): List<AnswerDTO> {
        return answerService.getAnswerByFeedBackId(feedbackId)
    }

    // Nowhere to use
    @GetMapping("/v1/answers/owner/{ownerId}")
    fun getFeedbackByOwner(@PathVariable ownerId: Long): List<AnswerDTO> {
        return answerService.getAllAnswerByOwner(ownerId)
    }

    @GetMapping("/v1/answers/question/{questionId}")
    fun getAnswerByQuestionId(@PathVariable("questionId") questionId: Long): List<AnswerDTO> {
        return answerService.getAnswerByQuestionId(questionId)
    }

    @GetMapping("/v2/answers/question/{questionId}")
    fun getAnswerByQuestionIdWithAuth(@RequestHeader("Authorization") token: String,@PathVariable("questionId") questionId: Long): List<AnswerDTO> {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return answerService.getAnswerByQuestionIdWithAuth(username,questionId)
    }

//    @PostMapping("/v1/answers")
//    fun createAnswer(
//        @RequestBody createAnswerDTO: CreateAnswerDTO
//    ): ResponseEntity<AnswerDTO> {
//        val createdAnswer = answerService.createAnswer(createAnswerDTO)
//        return ResponseEntity.ok(createdAnswer)
//    }

    @PostMapping("/v2/answers")
    fun createAnswer(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid createAnswerDTO: CreateAnswerDTO
    ): AnswerDTO {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return answerService.createAnswerWithAuth(username,createAnswerDTO)
    }

}