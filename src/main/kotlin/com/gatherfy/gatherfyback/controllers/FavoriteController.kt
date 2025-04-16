package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.dtos.CreateFavoriteDTO
import com.gatherfy.gatherfyback.dtos.FavoriteDTO
import com.gatherfy.gatherfyback.entities.Favorite
import com.gatherfy.gatherfyback.services.FavoriteService
import com.gatherfy.gatherfyback.services.TokenService
import org.apache.coyote.BadRequestException
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/", "http://localhost:3000/"])
class FavoriteController(
    private val tokenService: TokenService,
    private val favoriteService: FavoriteService
) {
    @GetMapping("/v1/favorites")
    fun getFavoriteEvents(@RequestHeader("Authorization") token: String): List<FavoriteDTO>{
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return favoriteService.getAllFavoriteEvents(userId.toInt())
    }

    @PostMapping("/v1/favorites")
    fun addFavoriteEvent(@RequestHeader("Authorization") token: String, @RequestBody createFavoriteDTO: CreateFavoriteDTO): FavoriteDTO{
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        return favoriteService.addFavoriteEvent(userId.toInt(), createFavoriteDTO)
    }

    @DeleteMapping("/v1/favorites/{favoriteId}")
    fun removeFavoriteEvent(@RequestHeader("Authorization") token: String, @PathVariable favoriteId: String){
        val userId = tokenService.getSubjectFromToken(token.substringAfter("Bearer "))
        val id = favoriteId.toLongOrNull()
            ?: throw BadRequestException("Invalid event ID format")
        return favoriteService.removeFavoriteEvent(userId.toInt(), id)
    }
}