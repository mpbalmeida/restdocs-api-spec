plugins {
    kotlin("jvm")
    signing
}
repositories {
    mavenCentral()
}

val springBootVersion: String by extra
val springRestDocsVersion: String by extra
val junitVersion: String by extra

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile(project(":restdocs-api-spec"))
    compile("org.springframework.restdocs:spring-restdocs-restassured:$springRestDocsVersion")

    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion") {
        exclude("junit")
    }
    testCompile("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:0.3.0")
    testCompile("org.springframework.boot:spring-boot-starter-hateoas:$springBootVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("REST Doc API Spec - REST Assured")
                description.set("Adds API specification support to Spring REST Docs ")
                url.set("https://github.com/mpbalmeida/restdocs-api-spec")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/mpbalmeida/restdocs-api-spec/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("marcos.almeida")
                        name.set("Marcos Almeida")
                        email.set("me@marcosalmeida.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/mpblamieda/restdocs-api-spec.git")
                    developerConnection.set("scm:git:ssh://github.com/mpbalmeida/restdocs-api-spec.git")
                    url.set("https://github.com/mpbalmeida/restdocs-api-spec")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

java {
    withJavadocJar()
    withSourcesJar()
}
