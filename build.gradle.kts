/*
  ~ MIT License
  ~ Copyright (c) 2022-2023 Andre_601
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy
  ~ of this software and associated documentation files (the "Software"), to deal
  ~ in the Software without restriction, including without limitation the rights
  ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  ~ copies of the Software, and to permit persons to whom the Software is
  ~ furnished to do so, subject to the following conditions:
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  ~ SOFTWARE.
*/

plugins {
    id("java")
    id("java-library")
    id("com.diffplug.spotless") version "7.0.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    eclipse
}

java { sourceCompatibility = JavaVersion.VERSION_17 }

group = "ch.andre601.advancedserverlist"
version = "4.0.0"

val apiVersion by extra("v3.2.0")

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.diffplug.spotless")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.GRAAL_VM)
        }
    }

    tasks.named<ProcessResources>("processResources") {
        val props = mapOf("version" to version, "apiVersion" to rootProject.extra["apiVersion"])
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
        from("${rootProject.projectDir}/LICENSE") { into("/") }
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io/") }
        maven { url = uri("https://codeberg.org/api/packages/Andre601/maven/") }
        maven { url = uri("https://repo.william278.net/releases/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
        maven { url = uri("https://repo.viaversion.com/") }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
        archiveClassifier.set("shadow")
        exclude("META-INF/*.SF", "META-INF/*.RSA")
        minimize()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:deprecation"))
        options.encoding = "UTF-8"
        options.isFork = true
    }

    tasks.build {
        dependsOn(tasks.spotlessApply, tasks.shadowJar)
    }

    spotless {
        java {
            removeUnusedImports()
            palantirJavaFormat()
        }
        kotlinGradle {
            ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
            target("*.gradle.kts")
        }
    }
}

tasks.jar { archiveClassifier.set("part") }

