package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.dtos.CreateSubscriptionDTO
import com.gatherfy.gatherfyback.entities.Subscription
import com.gatherfy.gatherfyback.repositories.SubscriptionRepository
import com.gatherfy.gatherfyback.repositories.TagRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val emailSenderService: EmailSenderService,
    private val tagRepository: TagRepository
) {

    fun getSubscription(userId: Long): List<Subscription>?{
        try{
            return subscriptionRepository.findAllByUserId(userId)
        }catch (e: Exception){
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun createSubscription(userId: Long, newSubscribe: CreateSubscriptionDTO): Subscription{
        try{
            val user = userRepository.findByUserId(userId)
            val isSubscribed = subscriptionRepository.findSubscriptionByUserAndTag(userId, newSubscribe.tagId)
            val tag = tagRepository.findTagByTagId(newSubscribe.tagId)
            if(isSubscribed != null){
                throw ConflictException("User is already subscribe for this tag")
            }
            val subscription = Subscription(
                subscriptionId = null,
                userId = userId,
                tagId = newSubscribe.tagId,
            )
            val savedSubscription =  subscriptionRepository.save(subscription)
            emailSenderService.sendEmailFollowTag(tag!!,user)
            return savedSubscription
        } catch (e: ConflictException) {
            throw ConflictException(e.message!!)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun deleteSubscription(userId: Long, tagId: Long){
        try{
            val user = userRepository.findByUserId(userId)
            val isSubscribed = subscriptionRepository.findSubscriptionByUserAndTag(userId, tagId)
            val tag = tagRepository.findTagByTagId(tagId)

            if(isSubscribed === null){
                throw EntityNotFoundException("User is not subscribe for this tag")
            }else {
                subscriptionRepository.delete(isSubscribed)
                emailSenderService.sendEmailUnfollowTag(tag!!,user)
            }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: Exception) {
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun getTagsAlreadySubscribed(userId: Long): Map<String, List<Long>>{
        val tagList = subscriptionRepository.findSubscriptionsByUserId(userId)
        return mapOf("tagId" to tagList!!.map { it.tagId })
    }
}