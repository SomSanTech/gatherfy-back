plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.gatherfy"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("mysql:mysql-connector-java:8.0.33")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.modelmapper:modelmapper:3.1.1")
	implementation("io.minio:minio:8.5.12")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
	implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation:3.4.2")
	implementation("jakarta.validation:jakarta.validation-api:3.1.0")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf:3.4.3")
	implementation("org.thymeleaf:thymeleaf-spring5:3.1.3.RELEASE")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") // ✅ Required for JWT authentication
	implementation("com.nimbusds:nimbus-jose-jwt:9.37.2") // ✅ Supports RS256 verification
	implementation ("org.json:json:20210307")
	implementation("org.apache.poi:poi-ooxml:5.0.0")
	runtimeOnly("com.mysql:mysql-connector-j")
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
