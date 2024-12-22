package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.services.QuestionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class QuestionController(val questionService: QuestionService) {

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

    @PostMapping("/v1/questions")
    fun createQuestion(
        @RequestBody createQuestionDTO: CreateQuestionDTO
    ): ResponseEntity<Question> {
        val createdQuestion = questionService.createQuestion(createQuestionDTO)
        return ResponseEntity.ok(createdQuestion)
    }

    @DeleteMapping("/v1/questions/{questionId}")
    fun deleteQuestion(
        @PathVariable("questionId") questionId: Long,
    ){
        questionService.deleteQuestion(questionId)
    }

}