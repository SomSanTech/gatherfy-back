package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.dtos.CreateUserDTO
import com.gatherfy.gatherfyback.dtos.EditUserDTO
import com.gatherfy.gatherfyback.dtos.UserDTO
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.UserRepository
import org.apache.coyote.BadRequestException
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
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
            println("on create user")
            if(existingUsername != null){
                println("Username already taken")
                throw ResponseStatusException(HttpStatus.CONFLICT, "Username already taken")
            }
            else if (existingEmail != null){
                println("Email already taken")
                throw ResponseStatusException(HttpStatus.CONFLICT, "Email already taken")
            }
            else {
                val encoder = BCryptPasswordEncoder(16)
                val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}".toRegex()
                val emailPattern = "^[^@]+@[^@]+\\.[^@]+\$".toRegex()
                if(!userDto.email.matches(emailPattern)){
                    throw BadRequestException("Email wrong pattern")
                }
                else if (!userDto.password.matches(passwordPattern)){
                    throw BadRequestException("Password wrong pattern")
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

    fun updateUser(username: String, userEdit: EditUserDTO): User {
        try{
            val userProfile = userRepository.findByUsername(username)
            if(userProfile != null) {
                val existingUsername = userRepository.findByUsername(userEdit.username)
                val existingEmail = userRepository.findByEmail(userEdit.email)
                if(existingUsername != null && existingUsername.users_id != userProfile.users_id ){
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Username already taken")
                }
                else if (existingEmail != null && existingEmail.users_id != userProfile.users_id ){
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Email already taken")
                }
                else {
                    val encoder = BCryptPasswordEncoder(16)
                    val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}".toRegex()
                    val emailPattern = "^[^@]+@[^@]+\\.[^@]+\$".toRegex()
                    if(!userEdit.email.matches(emailPattern)){
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Email wrong pattern")
                    }
                    else if (!userEdit.password.matches(passwordPattern)){
                        throw ResponseStatusException(HttpStatus.BAD_REQUEST,"Password wrong pattern")
                    }
                    else {
                        userProfile.users_firstname = userEdit.firstname
                        userProfile.users_lastname = userEdit.lastname
                        userProfile.username = userEdit.username
                        userProfile.users_gender = userEdit.gender
                        userProfile.users_email = userEdit.email
                        userProfile.users_phone = userEdit.phone
                        userProfile.users_birthday = userEdit.birthday
                        userProfile.password = encoder.encode(userEdit.password)
                        val savedUser = userRepository.save(userProfile)
                        return savedUser
                    }
                }
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND,"User not found")
            }
        } catch (e: ResponseStatusException){
            throw e
        }
    }

    fun getUserProfile(username: String): User?{
        try {
            return userRepository.findByUsername(username)
        } catch (e: Exception){
            throw ResponseStatusException(HttpStatus.NOT_FOUND,"User not found")
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

    fun toEditUserDto(user: User): EditUserDTO{
        return EditUserDTO(
            firstname = user.users_firstname,
            lastname = user.users_lastname,
            username = user.username,
            gender = user.users_gender,
            email = user.users_email,
            phone = user.users_phone,
            birthday = user.users_birthday,
            password = user.password
        )
    }

}

