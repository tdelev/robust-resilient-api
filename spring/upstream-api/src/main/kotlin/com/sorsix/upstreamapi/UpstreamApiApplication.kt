package com.sorsix.upstreamapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @GetMapping("/slow")
    fun slow(): String {
        val waitTime = 3000 + (Math.random() * 2000).toLong()
//        Thread.sleep(waitTime)
        return "waited: $waitTime"
    }

    @GetMapping("/per-second/{rate}")
    fun perSecond(@PathVariable rate: Int): String {
        val result = executor.submit(
            Callable {
                Thread.sleep(1000 / rate.toLong())
                Math.random()
            }
        )
        return "result: ${result.get()}"
    }

}
