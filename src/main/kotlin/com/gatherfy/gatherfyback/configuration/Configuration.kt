package com.gatherfy.gatherfyback.configuration

import com.gatherfy.gatherfyback.properties.JwtProperties
import com.gatherfy.gatherfyback.properties.MailSenderProperties
import com.gatherfy.gatherfyback.repositories.UserRepository
import com.gatherfy.gatherfyback.services.CustomUserDetailsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.client.RestTemplate
import java.util.*


@Configuration
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(JwtProperties::class, MailSenderProperties::class)
class Configuration(private val mailSenderProperties: MailSenderProperties) {
    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService =
        CustomUserDetailsService(userRepository)

    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(userRepository: UserRepository): AuthenticationProvider =
        DaoAuthenticationProvider().also {
            it.setUserDetailsService(userDetailsService(userRepository))
            it.setPasswordEncoder(encoder())
        }
    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun javaMailSender(mailSenderProperties: MailSenderProperties): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = mailSenderProperties.host
        mailSender.port = mailSenderProperties.port
        mailSender.username = mailSenderProperties.username
        mailSender.password = mailSenderProperties.password

        configureJavaMailProperties(mailSender.javaMailProperties)
        return mailSender
    }

    private fun configureJavaMailProperties(properties: Properties) {
        properties["mail.transport.protocol"] = mailSenderProperties.protocol
        properties["mail.smtp.auth"] = mailSenderProperties.auth
        properties["mail.smtp.starttls.enable"] = mailSenderProperties.starttlsEnable
        properties["mail.debug"] = mailSenderProperties.debug
    }

//    // Google : OAuth2 setup
//    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
//    private lateinit var clientId: String
//
//    @Value("\${spring.security.oauth2.client.registration.google.client-secret}")
//    private lateinit var clientSecret: String
//    @Bean
//    fun clientRegistrationRepository(): ClientRegistrationRepository{
//        val googleClientRegistration = ClientRegistration.withRegistrationId("google")
//            .clientId(clientId)
//            .clientSecret(clientSecret)
//            .redirectUri("http://localhost:4040/login/oauth2/code/google")
//            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
//            .tokenUri("https://oauth2.googleapis.com/token")
//            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
//            .userNameAttributeName("sub")
//            .scope("openid","profile", "email", "https://www.googleapis.com/auth/user.birthday.read") // ✅ Correctly formatted
//            .clientName("Google")
//            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//            .build()
//        return InMemoryClientRegistrationRepository(googleClientRegistration)
//    }
//    @Bean
//    fun restTemplate(): RestTemplate {
//        return RestTemplate()
//    }
}