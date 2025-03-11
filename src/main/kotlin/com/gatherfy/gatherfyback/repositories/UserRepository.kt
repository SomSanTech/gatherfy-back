package com.gatherfy.gatherfyback.repositories

import com.gatherfy.gatherfyback.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository: JpaRepository<User, Long> {
     override fun findById(ownerId: Long): Optional<User>
     fun findByUsername(username: String): User?
     @Query("from users where users_email = :email")
     fun findByEmail(@Param("email") email: String): User?
     @Query("from users where username = :input or users_email = :input")
     fun findUserByUsernameOrEmail(@Param("input") input: String): User?
     @Query("from users where users_id = :id")
     fun findUserById(@Param("id") id: Long): User?
     @Query("SELECT u.username FROM users u WHERE u.users_email = :email")
     fun findUsernameByEmail(@Param("email") email: String): String
     @Query("select distinct u from users u join subscriptions s on u.users_id = s.userId where s.tagId in :tagIds and u.email_new_events = true")
     fun findFollowerByTagIdAndEnableEmailNewEvents(@Param("tagIds") tagIds: List<Long?>): List<User>
     @Query("from users u join registrations r on u.users_id = r.userId where r.eventId = :eventId and u.email_reminders_day = true")
     fun findParticipantsByEventIdAndEnableEmailReminderDay(@Param("eventId") eventId: Long): List<User>?
     @Query("from users u join registrations r on u.users_id = r.userId where r.eventId = :eventId and u.email_reminders_hour = true")
     fun findParticipantsByEventIdAndEnableEmailReminderHour(@Param("eventId") eventId: Long): List<User>?
     @Query("from users u join registrations r on u.users_id = r.userId where r.eventId = :eventId and u.email_updated_events = true")
     fun  findParticipantsByEventIdAndEnableEmailUpdatedEvent(@Param("eventId") eventId: Long): List<User>
}