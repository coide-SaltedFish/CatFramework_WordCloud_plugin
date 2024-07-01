import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.sereinfish.catcat.framework.word.cloud"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.sereinfish.catcat.frame:CatFrame:0.0.197")
    implementation("org.slf4j:slf4j-api:2.0.12")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    implementation("com.kennycason:kumo-core:1.28")
    implementation("com.kennycason:kumo-tokenizers:1.28")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes["CatPluginId"] = "org.sereinfish.catcat.framework.word.cloud"
    }
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}