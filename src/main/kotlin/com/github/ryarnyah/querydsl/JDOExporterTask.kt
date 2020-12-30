package com.github.ryarnyah.querydsl

import com.querydsl.codegen.GenericExporter
import com.querydsl.codegen.PropertyHandling
import javax.jdo.annotations.Embedded
import javax.jdo.annotations.EmbeddedOnly
import javax.jdo.annotations.NotPersistent
import javax.jdo.annotations.PersistenceCapable

open class JDOExporterTask: AbstractExporterTask() {
    override fun configureExporter(exporter: GenericExporter) {
        super.configureExporter(exporter)
        exporter.setEmbeddableAnnotation(EmbeddedOnly::class.java)
        exporter.setEmbeddedAnnotation(Embedded::class.java)
        exporter.setEntityAnnotation(PersistenceCapable::class.java)
        exporter.setSkipAnnotation(NotPersistent::class.java)
        exporter.setPropertyHandling(PropertyHandling.JDO)
    }
}