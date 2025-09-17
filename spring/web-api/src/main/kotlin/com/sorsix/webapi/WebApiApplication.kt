package com.sorsix.webapi

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import jakarta.persistence.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.Duration
import java.time.LocalDateTime

@SpringBootApplication
class WebApiApplication

fun main(args: Array<String>) {
    runApplication<WebApiApplication>(*args)
}

@RestController
@RequestMapping("/api/web")
class WebApiController(
    restTemplateBuilder: RestTemplateBuilder,
    val repository: DogRepository,
    val transactionTemplate: TransactionTemplate,
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val restTemplate: RestTemplate = restTemplateBuilder
//        .connectTimeout(Duration.ofSeconds(10))
//        .readTimeout(Duration.ofSeconds(5))
        .build()

    val EXTERNAL_API_URL = "http://localhost:8081/api/external"

    @GetMapping("/timeout/{probability}")
    fun timeout(@PathVariable probability: Int): Map<String, Any> {
        logger.info("Timeout [{}]", probability)
        val external = try {
            restTemplate.getForObject<String>("$EXTERNAL_API_URL/timeout/$probability")
        } catch (e: Exception) {
            logger.warn("Error accessing external API [{}]", e.message, e)
            "timeout"
        }
        return mapOf("time" to LocalDateTime.now(), "external" to external)
    }

    @GetMapping("/rate-limit/{rate}")
    fun rateLimit(@PathVariable rate: Int): ResponseEntity<*> {
        logger.info("Rate per second [{}]r/s", rate)
        val external = try {
            rateLimiter.executeCallable {
                restTemplate.getForObject<String>("$EXTERNAL_API_URL/rate-limit/$rate")
            }
        } catch (e: Exception) {
            logger.warn("Error accessing external API [{}]", e.message, e)
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("rate-error")
        }
        return ResponseEntity.ok(mapOf("time" to LocalDateTime.now(), "external" to external))
    }

    @GetMapping("/timeout/{probability}/db")
    fun timeoutWithDb(@PathVariable probability: Int): Map<String, Any> {
        logger.info("Timeout with DB read [{}]", probability)
        val dogs = repository.findAll()
        val external = try {
            restTemplate.getForObject<String>("$EXTERNAL_API_URL/timeout/$probability")
        } catch (e: Exception) {
            logger.warn("Error accessing external API [{}]", e.message, e)
            "timeout-error"
        }
        return mapOf("time" to LocalDateTime.now(), "external" to external, "dogs" to dogs)
    }

    @GetMapping("/timeout/{probability}/db_transaction")
    fun timeoutWithDbTransaction(@PathVariable probability: Int): Map<String, Any?> {
        logger.info("Timeout with DB transaction [{}]", probability)
        val dog = try {
            transactionTemplate.execute {
                val dogName = restTemplate.getForObject<String>("$EXTERNAL_API_URL/timeout/$probability")
                repository.save(Dog(name = dogName))
            }
        } catch (e: Exception) {
            logger.warn("Error accessing external API [{}]", e.message, e)
            "timeout-error"
        }
        return mapOf("time" to LocalDateTime.now(), "dog" to dog)
    }

    @GetMapping("/error/{rate}")
    fun errorRate(@PathVariable rate: Int): ResponseEntity<*> {
        logger.info("Error rate [{}]", rate)
        val external = try {
//            circuitBreaker.executeCallable {
            expensiveCallThatCanFail(rate)
//            }
        } catch (e: Exception) {
            logger.warn("Error accessing external API [{}]", e.message, e)
            return ResponseEntity.internalServerError().body("external-error: ${e.message}")
        }
        return ResponseEntity.ok(mapOf("time" to LocalDateTime.now(), "external" to external))
    }

    @GetMapping("/dogs")
    fun getDogs(): List<Dog> = repository.findAll()

    val config = RateLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(1))
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .limitForPeriod(5)
        .build()

    val rateLimiterRegistry = RateLimiterRegistry.of(config)

    val rateLimiter = rateLimiterRegistry.rateLimiter("5_request_per_second")

    /**
     * Real world examples:
     *
     * - email service which might be slow or unavailable
     *
     */
    val circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(50f)
        .waitDurationInOpenState(Duration.ofSeconds(5))
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(4)
        .slowCallDurationThreshold(Duration.ofMillis(1000))
        .build()

    val circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)

    val circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit_breaker")

    private fun expensiveCallThatCanFail(rate: Int): String {
        matrixMultiply(500)
        return restTemplate.getForObject<String>("$EXTERNAL_API_URL/error/$rate")
    }

    // CPU-intensive matrix multiplication
    fun matrixMultiply(size: Int): Array<IntArray> {
        val a = Array(size) { IntArray(size) { (1..100).random() } }
        val b = Array(size) { IntArray(size) { (1..100).random() } }
        val result = Array(size) { IntArray(size) }

        for (i in 0 until size) {
            for (j in 0 until size) {
                for (k in 0 until size) {
                    result[i][j] += a[i][k] * b[k][j]
                }
            }
        }
        return result
    }

}

@Entity
@Table(name = "dogs")
data class Dog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
)

interface DogRepository : JpaRepository<Dog, Long>
