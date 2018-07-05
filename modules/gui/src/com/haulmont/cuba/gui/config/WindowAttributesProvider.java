package com.haulmont.cuba.gui.config;

import com.haulmont.cuba.gui.Screen;

public interface WindowAttributesProvider {
    String getTemplate(WindowInfo windowInfo);

    boolean isMultiOpen(WindowInfo windowInfo);

    Class<? extends Screen> getScreenClass(WindowInfo windowInfo);
}