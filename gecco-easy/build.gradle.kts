plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "de.unimuenster.imi.medic"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

val hapiVersion = "5.4.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
}




publishing {
    publications {
        create<MavenPublication>("mavenJava")  {
            from(components["java"])
            pom {
                name.set("gecco-easy")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Johannes Oehm")
                        email.set("johannes.oehm@uni-muenster.de")
                    }
                }
//                scm {
//                    connection = 'scm:git:git://example.com/my-library.git'
//                    developerConnection = 'scm:git:ssh://example.com/my-library.git'
//                    url = 'http://example.com/my-library/'
//                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

