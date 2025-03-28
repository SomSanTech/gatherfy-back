package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.dtos.EditQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.services.QuestionService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
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

    @PutMapping("/v2/questions/{questionId}")
    fun updateQuestion(
        @PathVariable("questionId") questionId: String,
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid updateQuestion: EditQuestionDTO,
    ): Question {
        val id = questionId.toLongOrNull()
            ?: throw BadRequestException("Invalid question ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return questionService.updateQuestionWithAuth(userId.toLong(), id, updateQuestion)
    }

    @PostMapping("/v2/questions")
    fun createQuestionWithAuth(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid createQuestionDTO: CreateQuestionDTO
    ): Question {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return questionService.createQuestionWithAuth(userId.toLong(), createQuestionDTO)
    }

    @DeleteMapping("/v2/questions/{questionId}")
    fun deleteQuestionWithAuth(
        @RequestHeader("Authorization") token: String,
        @PathVariable("questionId") questionId: String,
    ){
        val id = questionId.toLongOrNull()
            ?: throw BadRequestException("Invalid question ID format")
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        questionService.deleteQuestionWithAuth(userId.toLong(), id)
    }

}