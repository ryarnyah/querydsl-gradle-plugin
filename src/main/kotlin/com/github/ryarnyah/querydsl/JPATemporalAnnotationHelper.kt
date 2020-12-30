package com.github.ryarnyah.querydsl

import com.querydsl.codegen.AnnotationHelper
import com.mysema.codegen.model.TypeCategory
import javax.persistence.Temporal
import javax.persistence.TemporalType


class JPATemporalAnnotationHelper: AnnotationHelper {
    companion object {
        val INSTANCE: JPATemporalAnnotationHelper = JPATemporalAnnotationHelper()
    }

    override fun isSupported(annotationClass: Class<out Annotation?>?): Boolean {
        return annotationClass != null && Temporal::class.java.isAssignableFrom(annotationClass)
    }

    override fun getCustomKey(annotation: Annotation): Any {
        return (annotation as Temporal).value
    }

    override fun getTypeByAnnotation(cl: Class<*>?, annotation: Annotation): TypeCategory? {
        return when ((annotation as Temporal).value) {
            TemporalType.DATE -> TypeCategory.DATE
            TemporalType.TIME -> TypeCategory.TIME
            TemporalType.TIMESTAMP -> TypeCategory.DATETIME
        }
    }
}