plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.grammarkit") version "2021.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
}


allprojects {

    group = "ru.sbtqa.tag"
    version = "1.5.11"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
        testImplementation("junit:junit:4.13.1")
        implementation("io.cucumber:cucumber-expressions:3.0.0")
        implementation("io.cucumber:cucumber-core:1.2.6")
        implementation("io.cucumber:gherkin:3.2.0")
    }
}

intellij {
    version.convention("2023.2")
    type.set("IC")
    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    sourceSets {
        this.getByName("main") {
            this.java.srcDir("src/")
            this.resources.srcDir("resources/")
        }
        this.getByName("test") {
            this.java.srcDir("test/")
        }
    }

    patchPluginXml {
        changeNotes.set("""
      Support for 2023.2""")
    }
}
