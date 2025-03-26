package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.AccessDeniedException
import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.dtos.EditQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.QuestionRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class QuestionService(
    val questionRepository: QuestionRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
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
            throw BadRequestException("Invalid question type provided. Allowed values are: 'text', 'rating'.")
        }
    }

    fun createQuestionWithAuth(userId: Int, createQuestionDTO: CreateQuestionDTO): Question {
        try {
//            val user = userRepository.findByUsername(username)
            eventRepository.findById(createQuestionDTO.eventId!!).orElseThrow {
                EntityNotFoundException("Event id ${createQuestionDTO.eventId} does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId.toLong(), createQuestionDTO.eventId)
            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            val question = Question(
                eventId = createQuestionDTO.eventId,
                questionText =  createQuestionDTO.questionText,
                questionType = createQuestionDTO.questionType
            )
            val savedQuestion = questionRepository.save(question)
            return savedQuestion
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        } catch (e: Exception){
            throw BadRequestException("Invalid question type provided. Allowed values are: 'text', 'rating'.")
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

    fun updateQuestionWithAuth(userId: Int, questionId: Long,updateQuestion:EditQuestionDTO): Question {
        try {
//            val user = userRepository.findByUsername(username)
            val question = questionRepository.findById(questionId).orElseThrow{
                EntityNotFoundException("Question id $questionId does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId.toLong(), question.eventId)
            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            question.questionText = updateQuestion.questionText
            question.questionType = updateQuestion.questionType
            return questionRepository.save(question)
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        } catch (e: Exception){
            throw BadRequestException("Invalid question type provided. Allowed values are: 'Text', 'Rating'.")
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

    fun deleteQuestionWithAuth(userId: Int, questionId: Long){
        try {
//            val user = userRepository.findByUsername(username)
            val question = questionRepository.findById(questionId).orElseThrow{
                EntityNotFoundException("Question id $questionId does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId.toLong(), question.eventId)
            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            questionRepository.delete(question)
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }
}