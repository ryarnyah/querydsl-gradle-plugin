package com.github.ryarnyah.querydsl

import com.querydsl.codegen.GenericExporter
import com.querydsl.codegen.PropertyHandling
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity

import javax.persistence.MappedSuperclass

open class JPAExporterTask: AbstractExporterTask() {
    override fun configureExporter(exporter: GenericExporter) {
        super.configureExporter(exporter)
        exporter.setEmbeddableAnnotation(Embeddable::class.java)
        exporter.setEmbeddedAnnotation(Embedded::class.java)
        exporter.setEntityAnnotation(Entity::class.java)
        exporter.setSkipAnnotation(Transient::class.java)
        exporter.setSupertypeAnnotation(MappedSuperclass::class.java)
        exporter.setPropertyHandling(PropertyHandling.JPA)

        // AnnotationHelpers to process specific JPA annotations
        exporter.addAnnotationHelper(JPATemporalAnnotationHelper.INSTANCE)
    }
}