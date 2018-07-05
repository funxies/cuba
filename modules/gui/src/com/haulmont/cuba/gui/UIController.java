package com.haulmont.cuba.gui;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JavaDoc
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface UIController {
    String value();

    String template() default "";

    boolean multipleOpen() default false;
}