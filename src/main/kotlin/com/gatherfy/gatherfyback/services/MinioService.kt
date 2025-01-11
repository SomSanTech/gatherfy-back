package com.gatherfy.gatherfyback.services

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import io.minio.http.Method

@Service
class MinioService(
    private val minioClient: MinioClient,
    @Value("\${minio.bucket}") private val bucket: String
) {
    fun uploadFile(file: MultipartFile): String {
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

    fun getFileUrl(fileName: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .`object`(fileName)
                .method(Method.GET)
                .build()
        )
    }
}
