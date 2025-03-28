package com.gatherfy.gatherfyback.Exception

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException


//@ControllerAdvice
@RestControllerAdvice
class CustomExceptionHandle {

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNullPointerExceptions(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND // 404
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Not Found",
                e.message!!,
            ),
            status
        )
    }

    @ExceptionHandler(ConflictException::class)
    fun handleNullPointerExceptions(e: ConflictException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.CONFLICT
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Conflict",
                e.message!!,
            ),
            status
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<String> {
        return ResponseEntity.status(ex.statusCode).body(ex.reason)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST // 404
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Bad Request",
                "Invalid JSON input",
            ),
            status
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.badRequest().body(mapOf("error" to "Validation failed", "details" to errors))
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleIllegalArgument(e: BadRequestException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Bad Request",
                e.message!!,
            ),
            status
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.FORBIDDEN
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Forbidden",
                e.message!!,
            ),
            status
        )
    }

    @ExceptionHandler(CustomUnauthorizedException::class)
    fun handleUnauthorized(e: CustomUnauthorizedException): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNAUTHORIZED
        return ResponseEntity(
            ErrorResponse(
                status.value(),
                "Unauthorized",
                e.message!!,
            ),
            status
        )
    }

}