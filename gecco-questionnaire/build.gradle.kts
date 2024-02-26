plugins {
    kotlin("jvm")
}

group = "de.unimuenster.imi"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

val hapiVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":gecco-easy"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
}

