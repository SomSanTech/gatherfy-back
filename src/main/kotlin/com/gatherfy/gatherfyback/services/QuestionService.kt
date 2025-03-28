package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.AccessDeniedException
import com.gatherfy.gatherfyback.dtos.CreateQuestionDTO
import com.gatherfy.gatherfyback.dtos.EditQuestionDTO
import com.gatherfy.gatherfyback.entities.Question
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.QuestionRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class QuestionService(
    val questionRepository: QuestionRepository,
    private val eventRepository: EventRepository,
){

    fun getAllQuestionByEventId(eventId:Long): List<Question> {
        val questions = questionRepository.findQuestionsByEventId(eventId)
        return questions
    }

    fun createQuestionWithAuth(userId: Long, createQuestionDTO: CreateQuestionDTO): Question {
        try {
            eventRepository.findById(createQuestionDTO.eventId!!).orElseThrow {
                EntityNotFoundException("Event id ${createQuestionDTO.eventId} does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId, createQuestionDTO.eventId)
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

    fun updateQuestionWithAuth(userId: Long, questionId: Long,updateQuestion:EditQuestionDTO): Question {
        try {
            val question = questionRepository.findById(questionId).orElseThrow{
                EntityNotFoundException("Question id $questionId does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId, question.eventId)
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

    fun deleteQuestionWithAuth(userId: Long, questionId: Long){
        try {
            val question = questionRepository.findById(questionId).orElseThrow{
                EntityNotFoundException("Question id $questionId does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId, question.eventId)
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