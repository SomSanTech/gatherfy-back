package com.gatherfy.gatherfyback.Exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class AccessDeniedException(message: String): RuntimeException(message) {
}