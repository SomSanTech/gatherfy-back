package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.dtos.EditQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.services.QuestionService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
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
        @PathVariable("questionId") questionId: String,
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid updateQuestion: EditQuestionDTO,
    ): Question {
        val id = questionId.toLongOrNull()
            ?: throw BadRequestException("Invalid question ID format")
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return questionService.updateQuestionWithAuth(userId, id, updateQuestion)
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
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        return questionService.createQuestionWithAuth(userId, createQuestionDTO)
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
        @PathVariable("questionId") questionId: String,
    ){
        val id = questionId.toLongOrNull()
            ?: throw BadRequestException("Invalid question ID format")
        val userId = tokenService.getUserIdFromToken(token.substringAfter("Bearer "))
        questionService.deleteQuestionWithAuth(userId, id)
    }

}