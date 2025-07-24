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

//    val scn = scenario("Front Endpoint Load Test").exec(
//        http("timeout")
//            .get("/api/front/timeout/5/db")
//            .check(status().`is`(200))
//    )

//    val scn = scenario("Front Endpoint Load Test").exec(
//        http("rate-per-second")
//            .get("/api/front/rate/5")
//            .check(status().`is`(200))
//    )

    val scn = scenario("Front Endpoint Load Test").exec(
        http("error-rate")
            .get("/api/front/error/20")
            .check(status().`is`(200))
    )

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
