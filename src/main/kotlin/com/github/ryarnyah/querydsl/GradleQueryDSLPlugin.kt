package com.github.ryarnyah.querydsl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

class GradleQueryDSLPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(JavaPlugin::class.java) {
            val exportTask = project.tasks.register(
                "queryDslMetadataExport",
                MetadataExportTask::class.java
            ).get()

            val jdoExporterTask = project.tasks.register(
                "queryDslJdoExport",
                JDOExporterTask::class.java
            ).get()

            val jpaExporterTask = project.tasks.register(
                "queryDslJpaExport",
                JPAExporterTask::class.java
            ).get()

            val genericExporterTask = project.tasks.register(
                "queryDslGenericExport",
                GenericExporterTask::class.java
            ).get()

            val generatedSources = setOf(
                exportTask.targetFolder,
                exportTask.beanTargetFolder,
                jdoExporterTask.targetFolder,
                jpaExporterTask.targetFolder,
                genericExporterTask.targetFolder
            )
            val sourceSets: SourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
            val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            main.java.srcDirs(generatedSources)
        }
    }
}