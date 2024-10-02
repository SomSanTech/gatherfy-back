package com.gatherfy.gatherfyback

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.Normalizer

@SpringBootApplication
class GatherfyBackApplication

fun main(args: Array<String>) {
	runApplication<GatherfyBackApplication>(*args)
}

fun slugify(word: String, replacement: String = "-") = Normalizer
	.normalize(word, Normalizer.Form.NFD)
	.replace("[^\\p{ASCII}]".toRegex(), "")
	.replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
	.replace("\\s+".toRegex(), replacement)
	.toLowerCase()

