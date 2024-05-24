plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.0"
    application
    `maven-publish`
}
val projectVersion: String by project
group = "de.unimuenster.imi.medic"
version = projectVersion

repositories {
    mavenCentral()
}

val ktor_version: String by project
val kotlinLoggingVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
    implementation("org.bouncycastle:bcmail-jdk18on:1.77")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

application {
    mainClass.set("MainKt")
}

