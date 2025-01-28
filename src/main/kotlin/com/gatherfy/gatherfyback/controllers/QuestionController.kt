package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.services.QuestionService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class QuestionController(
    val questionService: QuestionService,
    private val tokenService: TokenService
) {

    @GetMapping("/v1/questions/event/{eventId}")
    fun getAllRegistrations(@PathVariable("eventId") eventId:Long):List<Question> {
        return questionService.getAllQuestionByEventId(eventId)
    }

    @PutMapping("/v1/questions/{questionId}")
    fun updateQuestion(
        @PathVariable("questionId") questionId: Long,
        @RequestBody updateQuestion: CreateQuestionDTO,
    ): ResponseEntity<Question> {
        val updatedQuestion = questionService.updateQuestion(questionId, updateQuestion)
        return ResponseEntity.ok(updatedQuestion)
    }

    @PutMapping("/v2/questions/{questionId}")
    fun updateQuestion(
        @PathVariable("questionId") questionId: Long,
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid updateQuestion: CreateQuestionDTO,
    ): Question {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return questionService.updateQuestionWithAuth(username, questionId, updateQuestion)
    }

    @PostMapping("/v1/questions")
    fun createQuestion(
        @RequestBody createQuestionDTO: CreateQuestionDTO
    ): ResponseEntity<Question> {
        val createdQuestion = questionService.createQuestion(createQuestionDTO)
        return ResponseEntity.ok(createdQuestion)
    }

    @PostMapping("/v2/questions")
    fun createQuestionWithAuth(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid createQuestionDTO: CreateQuestionDTO
    ): Question {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return questionService.createQuestionWithAuth(username, createQuestionDTO)
    }

    @DeleteMapping("/v1/questions/{questionId}")
    fun deleteQuestion(
        @PathVariable("questionId") questionId: Long,
    ){
        questionService.deleteQuestion(questionId)
    }

    @DeleteMapping("/v2/questions/{questionId}")
    fun deleteQuestionWithAuth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("questionId") questionId: Long,
    ){
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        questionService.deleteQuestionWithAuth(username, questionId)
    }

}