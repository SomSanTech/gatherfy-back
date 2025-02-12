package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.dtos.CreateUserDTO
import com.gatherfy.gatherfyback.dtos.EditUserDTO
import com.gatherfy.gatherfyback.dtos.UserDTO
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder
) {

    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun createUser(userDto: CreateUserDTO): UserDTO {
        try{
            val existingUsername = userRepository.findByUsername(userDto.username)
            val existingEmail = userRepository.findByEmail(userDto.email)
            if(existingUsername != null){
                throw ConflictException("Username already taken")
            }
            else if (existingEmail != null){
                throw ConflictException("Email already taken")
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
                        users_image = null,
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
        } catch (e: BadRequestException){
            throw BadRequestException(e.message)
        } catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }

    fun updateUser(username: String, userEdit: EditUserDTO): User {
        try{
            val userProfile = userRepository.findByUsername(username)
            if(userProfile === null){
                throw EntityNotFoundException("User not found")
            }
            if(!userEdit.username.isNullOrBlank()){
                val duplicateUsername = userRepository.findByUsername(userEdit.username!!)
                if(duplicateUsername != null && duplicateUsername.users_id != userProfile.users_id ){
                    throw ConflictException("Username already taken")
                }
            }
            if(!userEdit.email.isNullOrBlank()){
                val emailPattern = "^[^@]+@[^@]+\\.[^@]+\$".toRegex()
                val duplicateEmail = userRepository.findByEmail(userEdit.email!!)
                if (duplicateEmail != null && duplicateEmail.users_id != userProfile.users_id ){
                    throw ConflictException("Email already taken")
                }
                else if(!userEdit.email!!.matches(emailPattern)){
                    throw BadRequestException("Email should match pattern abc@example.com")
                }
            }
            if(!userEdit.password.isNullOrBlank()){
                val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=])(?=\\S+\$).{8,}".toRegex()
                if (!userEdit.password!!.matches(passwordPattern)){
                    throw BadRequestException("Password does not match pattern")
                }
            }
            val encoder = BCryptPasswordEncoder(16)

            val updateUser = userProfile.copy(
                users_firstname = userEdit.firstname ?: userProfile.users_firstname,
                users_lastname = userEdit.lastname ?: userProfile.users_lastname,
                username = userEdit.username ?: userProfile.username,
                users_gender = userEdit.gender ?: userProfile.users_gender,
                users_email = userEdit.email ?: userProfile.users_email,
                users_phone = userEdit.phone ?: userProfile.users_phone,
                users_image = userEdit.image ?: userProfile.users_image,
                users_birthday = userEdit.birthday ?: userProfile.users_birthday,
                password = userEdit.password?.let { encoder.encode(it) } ?: userProfile.password
            )
            val updatedUser = userRepository.save(updateUser)
            return updatedUser
        } catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        } catch (e: ConflictException){
            throw ConflictException(e.message!!)
        } catch (e: BadRequestException){
            throw BadRequestException(e.message)
        }
    }

    fun getUserProfile(username: String): User?{
        try {
            val user = userRepository.findByUsername(username)
            if(user != null){
                user.users_image = getImageUrl("profiles",user.users_image!!)
            }
            return user
        } catch (e: Exception){
            throw EntityNotFoundException("User not found")
        }
    }

    fun toUserDto(user: User): UserDTO{
        return UserDTO(
            username = user.username,
            email = user.users_email,
            phone = user.users_phone,
            role = user.users_role,
//            image = getImageUrl("profiles", user.users_image!!)
        )
    }

    fun getImageUrl(bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }
}

