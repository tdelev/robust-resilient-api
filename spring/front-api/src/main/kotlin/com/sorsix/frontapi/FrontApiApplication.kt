package com.sorsix.frontapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.Duration
import java.time.LocalDateTime

@SpringBootApplication
class FrontApiApplication

fun main(args: Array<String>) {
    runApplication<FrontApiApplication>(*args)
}

@RestController
@RequestMapping("/api/front")
class FrontApiController(restTemplateBuilder: RestTemplateBuilder) {

    val restTemplate: RestTemplate = restTemplateBuilder
        .readTimeout(Duration.ofSeconds(5))
        .build()
    val UPSTREAM_URL = "http://localhost:8081/api/upstream"

    @GetMapping("/timeout/{probability}")
    fun timeout(@PathVariable probability: Int): Map<String, Any> {
        val upstream = restTemplate.getForObject<String>("$UPSTREAM_URL/timeout/$probability")
        return mapOf("time" to LocalDateTime.now(), "upstream" to upstream)
    }

    @GetMapping("/rate/{rate}")
    fun ratePerSecond(@PathVariable rate: Int): Map<String, Any> {
        val upstream = restTemplate.getForObject<String>("$UPSTREAM_URL/per-second/$rate")
        return mapOf("time" to LocalDateTime.now(), "upstream" to upstream)
    }
}
