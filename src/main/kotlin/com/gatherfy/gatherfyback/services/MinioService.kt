package com.gatherfy.gatherfyback.services

import io.minio.*
import io.minio.errors.ErrorResponseException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import io.minio.http.Method
import jakarta.persistence.EntityNotFoundException

@Service
class MinioService(
    private val minioClient: MinioClient,
) {
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
            if(!isObjectExist(bucket, objectName)){
                throw EntityNotFoundException("File $objectName is not exist")
            }
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
}
