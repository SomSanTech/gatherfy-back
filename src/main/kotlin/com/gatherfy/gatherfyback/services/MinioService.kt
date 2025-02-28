package com.gatherfy.gatherfyback.services

import io.minio.*
import io.minio.errors.ErrorResponseException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import io.minio.http.Method
import jakarta.persistence.EntityNotFoundException
import java.net.HttpURLConnection
import java.net.URL

@Service
class MinioService(
    private val minioClient: MinioClient,
) {
    @Value("\${minio.domain}")
    private lateinit var minioDomain: String

    fun uploadFile(bucket: String, file: MultipartFile): String {
        try{
//            val fileName = "${System.currentTimeMillis()}-${file.originalFilename}"
            val fileName = "${file.originalFilename}"
            val isBucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
            if(!isBucketExist){
                throw EntityNotFoundException("Bucket $bucket is not exist")
            }
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(fileName)
                    .contentType(file.contentType)
                    .stream(file.inputStream, file.size, -1)
                    .build()
            )

            return fileName
        }
        catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun deleteFile(bucket: String, objectName: String): Boolean {
        return try {
            val isBucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
            if(!isBucketExist){
                throw EntityNotFoundException("Bucket $bucket is not exist")
            }
//            if(!isObjectExist(bucket, objectName)){
//                throw EntityNotFoundException("File $objectName is not exist")
//            }
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectName)
                    .build()
            )
            true
        }
        catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun isObjectExist(bucket: String, fileName: String): Boolean{
        try{
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).`object`(fileName).build())
            return true
        }catch (e: ErrorResponseException){
            return false
        }
    }

    fun getFileUrl(bucket: String, fileName: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .`object`(fileName)
                .method(Method.GET)
                .build()
        )
    }

    fun uploadImageFromUrl(bucket: String, imageUrl: String, fileNameWithoutExt: String): String{
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // ✅ Extract content type (e.g., "image/jpeg", "image/png")
        val contentType = connection.contentType ?: "image/jpeg" // Default to JPEG if unknown

        // ✅ Determine file extension based on content type
        val fileExtension = when (contentType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg" // Default to .jpg for unknown types
        }

        val fileName = fileNameWithoutExt + fileExtension

        connection.inputStream.use { inputStream ->
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(fileName)
                    .contentType(contentType)
                    .stream(inputStream, connection.contentLengthLong, -1)
                    .build()
            )
        }
        return fileName
    }

    fun getImageUrl(bucketName: String, objectName: String): String {
        return "$minioDomain/$bucketName/$objectName"
    }
}
