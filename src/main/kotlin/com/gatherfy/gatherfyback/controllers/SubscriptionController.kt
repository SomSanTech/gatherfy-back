
package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.services.SubscriptionService
import com.gatherfy.gatherfyback.services.TokenService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.gatherfy.gatherfyback.dtos.CreateSubscriptionDTO
import com.gatherfy.gatherfyback.entities.Subscription
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody


@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class SubscriptionController(
    private val tokenService: TokenService,
    private val subscriptionService: SubscriptionService
) {

    @PostMapping("/v1/subscribe")
    fun createSubscription(
        @RequestHeader("Authorization")token: String,
        @RequestBody newSubscription: CreateSubscriptionDTO
    ): Subscription {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return subscriptionService.createSubscription(username, newSubscription)
    }

    @GetMapping("/v1/subscribe")
    fun getSubscription(
        @RequestHeader("Authorization")token: String
    ): List<Subscription>? {
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return subscriptionService.getSubscription(username)
    }

    @DeleteMapping("/v1/subscribe/{tagId}")
    fun deleteSubscription(
        @RequestHeader("Authorization")token: String,
        @PathVariable tagId: Long
    ){
        val username = tokenService.getUsernameFromToken(token.substringAfter("Bearer "))
        return subscriptionService.deleteSubscription(username, tagId)
    }
}