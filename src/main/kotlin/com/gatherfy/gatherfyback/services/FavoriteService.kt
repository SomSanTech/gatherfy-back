package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.dtos.CreateFavoriteDTO
import com.gatherfy.gatherfyback.dtos.FavoriteDTO
import com.gatherfy.gatherfyback.entities.Favorite
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.FavoriteRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class FavoriteService(
    val favoriteRepository: FavoriteRepository,
    private val minioService: MinioService,
    private val eventRepository: EventRepository
) {
    fun getAllFavoriteEvents(userId: Int): List<FavoriteDTO>{
        val favoriteList = favoriteRepository.findFavoritesByUserId(userId.toLong())
        return  favoriteList.map { toFavoriteDto(it) }
    }

    fun addFavoriteEvent(userId: Int, createFavoriteDTO: CreateFavoriteDTO): FavoriteDTO{
        try{
            val isEventExist = eventRepository.findEventByEventId(createFavoriteDTO.eventId)
                ?: throw EntityNotFoundException("Event id ${createFavoriteDTO.eventId} does not exist")

            val isFavoriteExist = favoriteRepository.findByUserIdAndAndEventId(userId.toLong(), createFavoriteDTO.eventId)
            if(isFavoriteExist == null){
                val fav = Favorite(
                    userId = userId.toLong(),
                    eventId = createFavoriteDTO.eventId,
                    event = isEventExist
                )
                val savedFav = favoriteRepository.save(fav)
                return toFavoriteDto(savedFav)
            }else{
                throw ConflictException("You already added this event to your favorite")
            }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }

    fun removeFavoriteEvent(userId: Int, eventId: Long){
        try{
            eventRepository.findEventByEventId(eventId)
                ?: throw EntityNotFoundException("Event id $eventId does not exist")
            val isFavoriteExist = favoriteRepository.findByUserIdAndAndEventId(userId.toLong(), eventId)
            if(isFavoriteExist !== null){
                return favoriteRepository.delete(isFavoriteExist)
            }else{
                throw EntityNotFoundException("You do not have this event in your favorite")
            }
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun toFavoriteDto(favorite: Favorite): FavoriteDTO{
        return FavoriteDTO(
            favoriteId = favorite.favoriteId!!,
            eventId = favorite.event.event_id!!,
            eventName = favorite.event.event_name,
            eventSlug = favorite.event.event_slug,
            eventImage = minioService.getImageUrl("thumbnails", favorite.event.event_image)
        )
    }
}