package com.gatherfy.gatherfyback.controllers

import com.gatherfy.gatherfyback.services.ReportService
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
@CrossOrigin(origins = ["http://cp24us1.sit.kmutt.ac.th:3000/","http://localhost:3000/"])
class ReportController(private val reportService: ReportService) {

    @PostMapping("/v1/report/{eventId}")
    fun generateReport(
        @RequestHeader("Authorization") token: String,
        @PathVariable eventId: Long)
    : ResponseEntity<ByteArrayResource> {
        val workbook: Workbook = XSSFWorkbook()

        // Call your report generation function here
        val reportBytes = reportService.generateReport(workbook, eventId)

        // Return the generated report as a downloadable file
        val resource = ByteArrayResource(reportBytes)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"feedback_report.xlsx\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(reportBytes.size.toLong())
            .body(resource)
    }
}