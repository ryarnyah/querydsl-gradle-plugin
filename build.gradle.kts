plugins {
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.12.0"
    id("java-gradle-plugin")
}

version = getGitVersion()
group = "com.github.ryarnyah"

repositories {
    mavenCentral()
    jcenter()
}

pluginBundle {
    website = "https://github.com/ryarnyah/${project.name}"
    vcsUrl = "https://github.com/ryarnyah/${project.name}"
    tags = listOf("querydsl", "plugin", "gradle", "sql", "jdo", "jpa")
}

dependencies {
    implementation(kotlin("stdlib", "1.3.72"))

    implementation("jakarta.persistence:jakarta.persistence-api:2.2.3")
    implementation("javax.jdo:jdo2-api:2.3-eb")
    implementation("com.querydsl:querydsl-sql-codegen:4.4.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

gradlePlugin {
    plugins {
        create("querydsl") {
            id = "com.github.ryarnyah.querydsl"
            implementationClass = "com.github.ryarnyah.querydsl.GradleQueryDSLPlugin"
            displayName = "QueryDSL plugin for Gradle"
            description = "Port of QueryDSL maven plugin for Gradle"
        }
    }
}

setupPublishingEnvironment()

java {
    sourceCompatibility = JavaVersion.toVersion("11")

    withJavadocJar()
    withSourcesJar()
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}


// Tooling
fun getGitVersion(defaultVersion: String = "0.0.1"): String {
    var gitLastTag = "git describe --abbrev=0 --tags".runCommand()
    if (gitLastTag.isEmpty()) {
        gitLastTag = defaultVersion
    }
    val gitCurrentTag = "git describe --exact-match --tags HEAD".runCommand()
    return gitLastTag + (if (gitCurrentTag != gitLastTag) "-SNAPSHOT" else "")
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

fun setupPublishingEnvironment() {
    val keyEnvironmentVariable = "GRADLE_PUBLISH_KEY"
    val secretEnvironmentVariable = "GRADLE_PUBLISH_SECRET"

    val keyProperty = "gradle.publish.key"
    val secretProperty = "gradle.publish.secret"

    if (System.getProperty(keyProperty) == null || System.getProperty(secretProperty) == null) {
        logger
            .info("`$keyProperty` or `$secretProperty` were not set. Attempting to configure from environment variables")

        val key: String? = System.getenv(keyEnvironmentVariable)
        val secret: String? = System.getenv(secretEnvironmentVariable)
        if (key != null && secret != null) {
            System.setProperty(keyProperty, key)
            System.setProperty(secretProperty, secret)
        } else {
            logger.warn("Publishing key or secret was null")
        }
    }
}

inline val Project.isSnapshot
    get() = version.toString().endsWith("-SNAPSHOT")