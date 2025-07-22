plugins {
    kotlin("jvm") version "2.1.20"
    id("io.gatling.gradle") version "3.14.3.3"
}

group = "com.sorsix"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
