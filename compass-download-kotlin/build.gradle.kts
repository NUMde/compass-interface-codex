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
val ktor_version = "1.6.1"

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")

    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.github.microutils:kotlin-logging:1.7.7")

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

val imiUser: String? by project
val imiPassword: String? by project
if(imiUser == null || imiPassword == null) {
    throw Exception("Please add gradle.properties file to your ~/.gradle folder and define imiUser and imiPassword there!")
}


publishing {

    publications {
        create<MavenPublication>("kotlin") {
            from(components["kotlin"])
        }
    }

    repositories {
        maven {
            name = "Test"//test
            url = uri("https://mvn.uni-muenster.de:443/artifactory/imi-test/")
            credentials {
                username = imiUser
                password = imiPassword
            }
        }
    }
}
