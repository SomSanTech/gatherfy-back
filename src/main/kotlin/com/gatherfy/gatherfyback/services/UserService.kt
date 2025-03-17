package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.Exception.ConflictException
import com.gatherfy.gatherfyback.dtos.*
import com.gatherfy.gatherfyback.entities.OTPVerificationRequest
import com.gatherfy.gatherfyback.entities.ResendOTPRequest
import com.gatherfy.gatherfyback.entities.User
import com.gatherfy.gatherfyback.repositories.SocialRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.MethodParameter
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.server.ResponseStatusException
import java.lang.reflect.Method
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
    private val emailSenderService: EmailSenderService,
    private val tokenService: TokenService,
    private val minioService: MinioService,
    private val socialRepository: SocialRepository
) {

    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun createUser(userDto: CreateUserDTO): UserDTO {
        try{
            val existingUsername = userRepository.findByUsername(userDto.username!!)
            val existingEmail = userRepository.findByEmail(userDto.email!!)

            val bindingResult = BeanPropertyBindingResult(userDto, "userDto") // Collect validation errors

            if (existingUsername != null) {
                bindingResult.rejectValue("username", "USERNAME_INVALID", "Username already taken")
            }
            if (existingEmail != null) {
                bindingResult.rejectValue("email", "EMAIL_INVALID", "Email already taken")
            }

            val encoder = BCryptPasswordEncoder(16)
            val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=.*])(?=\\S+\$).{8,}".toRegex()
            val emailPattern = "^[^@]+@[^@]+\\.[^@]+(\\.[^@]+)*\$".toRegex()

            if (!userDto.email!!.matches(emailPattern)) {
                bindingResult.rejectValue("email","EMAIL_INVALID", "Email wrong pattern")
            }
            if (!userDto.password!!.matches(passwordPattern)) {
                bindingResult.rejectValue("password","PASSWORD_INVALID","Password wrong pattern")
            }

            if (bindingResult.hasErrors()) {
                val method: Method = this::class.java.getDeclaredMethod("createUser", CreateUserDTO::class.java)
                val methodParameter = MethodParameter(method, 0) // Create a valid MethodParameter
                throw MethodArgumentNotValidException(methodParameter, bindingResult)
            }
            else {
                val user = User(
                    users_id = null,
                    users_firstname = userDto.firstname!!,
                    users_lastname = userDto.lastname!!,
                    username = userDto.username!!,
                    users_gender = userDto.gender!!,
                    users_email = userDto.email!!,
                    users_phone = userDto.phone!!,
                    users_image = null,
                    users_role = userDto.role!!,
                    users_birthday = userDto.birthday!!,
                    users_age = null,
                    password = encoder.encode(userDto.password),
                    otp = generateOTP(),
                    is_verified = false,
                    otp_expires_at = LocalDateTime.now().plusMinutes(5),
                    auth_provider = "system",
                    email_new_events = true,
                    email_reminders_day = true,
                    email_reminders_hour = true,
                    email_updated_events = true
                )
                val savedUser = userRepository.save(user)
                emailSenderService.sendOtpVerification(savedUser)
                return toUserDto(savedUser)
            }
        } catch (e: ResponseStatusException){
            throw e
        } catch (e: BadRequestException){
            throw BadRequestException(e.message)
        } catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }

    fun createUserFromGoogle(createUserGoogle: CreateUserGoogleDTO): UserDTO {
        try{
            val accountDetail = tokenService.getAllClaimsFromToken(createUserGoogle.token)

            val profilePicture = minioService.uploadImageFromUrl("profiles", accountDetail!!["picture"].toString(),accountDetail["name"].toString())

            val user = User(
                users_id = null,
                users_firstname = accountDetail["given_name"].toString(),
                users_lastname = accountDetail["family_name"].toString(),
                username = accountDetail["name"].toString(),
                users_gender = null,
                users_email = accountDetail["email"].toString(),
                users_phone = null,
                users_image = profilePicture,
                users_role = createUserGoogle.role,
                users_birthday = null,
                users_age = null,
                password = null,
                otp = null,
                is_verified = true,
                otp_expires_at = null,
                auth_provider = "google",
                email_new_events = true,
                email_reminders_day = true,
                email_reminders_hour = true,
                email_updated_events = true
            )
            val savedUser = userRepository.save(user)
            return toUserDto(savedUser)
        } catch (e: ResponseStatusException){
            throw e
        } catch (e: BadRequestException){
            throw BadRequestException(e.message)
        } catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }

    fun generateOTP(): String {
        return (100000..999999).random().toString()
    }

    fun resendOTP(resendOTPRequest: ResendOTPRequest): ResponseEntity<String> {
        try{
            val user = userRepository.findByEmail(resendOTPRequest.email) ?: throw BadRequestException("User not found")
            // Prevent resending OTP if the user is already verified
            if (user.is_verified) {
                throw BadRequestException("Email is already verified. No OTP required.")
            }
            // Prevent too many OTP requests (e.g., only allow a new OTP every 1 minute)
            val now = LocalDateTime.now()
            if (user.otp_expires_at != null && user.otp_expires_at!!.isAfter(now.minusMinutes(1))) {
                throw BadRequestException("Please wait before requesting a new OTP.")
            }
            user.otp = generateOTP()
            user.otp_expires_at = LocalDateTime.now().plusMinutes(5)
            userRepository.save(user)
            emailSenderService.sendOtpVerification(user) // Resend new OTP
            return ResponseEntity.ok("New OTP sent to your email.")
        }catch (e: BadRequestException){
            throw BadRequestException(e.message)
        }
    }

    fun verifyOTP(otpVerificationRequest: OTPVerificationRequest): ResponseEntity<String> {
        try{
            val user = userRepository.findByEmail(otpVerificationRequest.email) ?: throw BadRequestException("Email incorrect")

            if(user.is_verified){
                throw ConflictException("This email has already been verified")
            }

            if (user.otp_expires_at!!.isBefore(LocalDateTime.now())) {
                throw BadRequestException("OTP expired")
            }
            if (user.otp != otpVerificationRequest.otp) {
                throw BadRequestException("Incorrect OTP")
            }
            user.is_verified = true
            user.otp = null
            user.otp_expires_at = null
            userRepository.save(user)

            return ResponseEntity.ok("Email verified successfully!")
        }catch (e: BadRequestException){
            throw BadRequestException(e.message)
        }catch (e: ConflictException){
            throw ConflictException(e.message!!)
        }
    }

    fun updateUser(username: String, userEdit: EditUserDTO): User {
        try{
            val userProfile = userRepository.findByUsername(username)
            if(userProfile === null){
                throw EntityNotFoundException("User not found")
            }

            val bindingResult = BeanPropertyBindingResult(userEdit, "userEdit") // Collect validation errors
            val encoder = BCryptPasswordEncoder(16)

            if (bindingResult.hasErrors()) {
                val method: Method = this::class.java.getDeclaredMethod("createUser", CreateUserDTO::class.java)
                val methodParameter = MethodParameter(method, 0) // Create a valid MethodParameter
                throw MethodArgumentNotValidException(methodParameter, bindingResult)
            }

            if(!userEdit.username.isNullOrBlank()){
                val duplicateUsername = userRepository.findByUsername(userEdit.username!!)
                if(duplicateUsername != null && duplicateUsername.users_id != userProfile.users_id ){
                    bindingResult.rejectValue("username", "USERNAME_INVALID", "Username already taken")
                }
            }
            if(!userEdit.email.isNullOrBlank()){
                val emailPattern = "^[^@]+@[^@]+\\.[^@]+(\\.[^@]+)*\$".toRegex()
                val duplicateEmail = userRepository.findByEmail(userEdit.email!!)
                if (duplicateEmail != null && duplicateEmail.users_id != userProfile.users_id ){
                    bindingResult.rejectValue("email", "EMAIL_INVALID", "Email already taken")
                }
                else if(!userEdit.email!!.matches(emailPattern)){
                    bindingResult.rejectValue("email","EMAIL_INVALID", "Email wrong pattern")
                }
            }
            if(!userEdit.password.isNullOrBlank()){
                val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=.*])(?=\\S+\$).{8,}".toRegex()
                if (!userEdit.password!!.matches(passwordPattern)){
                    bindingResult.rejectValue("password","PASSWORD_INVALID","Password wrong pattern")
                }
            }

            val updateUser = userProfile.copy(
                users_firstname = userEdit.firstname ?: userProfile.users_firstname,
                users_lastname = userEdit.lastname ?: userProfile.users_lastname,
                username = userEdit.username ?: userProfile.username,
                users_gender = userEdit.gender ?: userProfile.users_gender,
                users_email = userEdit.email ?: userProfile.users_email,
                users_phone = userEdit.phone ?: userProfile.users_phone,
                users_image = userEdit.image ?: userProfile.users_image,
                users_birthday = userEdit.birthday ?: userProfile.users_birthday,
                password = userEdit.password?.let { encoder.encode(it) } ?: userProfile.password,
                email_new_events = userEdit.newEvents ?: userProfile.email_new_events,
                email_reminders_day = userEdit.remindersDay ?: userProfile.email_reminders_day,
                email_reminders_hour = userEdit.remindersHour ?: userProfile.email_reminders_hour,
                email_updated_events = userEdit.updatedEvents ?: userProfile.email_updated_events
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
        val user = userRepository.findByUsername(username)

        if(user != null){
            if(user.users_image !== null){
                user.users_image = getImageUrl("profiles",user.users_image!!)
            }
        }
        return user
    }

    fun getUserProfileWithSocials(username: String): ProfileDTO{
        val user = userRepository.findByUsername(username)
        val socials = socialRepository.findSocialsByUserId(user?.users_id!!).map { socail ->
            Social(
                socialPlatform = socail.socialPlatform,
                socialLink = socail.socialLink
            )
        }

        if(user != null){
            if(user.users_image !== null){
                user.users_image = getImageUrl("profiles",user.users_image!!)
            }
        }
        return ProfileDTO(
            userProfile = user,
            userSocials = socials
        )
    }

    fun toUserDto(user: User): UserDTO{
        return UserDTO(
            username = user.username,
            email = user.users_email,
            phone = user.users_phone,
            role = user.users_role,
            authProvider = user.auth_provider!!
//            image = getImageUrl("profiles", user.users_image!!)
        )
    }

    fun getImageUrl(bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }

    fun updatePassword(username: String, editPasswordDTO: EditPasswordDTO): ResponseEntity<String>{
        val encoder = BCryptPasswordEncoder(16)
        val user = userRepository.findByUsername(username)
        val passwordPattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=.*])(?=\\S+\$).{8,}".toRegex()
        if(verifyPassword(editPasswordDTO.currentPassword,user?.password!!)){
            if(!editPasswordDTO.newPassword.matches(passwordPattern)){
                throw BadRequestException("Password wrong pattern")
            }
            user.password = encoder.encode(editPasswordDTO.newPassword)
            userRepository.save(user)
        }
        return ResponseEntity.ok("Password updated successfully!")
    }

    fun verifyPassword(rawPassword: String, encodedPassword: String): Boolean{
        try{
            val encoder = BCryptPasswordEncoder(16)
            if(!encoder.matches(rawPassword,encodedPassword)){
                throw BadRequestException("Password not match")
            }
            return true
        }catch (e: BadRequestException){
            throw BadRequestException(e.message)
        }
    }
}

