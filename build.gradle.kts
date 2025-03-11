import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
        id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

group = "com.silverest"
version = "1.0-SNAPSHOTnishh"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4:models")

    implementation("org.yaml:snakeyaml:1.30")

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "art-scry"
            packageVersion = "1.0.0"
        }
    }
}
