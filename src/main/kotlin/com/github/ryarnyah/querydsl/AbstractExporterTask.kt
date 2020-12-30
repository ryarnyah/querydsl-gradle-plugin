package com.github.ryarnyah.querydsl

import com.querydsl.codegen.GenericExporter
import com.querydsl.codegen.Serializer
import com.querydsl.codegen.TypeMappings
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.net.MalformedURLException
import java.net.URLClassLoader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class AbstractExporterTask: DefaultTask() {
    /**
     * target folder for sources
     *
     * @parameter
     * @required
     */
    @OutputDirectory
    var targetFolder: File = File("${project.buildDir.absolutePath}/generated/source/querydsl/main/java")

    /**
     * switch for scala source generation
     *
     * @parameter default-value=false
     */
    @Internal
    var scala = false

    /**
     * packages to be exported
     *
     * @parameter
     * @required
     */
    @Internal
    var packages: Array<String> = arrayOf()

    /**
     * switch for inspecting fields
     *
     * @parameter default-value=true
     */
    @Internal
    var handleFields = true

    /**
     * switch for inspecting getters
     *
     * @parameter default-value=true
     */
    @Internal
    var handleMethods = true

    /**
     * switch for usage of field types instead of getter types
     *
     * @parameter default-value=false
     */
    @Internal
    var useFieldTypes = false

    /**
     * source file encoding
     *
     * @parameter
     */
    @Internal
    var sourceEncoding: String? = null

    /**
     * test classpath usage switch
     *
     * @parameter default-value=false
     */
    @Internal
    var testClasspath = false

    @TaskAction
    fun execute() {
        val classLoader: ClassLoader
        try {
            classLoader = getProjectClassLoader()
        } catch (e: MalformedURLException) {
            throw RuntimeException(e.message, e)
        }

        val charset = Charset.forName(sourceEncoding ?: StandardCharsets.UTF_8.name())
        val exporter = GenericExporter(classLoader, charset)
        exporter.setTargetFolder(targetFolder)

        if (scala) {
            try {
                exporter.setSerializerClass(
                    Class
                        .forName("com.querydsl.scala.ScalaEntitySerializer") as Class<out Serializer?>
                )
                exporter.setTypeMappingsClass(
                    Class
                        .forName("com.querydsl.scala.ScalaTypeMappings") as Class<out TypeMappings?>
                )
                exporter.setCreateScalaSources(true)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e.message, e)
            }
        }

        configureExporter(exporter)
        exporter.export(*packages)
    }

    /**
     * Configures the [GenericExporter]; subclasses may override if desired.
     */
    @Suppress("DEPRECATION")
    protected open fun configureExporter(exporter: GenericExporter) {
        exporter.setHandleFields(handleFields)
        exporter.setHandleMethods(handleMethods)
        exporter.setUseFieldTypes(useFieldTypes)
    }

    private fun getProjectClassLoader(): ClassLoader {
        val sourceSets: SourceSetContainer = project.convention.getByType(SourceSetContainer::class.java)
        val sourceSet = if (testClasspath) {
            sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
        } else {
            sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        }
        return URLClassLoader(sourceSet.runtimeClasspath.map { it.toURI().toURL() }.toTypedArray())
    }
}