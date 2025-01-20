package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateUserDTO
import com.gatherfy.gatherfyback.dtos.UserDTO
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository) {
    fun createUser(userDto: CreateUserDTO): UserDTO {
        try{
            val existingUsername = userRepository.findByUsername(userDto.username)
            val existingEmail = userRepository.findByEmail(userDto.email)
            if(existingUsername != null){
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already taken")
            }
            else if (existingEmail != null){
                throw ResponseStatusException(HttpStatus.CONFLICT, "Email already taken")
            }
            else {
                val encoder = BCryptPasswordEncoder(16)
                val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}".toRegex()
                val emailPattern = "^[^@]+@[^@]+\\.[^@]+\$".toRegex()
                if(!userDto.email.matches(emailPattern)){
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email wrong pattern")
                }
                else if (!userDto.password.matches(passwordPattern)){
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Password wrong pattern")
                }
                else {
                    val user = User(
                        users_id = null,
                        users_firstname = userDto.firstname,
                        users_lastname = userDto.lastname,
                        username = userDto.username,
                        users_gender = userDto.gender,
                        users_email = userDto.email,
                        users_phone = userDto.phone,
                        users_role = userDto.role,
                        users_birthday = userDto.birthday,
                        users_age = null,
                        password = encoder.encode(userDto.password),
                    )
                    val savedUser = userRepository.save(user)
                    return toUserDto(savedUser)
                }
            }
        } catch (e: ResponseStatusException){
            throw e
        }
    }

    fun editUser(userDto: CreateUserDTO): UserDTO {
        try{
            val existingUsername = userRepository.findByUsername(userDto.username)
            val existingEmail = userRepository.findByEmail(userDto.email)
            if(existingUsername != null){
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already taken")
            }
            else if (existingEmail != null){
                throw ResponseStatusException(HttpStatus.CONFLICT, "Email already taken")
            }
            else {
                val encoder = BCryptPasswordEncoder(16)
                val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}".toRegex()
                val emailPattern = "^[^@]+@[^@]+\\.[^@]+\$".toRegex()
                if(!userDto.email.matches(emailPattern)){
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email wrong pattern")
                }
                else if (!userDto.password.matches(passwordPattern)){
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Password wrong pattern")
                }
                else {
                    val user = User(
                        users_id = null,
                        users_firstname = userDto.firstname,
                        users_lastname = userDto.lastname,
                        username = userDto.username,
                        users_gender = userDto.gender,
                        users_email = userDto.email,
                        users_phone = userDto.phone,
                        users_role = userDto.role,
                        users_birthday = userDto.birthday,
                        users_age = null,
                        password = encoder.encode(userDto.password),
                    )
                    val savedUser = userRepository.save(user)
                    return toUserDto(savedUser)
                }
            }
        } catch (e: ResponseStatusException){
            throw e
        }
    }

    fun toUserDto(user: User): UserDTO{
        return UserDTO(
            username = user.username,
            email = user.users_email,
            phone = user.users_phone,
            role = user.users_role
        )
    }

}

