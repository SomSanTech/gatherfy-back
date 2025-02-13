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
}