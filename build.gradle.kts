import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.0.20"
	application
	id("com.github.ben-manes.versions") version "0.51.0"
	`maven-publish`
	signing
}

repositories {
	mavenLocal()
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

application {
	// specify an example you want to run
	mainClass = System.getProperty("mainClass") ?: "DiscoverKnxServers"
}

// specify an example we want to run
tasks.withType<JavaExec> {
	@Suppress("UNCHECKED_CAST")
	systemProperties(System.getProperties() as Map<String?, *>)
}

version = "2.6-rc1"

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
	implementation("com.github.calimero:calimero-core:$version")
	implementation("com.github.calimero:calimero-device:$version")
	runtimeOnly("com.github.calimero:calimero-rxtx:$version")
	runtimeOnly("io.calimero:serial-native:$version")
	runtimeOnly("io.calimero:calimero-usb:$version")
	runtimeOnly("org.slf4j:slf4j-jdk-platform-logging:2.0.12")
	runtimeOnly("org.slf4j:slf4j-simple:2.0.12")
}
