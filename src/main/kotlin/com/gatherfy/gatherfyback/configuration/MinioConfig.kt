package com.gatherfy.gatherfyback.configuration

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig{
    @Bean
    fun minioClient(@Value("\${minio.url}") minioUrl: String,
                    @Value("\${minio.accessKey}") accessKey: String,
                    @Value("\${minio.secretKey}") secretKey: String,
                    @Value("\${spring.profiles.active}") activeProfile: String): MinioClient {
        return if (activeProfile == "prod") {
            MinioClient.builder()
                .endpoint(minioUrl, 9000, false) // Production configuration
                .credentials(accessKey, secretKey)
                .build()
        } else {
            MinioClient.builder()
                .endpoint(minioUrl) // Staging configuration
                .credentials(accessKey, secretKey)
                .build()
        }
    }
}