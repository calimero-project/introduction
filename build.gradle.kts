import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.2.0"
	application
	id("com.github.ben-manes.versions") version "0.52.0"
	`maven-publish`
	signing
}

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://central.sonatype.com/repository/maven-snapshots/")
}

application {
	// specify an example you want to run
	mainClass = System.getProperty("mainClass") ?: "DiscoverKnxServers"
}

// specify an example we want to run
tasks.withType<JavaExec> {
	@Suppress("UNCHECKED_CAST")
	systemProperties(System.getProperties() as Map<String, *>)
}

version = "3.0-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

tasks.compileKotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.compilerArgs.addAll(listOf(
		"-Xlint:all",
		"-Xlint:-options"
	))
}

dependencies {
	implementation("io.calimero:calimero-core:$version")
	implementation("io.calimero:calimero-device:$version")
	runtimeOnly("io.calimero:calimero-rxtx:$version")
	runtimeOnly("io.calimero:calimero-usb:$version")

	runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.17")
	runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
}
