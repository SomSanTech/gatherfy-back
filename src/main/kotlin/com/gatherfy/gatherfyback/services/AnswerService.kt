package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.AnswerDTO
import com.gatherfy.gatherfyback.dtos.CreateAnswerDTO
import com.gatherfy.gatherfyback.entities.Answer
import com.gatherfy.gatherfyback.repositories.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import com.gatherfy.gatherfyback.Exception.AccessDeniedException

@Service
class AnswerService(
    val answerRepository: AnswerRepository,
    val feedbackRepository: FeedbackRepository,
    val questionRepository: QuestionRepository,
    private val registrationRepository: RegistrationRepository,
    private val eventRepository: EventRepository
) {

    fun getAnswerByFeedBackId(feedBackId: Long): List<AnswerDTO> {
        val answer =  answerRepository.findAnswersByFeedbackId(feedBackId)
        if(answer.isEmpty()){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found")
        }
        return answer.map { toAnswerDTO(it) }
    }

    fun getAllAnswerByOwner(ownerId: Long) : List<AnswerDTO>{
        val answerList = answerRepository.findAnswersByOwnerId(ownerId)
        if(answerList.isEmpty()){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No feedback now")
        }
        return answerList.map { toAnswerDTO(it) }
    }

    fun getAnswerByQuestionId(questionId: Long) : List<AnswerDTO> {
        val answer = answerRepository.findAnswersByQuestionId(questionId)
        return answer!!.map { toAnswerDTO(it) }
    }

    fun getAnswerByQuestionIdWithAuth(userId: Long, questionId: Long) : List<AnswerDTO> {
        try{
            val question = questionRepository.findById(questionId).orElseThrow{
                EntityNotFoundException("Question id $questionId does not exist")
            }
            val existEvent = eventRepository.findEventByEventOwnerAndEventId(userId, question.eventId)
            if(existEvent === null){
                throw AccessDeniedException("You are not owner of this event")
            }
            val answer = answerRepository.findAnswersByQuestionId(questionId)
            return answer!!.map { toAnswerDTO(it) }
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    fun createAnswerWithAuth(userId: Long, createAnswerDTO: CreateAnswerDTO): AnswerDTO {
        try {
            val question = questionRepository.findById(createAnswerDTO.questionId)
                .orElseThrow { EntityNotFoundException("Question id ${createAnswerDTO.questionId} does not exist") }

            val feedback = feedbackRepository.findFeedbackByUserIdAndEventId(userId, question.eventId!!)
            if(feedback === null){
                throw EntityNotFoundException("User do not feedback this event yet.")
            }
            val userRegistration = registrationRepository.findRegistrationsByUserIdAndEventId(userId.toInt(),
                feedback.eventId!!.toInt()
            )

            if(userRegistration != null){
                val answer = Answer(
                    question=question,
                    feedback = feedback,
                    questionId = createAnswerDTO.questionId,
                    answerText = createAnswerDTO.answerText,
                    feedbackId = feedback.feedbackId!!,
                )

                val savedAnswer = answerRepository.save(answer)
                return toAnswerDTO(savedAnswer)
            }else {
                throw AccessDeniedException("You are not attendee of this event")
            }
        } catch (e: EntityNotFoundException) {
            throw EntityNotFoundException(e.message)
        } catch (e: AccessDeniedException){
            throw AccessDeniedException(e.message!!)
        }
    }

    private fun toAnswerDTO(answer: Answer): AnswerDTO {
        return AnswerDTO(
            answerId = answer.answerId,
            feedbackId = answer.feedback.feedbackId,
            questionId = answer.question.questionId,
            questionText = answer.question.questionText,
            answerText = answer.answerText,
        )
    }

    fun getAnswerByEventId(eventId: Long) : List<AnswerDTO> {
        val answer = answerRepository.findAnswersByEventId(eventId)
        return answer!!.map { toAnswerDTO(it) }
    }
}