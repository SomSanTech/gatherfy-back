package com.gatherfy.gatherfyback.controllers


import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.services.AnswerService
import com.gatherfy.gatherfyback.services.TokenService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class AnswerController(val answerService: AnswerService, val tokenService: TokenService) {

    @GetMapping("/v1/answers/question/{questionId}")
    fun getAnswerByQuestionId(@PathVariable("questionId") questionId: Long): List<AnswerDTO> {
        return answerService.getAnswerByQuestionId(questionId)
    }

    @GetMapping("/v2/answers/question/{questionId}")
    fun getAnswerByQuestionIdWithAuth(@RequestHeader("Authorization") token: String,@PathVariable("questionId") questionId: Long): List<AnswerDTO> {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return answerService.getAnswerByQuestionIdWithAuth(userId.toLong(),questionId)
    }

    @PostMapping("/v2/answers")
    fun createAnswer(
        @RequestHeader("Authorization") token: String,
        @RequestBody @Valid createAnswerDTO: CreateAnswerDTO
    ): AnswerDTO {
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return answerService.createAnswerWithAuth(userId.toLong(),createAnswerDTO)
    }

}