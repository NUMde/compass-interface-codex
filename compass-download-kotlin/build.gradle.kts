import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.20"
    application
    id("maven-publish")
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("org.bouncycastle:bcprov-jdk15on:1.68")
    implementation("org.bouncycastle:bcmail-jdk16:1.46")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.68")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}

