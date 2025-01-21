import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("relocation") {
            groupId = "io.v47.tmdb-api-client"
            version = "${project.version}"

            pom {
                distributionManagement {
                    relocation {
                        groupId = "${project.group}"
                        artifactId = project.name
                    }
                }
            }
        }
    }
}
