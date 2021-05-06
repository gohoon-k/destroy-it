plugins {
    java
    kotlin("jvm") version "1.4.10"
    id("idea")
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "com.herokun.plugins"
version = "1.0.0.82"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")
}
