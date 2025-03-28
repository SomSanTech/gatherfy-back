package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.UsernameNotFoundException


typealias ApplicationUser = com.gatherfy.gatherfyback.entities.User

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findByUsername(username)?.mapToUserDetails()
            ?: throw UsernameNotFoundException("Username does not exist")


    fun ApplicationUser.mapToUserDetails(): UserDetails =
        User.builder()
            .username(this.users_id.toString())
            .password(this.password ?: "{noop}google-auth")
            .roles(this.users_role)
            .build()
}