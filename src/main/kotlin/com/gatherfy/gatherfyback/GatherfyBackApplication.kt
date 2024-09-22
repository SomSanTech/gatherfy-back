package com.gatherfy.gatherfyback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class GatherfyBackApplication

fun main(args: Array<String>) {
	runApplication<GatherfyBackApplication>(*args)
}

@RestController
class MessageController {
	@GetMapping("/")
	fun index() = "Hello!"
}
