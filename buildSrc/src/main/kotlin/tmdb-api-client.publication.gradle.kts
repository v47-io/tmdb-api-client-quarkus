import internal.ossrh

plugins {
    `java-base`
    `maven-publish`

    signing
    id("name.remal.maven-publish-ossrh")
}

val packageJavadoc = tasks.register("packageJavadoc", Jar::class.java) {
    archiveClassifier = "javadoc"

    val dokkaJavadoc = tasks.getByName("dokkaJavadoc")
    dependsOn(dokkaJavadoc)
    from(dokkaJavadoc.outputs)
}

val packageSources = tasks.register("packageSources", Jar::class.java) {
    archiveClassifier = "sources"
    from(sourceSets.main.get().allSource)
}

artifacts {
    add("archives", packageSources)
}

publishing {
    publications {
        create<MavenPublication>("tmdbApiClient") {
            groupId = "${project.group}"
            artifactId = project.name
            version = "${project.version}"

            from(components["java"])

            artifact(packageJavadoc) { classifier = "javadoc" }
            artifact(packageSources) { classifier = "sources" }

            pom {
                name = "TMDB API Client :: Quarkus ${project.name}"
                url = "https://github.com/v47-io/tmdb-api-client-quarkus"

                licenses {
                    license {
                        name = "BSD 3-Clause Clear License"
                        url = "https://spdx.org/licenses/BSD-3-Clause-Clear.html"
                    }
                }

                developers {
                    developer {
                        id = "vemilyus"
                        name = "Alex Katlein"
                        email = "dev@vemilyus.com"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/v47-io/tmdb-api-client-quarkus.git"
                    developerConnection =
                        "scm:git:ssh://git@github.com/v47-io/tmdb-api-client-quarkus.git"
                    url = "https://github.com/v47-io/tmdb-api-client-quarkus"
                }
            }
        }

        val ossrhUser = project.findProperty("ossrhUser")?.toString() ?: System.getenv("OSSRH_USER")
        val ossrhPass = project.findProperty("ossrhPass")?.toString() ?: System.getenv("OSSRH_PASS")

        if (ossrhUser != null && ossrhPass != null) {
            repositories {
                ossrh {
                    credentials {
                        username = ossrhUser
                        password = ossrhPass
                    }
                }
            }
        }
    }
}
