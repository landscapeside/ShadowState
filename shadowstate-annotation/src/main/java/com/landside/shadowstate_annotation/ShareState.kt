package com.landside.shadowstate_annotation

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Retention(RetentionPolicy.CLASS)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
annotation class ShareState(
    val states: Array<KClass<*>>,
    val agent: Array<KClass<*>>
)