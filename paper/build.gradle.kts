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
    id("java-library")
    id("com.github.johnrengelman.shadow")
}

val apiVersion: String by rootProject.extra

dependencies {
    implementation(project(":core"))
    implementation("ch.andre601.asl-api:platform-bukkit:$apiVersion")
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.viaversion:viaversion-api:4.9.2")
}

tasks.withType<JavaCompile>().configureEach { dependsOn(project(":core").tasks.named("jar")) }

tasks.shadowJar {
    archiveBaseName.set("AdvancedServerList")
    archiveClassifier.set("")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    relocate("org.bstats", "ch.andre601.advancedserverlist.spigot.depends.bstats")
    dependsOn(project(":core").tasks.named("shadowJar"))
    from(
        project(":core").tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").map {
            zipTree(it.archiveFile)
        }
    )
}

tasks.build { dependsOn(tasks.shadowJar) }
