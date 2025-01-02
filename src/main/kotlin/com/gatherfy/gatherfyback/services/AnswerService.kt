package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.AnswerDTO
import com.gatherfy.gatherfyback.dtos.CreateAnswerDTO
import com.gatherfy.gatherfyback.entities.Answer
import com.gatherfy.gatherfyback.repositories.AnswerRepository
import com.gatherfy.gatherfyback.repositories.FeedbackRepository
import com.gatherfy.gatherfyback.repositories.QuestionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AnswerService(val answerRepository: AnswerRepository,val feedbackRepository: FeedbackRepository,val questionRepository: QuestionRepository) {

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
        if(answer.isEmpty()){
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found")
        }
        return answer.map { toAnswerDTO(it) }
    }

    fun createAnswer(createAnswerDTO: CreateAnswerDTO): AnswerDTO {
        try {
             val feedback = feedbackRepository.findById(createAnswerDTO.feedbackId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found") }

            val question = questionRepository.findById(createAnswerDTO.questionId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found") }

            val answer = Answer(
                question=question,
                feedback = feedback,
                questionId = createAnswerDTO.questionId,
                eventId = createAnswerDTO.eventId,
                answerText = createAnswerDTO.answerText,
                feedbackId = createAnswerDTO.feedbackId,
            )

            val savedAnswer = answerRepository.save(answer)
            return toAnswerDTO(savedAnswer)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.BAD_REQUEST,e.message ?: "Unknown error")
        }
    }

    private fun toAnswerDTO(answer: Answer): AnswerDTO {
        return AnswerDTO(
            answerId = answer.answerId,
            feedbackId = answer.feedback.feedbackId,
            questionId = answer.question.questionId,
            eventId = answer.eventId,
            questionText = answer.question.questionText,
            answerText = answer.answerText,
        )
    }
}