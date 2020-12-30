package com.github.ryarnyah.querydsl

import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PluginTest {

    @Test
    fun pluginIsLoaded() {
        val project = project()
        Assertions.assertNotNull(project.plugins.getPlugin(GradleQueryDSLPlugin::class.java))
    }

    @Test
    fun pluginTaskShouldApply() {
        val project = project()
        Assertions.assertTrue(project.tasks.getByName("queryDslMetadataExport") is MetadataExportTask)
        Assertions.assertTrue(project.tasks.getByName("queryDslJdoExport") is JDOExporterTask)
        Assertions.assertTrue(project.tasks.getByName("queryDslJpaExport") is JPAExporterTask)
        Assertions.assertTrue(project.tasks.getByName("queryDslGenericExport") is GenericExporterTask)
    }

    @Test
    fun pluginExportMetadataTaskShouldRun() {
        val project = project()
        Assertions.assertTrue(project.tasks.getByName("queryDslMetadataExport") is MetadataExportTask)
        val task = project.tasks.getByName("queryDslMetadataExport") as MetadataExportTask
        task.jdbcDriver = "org.h2.Driver"
        task.jdbcUser = "sa"
        task.jdbcPassword = ""
        task.jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        task.packageName = "com.github.ryarnyah.querydsl"

        task.export()
    }

    @Test
    fun pluginExportJdoTaskShouldRun() {
        val project = project()
        Assertions.assertTrue(project.tasks.getByName("queryDslJdoExport") is JDOExporterTask)
        val task = project.tasks.getByName("queryDslJdoExport") as JDOExporterTask
        task.execute()
    }

    @Test
    fun pluginExportJpaTaskShouldRun() {
        val project = project()
        Assertions.assertTrue(project.tasks.getByName("queryDslJpaExport") is JPAExporterTask)
        val task = project.tasks.getByName("queryDslJpaExport") as JPAExporterTask
        task.execute()
    }

    @Test
    fun pluginExportGenericTaskShouldRun() {
        val project = project()
        Assertions.assertTrue(project.tasks.getByName("queryDslGenericExport") is GenericExporterTask)
        val task = project.tasks.getByName("queryDslGenericExport") as GenericExporterTask
        task.execute()
    }

    private fun project() = ProjectBuilder.builder().build().also { project ->
        project.pluginManager.apply(JavaPlugin::class.java)
        project.pluginManager.apply(GradleQueryDSLPlugin::class.java)
    }
}