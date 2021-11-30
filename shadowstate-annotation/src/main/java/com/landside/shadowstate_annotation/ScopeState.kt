package com.landside.shadowstate_annotation

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Retention(RetentionPolicy.CLASS)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
annotation class ScopeState(
    val states: Array<KClass<*>>,
    val agents: Array<KClass<*>>
)