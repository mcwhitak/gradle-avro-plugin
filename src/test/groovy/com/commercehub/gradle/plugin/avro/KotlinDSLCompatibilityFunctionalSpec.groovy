package com.commercehub.gradle.plugin.avro

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class KotlinDSLCompatibilityFunctionalSpec extends FunctionalSpec {
    File kotlinBuildFile

    def "setup"() {
        buildFile.delete() // Don't use the Groovy build file created by the superclass
        kotlinBuildFile = testProjectDir.newFile("build.gradle.kts")
        kotlinBuildFile << """
        |plugins {
        |    java
        |    id("com.commercehub.gradle.plugin.avro")
        |}
        |repositories {
        |    jcenter()
        |}
        |dependencies {
        |    implementation("org.apache.avro:avro:${avroVersion}")
        |}
        |""".stripMargin()
    }

    def "works with kotlin DSL"() {
        given:
        copyResource("user.avsc", avroDir)

        when:
        def result = run()

        then:
        result.task(":generateAvroJava").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("example/avro/User.class")).file
    }

    def "extension supports configuring all supported properties"() {
        given:
        copyResource("user.avsc", avroDir)
        kotlinBuildFile << """
        |avro {
        |    isCreateSetters.set(true)
        |    isCreateOptionalGetters.set(false)
        |    isGettersReturnOptional.set(false)
        |    isOptionalGettersForNullableFieldsOnly.set(false)
        |    fieldVisibility.set("PUBLIC_DEPRECATED")
        |    outputCharacterEncoding.set("UTF-8")
        |    stringType.set("String")
        |    templateDirectory.set(null as String?)
        |    isEnableDecimalLogicalType.set(true)
        |}
        |""".stripMargin()

        when:
        def result = run()

        then:
        result.task(":generateAvroJava").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS
        projectFile(buildOutputClassPath("example/avro/User.class")).file
    }
}
