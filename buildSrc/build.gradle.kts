import org.gradle.accessors.dm.LibrariesForLibs

private val Project.libs: LibrariesForLibs
    get() = extensions.getByType()

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.plugin.detekt)
    implementation(libs.plugin.dokka)
    implementation(libs.plugin.git.properties)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.kotlin.allopen)
    implementation(libs.plugin.kotlin.spring)
    implementation(libs.plugin.license)
    implementation(libs.plugin.quarkus)
    implementation(libs.plugin.quarkus.extension)
    implementation(libs.plugin.release)
    implementation(libs.remalGradlePlugins)
}
