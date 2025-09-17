package com.sorsix.externalapi

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@SpringBootApplication
class ExternalApiApplication

fun main(args: Array<String>) {
    runApplication<ExternalApiApplication>(*args)
}

@RestController
@RequestMapping("/api/external")
class ExternalApiController {

    val executor = Executors.newSingleThreadScheduledExecutor()
    val random = Random()
    val logger = LoggerFactory.getLogger(ExternalApiController::class.java)

    /**
     * {probability} in range of 0-100
     */
    @GetMapping("/timeout/{probability}")
    fun timeout(@PathVariable probability: Int): String {
        val waitTime = if (Math.random() < probability / 100.0) {
            logger.info("Timeout")
            30_000
        } else {
            0.0.coerceAtLeast(random.nextGaussian(50.0, 25.0))
        }
        Thread.sleep(waitTime.toLong())
        return "waited: $waitTime"
    }

    /**
     * {rate} Rate of requests per second
     */
    @GetMapping("/rate-limit/{rate}")
    fun rateLimit(@PathVariable rate: Int): String {
        val result = executor.submit(
            Callable {
                Thread.sleep(1000 / rate.toLong())
                logger.info("Executing request with rate [{}]r/s", rate)
                Math.random()
            }
        )
        return "result: ${result.get()}"
    }

    /**
     * {rate} from 0-100 means at what second from the minute the API will return error.
     * Ex. 25% means, the first 15 seconds of each minute the API will return error.
     */
    @GetMapping("/error/{rate}")
    fun error(@PathVariable rate: Int): ResponseEntity<String> {
        val errorTime = (rate * 60) / 100
        val second = LocalDateTime.now().second
        return if (second < errorTime) {
            Thread.sleep(3000 + (Math.random() * 500).toLong())
            logger.warn("Generating error [{}] second", second)
            ResponseEntity.internalServerError().body("error: $second/$errorTime")
        } else {
            ResponseEntity.ok(second.toString())
        }
    }

}
