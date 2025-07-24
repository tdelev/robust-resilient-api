package com.sorsix.upstreamapi

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
class UpstreamApiApplication

fun main(args: Array<String>) {
    runApplication<UpstreamApiApplication>(*args)
}

@RestController
@RequestMapping("/api/upstream")
class UpstreamApiController {

    val executor = Executors.newSingleThreadScheduledExecutor()
    val random = Random()
    val logger = LoggerFactory.getLogger(UpstreamApiController::class.java)

    @GetMapping("/timeout/{probability}")
    fun timeoutProbability(@PathVariable probability: Int): String {
        val waitTime = if (Math.random() < probability / 100.0) {
            logger.info("Timeout")
            30_000
        } else {
            0.0.coerceAtLeast(random.nextGaussian(50.0, 25.0))
        }
        Thread.sleep(waitTime.toLong())
        return "waited: $waitTime"
    }

    @GetMapping("/per-second/{rate}")
    fun perSecond(@PathVariable rate: Int): String {
        val result = executor.submit(
            Callable {
                Thread.sleep(1000 / rate.toLong())
                logger.info("Executing request with rate [{}]r/s", rate)
                Math.random()
            }
        )
        return "result: ${result.get()}"
    }

    @GetMapping("/error/{rate}")
    fun withError(@PathVariable rate: Int): ResponseEntity<String> {
        val errorTime = (rate * 60) / 100
        val second = LocalDateTime.now().second
        return if (second < errorTime) {
            logger.warn("Generating error [{}] second", second)
            Thread.sleep((Math.random() * 10_000).toLong())
            ResponseEntity.internalServerError().build()
        } else {
            ResponseEntity.ok(LocalDateTime.now().second.toString())
        }
    }

}
