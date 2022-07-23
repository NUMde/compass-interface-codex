import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

buildscript {
    repositories {
        mavenCentral() // For the ProGuard Gradle Plugin and anything else.
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.1")  // The ProGuard Gradle plugin.
    }
}

val projectVersion: String by project
group = "de.unimuenster.imi.medic"
version = projectVersion

repositories {
    mavenCentral()
}

val hapiVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.github.microutils:kotlin-logging:2.1.21")

    implementation(project("compass-download-kotlin"))
    implementation(project("gecco-questionnaire"))
    implementation(project("gecco-easy"))

    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")

    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = (project.properties["mainClass"] ?: "MainKt") as String
}

tasks.register<proguard.gradle.ProGuardTask>("minimizedJar") {
    dependsOn("shadowJar")
    verbose()

    injars("$buildDir/libs/compass-interface-codex-cli.jar") //TODO
    outjars("$buildDir/libs/compass-interface-codex-cli.min2.jar")

    val javaHome = System.getProperty("java.home")
    if (System.getProperty("java.version").startsWith("1.")) {
        libraryjars("$javaHome/lib/rt.jar")
    } else {
        for (module in listOf(
            "java.base", "jdk.xml.dom", "jdk.jsobject", "java.xml", "java.desktop",
            "java.datatransfer", "java.logging", "java.management", "java.naming", "java.net.http",
            "java.xml.crypto", "java.sql", "java.scripting"
        )) {
            libraryjars(
                mapOf("jarfilter" to "!**.jar", "filter" to "!module-info.class"),
                "$javaHome/jmods/$module.jmod"
            )
        }
    }
    printmapping("$buildDir/proguard-mapping.txt")
    configuration("proguard-rules.pro")
}


tasks {
    shadowJar {
        archiveFileName.set("compass-interface-codex-cli.jar")
    }
}