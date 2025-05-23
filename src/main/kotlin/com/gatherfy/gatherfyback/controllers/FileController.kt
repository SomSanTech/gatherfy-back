package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.services.MinioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class FileController(private val minioService: MinioService) {

    @PostMapping("/upload")
    fun uploadFile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("bucket") bucket: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        val fileName = minioService.uploadFile(bucket, file)
        val fileUrl = minioService.getFileUrl(bucket, fileName)
        val response = mapOf(
            "message" to "File uploaded successfully",
            "fileName" to fileName,
            "fileUrl" to fileUrl
        )
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("delete/{fileName}")
    fun deleteFile(
        @RequestHeader("Authorization") token: String,
        @RequestParam("bucket") bucket: String,
        @PathVariable("fileName") fileName: String) {
        minioService.deleteFile(bucket, fileName)
    }
}
