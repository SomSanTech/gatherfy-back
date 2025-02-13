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
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        try {
            val fileName = minioService.uploadFile(file)
            val fileUrl = minioService.getFileUrl(fileName)
            val response = mapOf(
                "message" to "File uploaded successfully",
                "fileUrl" to fileUrl
            )
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf("error" to "File upload failed: ${e.message}"))
        }
    }

    @DeleteMapping("delete/{fileName}")
    fun deleteFile(@PathVariable("fileName") fileName: String) {
        minioService.deleteFile(fileName)
    }
}
