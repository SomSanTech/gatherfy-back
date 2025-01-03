package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.repositories.QuestionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class QuestionService (
    val questionRepository: QuestionRepository,
){

    fun getAllQuestionByEventId(eventId:Long): List<Question> {
        val questions = questionRepository.findQuestionsByEventId(eventId)
        return questions
    }

    fun createQuestion(createQuestionDTO: CreateQuestionDTO): Question {
        try {
            val question = Question(
                eventId = createQuestionDTO.eventId,
                questionText =  createQuestionDTO.questionText,
                questionType = createQuestionDTO.questionType
            )
            val savedQuestion = questionRepository.save(question)
            return savedQuestion
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun updateQuestion(questionId: Long,updateQuestion:CreateQuestionDTO): Question {
        try {
            val question = questionRepository.findById(questionId).orElseThrow{  ResponseStatusException(HttpStatus.NOT_FOUND,"Question not found") }
            question.questionText = updateQuestion.questionText
            question.questionType = updateQuestion.questionType
            return questionRepository.save(question)
        } catch (e: ResponseStatusException){
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
    }

    fun deleteQuestion(questionId: Long){
        try {
            val question = questionRepository.findById(questionId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found")
            }
            questionRepository.delete(question)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}