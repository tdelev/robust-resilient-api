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
class WebApiController(restTemplateBuilder: RestTemplateBuilder, val repository: DogRepository) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val restTemplate: RestTemplate = restTemplateBuilder
//        .readTimeout(Duration.ofSeconds(5))
        .build()

    val EXTERNAL_API_URL = "http://localhost:8081/api/external"

    @GetMapping("/timeout/{probability}")
    fun timeout(@PathVariable probability: Int): Map<String, Any> {
        logger.info("Timeout [{}]", probability)
        val external = try {
            restTemplate.getForObject<String>("$EXTERNAL_API_URL/timeout/$probability")
        } catch (e: Exception) {
            logger.warn("Error accessing upstream [{}]", e.message, e)
            "timeout"
        }
        return mapOf("time" to LocalDateTime.now(), "external" to external)
    }

    @GetMapping("/rate-limit/{rate}")
    fun rateLimit(@PathVariable rate: Int): Map<String, Any> {
        logger.info("Rate per second [{}]r/s", rate)
        val upstream = try {
            rateLimiter.executeCallable {
                restTemplate.getForObject<String>("$EXTERNAL_API_URL/rate-limit/$rate")
            }
        } catch (e: Exception) {
            logger.warn("Error accessing upstream [{}]", e.message, e)
            "rate-error"
        }
        return mapOf("time" to LocalDateTime.now(), "upstream" to upstream)
    }

    @GetMapping("/timeout/{probability}/db")
    fun timeoutWithDb(@PathVariable probability: Int): Map<String, Any> {
        logger.info("Timeout with db [{}]", probability)
        val dogs = repository.findAll()
        val upstream = try {
            restTemplate.getForObject<String>("$EXTERNAL_API_URL/timeout/$probability")
        } catch (e: Exception) {
            logger.warn("Error accessing upstream [{}]", e.message, e)
            "timeout-error"
        }
        return mapOf("time" to LocalDateTime.now(), "upstream" to upstream, "dogs" to dogs)
    }

    @GetMapping("/error/{rate}")
    fun errorRate(@PathVariable rate: Int): Map<String, Any> {
        logger.info("Error rate [{}]", rate)
        val upstream = try {
            circuitBreaker.executeCallable {
                restTemplate.getForObject<String>("$EXTERNAL_API_URL/error/$rate")
            }
        } catch (e: Exception) {
            logger.warn("Error accessing upstream [{}]", e.message, e)
            "rate-error"
        }
        return mapOf("time" to LocalDateTime.now(), "upstream" to upstream)
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
        .waitDurationInOpenState(Duration.ofSeconds(10))
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(4)
        .slowCallDurationThreshold(Duration.ofMillis(250))
        .build()

    val circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig)

    val circuitBreaker = circuitBreakerRegistry.circuitBreaker("circuit_breaker")

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
