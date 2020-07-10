package com.landside.shadowstate_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kotlin.reflect.KClass;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface BindState {
    Class state();
    Class agent();
}
