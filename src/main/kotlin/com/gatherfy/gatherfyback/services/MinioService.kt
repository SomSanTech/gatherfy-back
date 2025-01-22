package com.gatherfy.gatherfyback.services

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import io.minio.http.Method

@Service
class MinioService(
    private val minioClient: MinioClient,
//    @Value("\${minio.bucket}") private val bucket: String
) {
    fun uploadFile(bucket: String, file: MultipartFile): String {
//        val fileName = "${System.currentTimeMillis()}-${file.originalFilename}"
        val fileName = "${file.originalFilename}"

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

    fun deleteFile(bucket: String, objectName: String): Boolean {
        return try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
}
