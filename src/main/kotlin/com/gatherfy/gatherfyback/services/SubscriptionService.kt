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

    fun getSubscription(username: String): List<Subscription>?{
        try{
            val user = userRepository.findByUsername(username)
            return subscriptionRepository.findAllByUserId(user?.users_id!!)
        }catch (e: Exception){
            throw RuntimeException("An unexpected error occurred: ${e.message}")
        }
    }

    fun createSubscription(username: String, newSubscribe: CreateSubscriptionDTO): Subscription{
        try{
            val user = userRepository.findByUsername(username)
            val isSubscribed = subscriptionRepository.findSubscriptionByUserAndTag(user?.users_id!!, newSubscribe.tagId)
            val tag = tagRepository.findTagByTagId(newSubscribe.tagId)
            if(isSubscribed != null){
                throw ConflictException("User is already subscribe for this tag")
            }
            val subscription = Subscription(
                subscriptionId = null,
                userId = user.users_id!!,
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

    fun deleteSubscription(username: String, tagId: Long){
        try{
            val user = userRepository.findByUsername(username)
            val isSubscribed = subscriptionRepository.findSubscriptionByUserAndTag(user?.users_id!!, tagId)
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

    fun getTagsAlreadySubscribed(username: String): Map<String, List<Long>>{
        val user = userRepository.findByUsername(username)
        val tagList = subscriptionRepository.findSubscriptionsByUserId(user?.users_id!!)
        return mapOf("tagId" to tagList!!.map { it.tagId })
    }
}