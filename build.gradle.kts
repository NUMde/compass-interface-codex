import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    application
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

val projectVersion: String by project
group = "de.unimuenster.imi.medic"
version = projectVersion

repositories {
    mavenCentral()
}

val hapiVersion = "5.4.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("io.github.microutils:kotlin-logging:1.7.7")

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
