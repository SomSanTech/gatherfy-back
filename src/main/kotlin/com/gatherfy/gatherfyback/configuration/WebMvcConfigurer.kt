package com.gatherfy.gatherfyback.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
class CorsConfiguration : WebMvcConfigurer {
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = org.springframework.web.cors.CorsConfiguration()

        // อนุญาตเฉพาะ origin ที่กำหนด
        configuration.allowedOrigins = listOf("http://localhost:3000","https://capstone24.sit.kmutt.ac.th/us1") // เปลี่ยนเป็น origin จริงเมื่ออยู่ใน production

        // อนุญาต credentials (สำหรับส่งคุกกี้, authorization headers, เป็นต้น)
        configuration.allowCredentials = true

        // อนุญาต HTTP methods ที่ระบุ
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // อนุญาต headers ที่ระบุ
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")

        // ตั้งค่า headers ที่จะถูกส่งกลับใน response
        configuration.exposedHeaders = listOf("Authorization")

        // นำการตั้งค่าไปใช้กับทุก path
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}