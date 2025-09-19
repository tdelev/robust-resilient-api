package com.sorsix.loadtesting

import io.gatling.javaapi.core.CoreDsl.incrementUsersPerSec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class BasicSimulation : Simulation() {

    val httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .userAgentHeader("gatling")
    val API_URL = "/api/web"

//    val scn = scenario("Timeout with 5% chance").exec(
//        http("timeout")
//            .get("$API_URL/timeout/5")
//            .check(status().`is`(200))
//    )

//    val scn = scenario("Timeout with DB queries").exec(
//        http("timeout")
//            .get("$API_URL/timeout/10/db")
//            .check(status().`is`(200))
//    )

//    val scn = scenario("External API is rate limited").exec(
//        http("rate-per-second")
//            .get("$API_URL/rate-limit/5")
//            .check(status().`is`(200))
//    )

//     val scn = scenario("Timeout with DB queries").exec(
//         http("timeout")
//             .get("$API_URL/timeout/10/db")
//             .check(status().`is`(200))
//     )
//    val scn = scenario("Front Endpoint Load Test").exec(
//        http("rate-per-second")
//            .get("$API_URL/rate/5")
//            .check(status().`is`(200))
//    )


    val scn = scenario("Front Endpoint Load Test").exec(
        http("error-rate")
            .get("$API_URL/error/50")
            .check(status().`is`(200))
    )

    /**
     * Start with 10 concurrent users and increment with rate of 5 users per second for 10 seconds.
     * Repeat this 3 times, and between each increment stay with constant load for 10 seconds.
     */
    init {
        setUp(
            scn.injectOpen(
                incrementUsersPerSec(5.0)
                    .times(3)
                    .eachLevelLasting(10)
                    .separatedByRampsLasting(10)
                    .startingFrom(10.0) // Double
            ).protocols(httpProtocol)
        )
    }

}
