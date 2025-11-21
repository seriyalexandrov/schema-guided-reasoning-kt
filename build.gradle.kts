plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example.sgr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.openai:openai-java:0.8.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    implementation("com.github.victools:jsonschema-generator:4.38.0")
    implementation("com.github.victools:jsonschema-module-jackson:4.38.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

springBoot {
    mainClass.set("com.example.sgr.ApplicationKt")
}
