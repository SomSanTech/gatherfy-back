package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository: JpaRepository <Subscription, Long> {

    @Query("from subscriptions where userId = :userId and tagId = :tagId")
    fun findSubscriptionByUserAndTag(@Param("userId") userId: Long, @Param("tagId") tagId: Long): Subscription?

    fun findAllByUserId(userId: Long): List<Subscription>?

    fun findSubscriptionsByTagIdIn(tagId: List<Long?>): List<Subscription>?

    fun findSubscriptionsByUserId(userId: Long): List<Subscription>?
}