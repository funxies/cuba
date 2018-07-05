/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.haulmont.cuba.gui.config;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.UIController;
import org.dom4j.Element;

import javax.annotation.Nullable;

/**
 * Screen's registration information.
 *
 * @see WindowConfig
 */
public class WindowInfo {

    private final String id;

    private final Element descriptor;
    private final WindowAttributesProvider windowAttributesProvider;
    private final String screenClass; // todo use String here

    public WindowInfo(String id, WindowAttributesProvider windowAttributesProvider, @Nullable Element descriptor) {
        this.id = id;
        this.windowAttributesProvider = windowAttributesProvider;
        this.descriptor = descriptor;
        this.screenClass = null;
    }

    public WindowInfo(String id, WindowAttributesProvider windowAttributesProvider,
                      String screenClass) {
        this.id = id;
        this.windowAttributesProvider = windowAttributesProvider;
        this.screenClass = screenClass;
        this.descriptor = null;
    }

    /**
     * Screen ID as set in <code>screens.xml</code>
     */
    public String getId() {
        return id;
    }

    /**
     * Screen class as set in <code>screens.xml</code>
     */
    @Nullable
    public Class<? extends Screen> getScreenClass() {
        if (screenClass == null) {
            return null;
        }

        return windowAttributesProvider.getScreenClass(this);
    }

    /**
     * The whole XML element of the screen as set in <code>screens.xml</code>
     */
    @Nullable
    public Element getDescriptor() {
        return descriptor;
    }

    /**
     * Screen template path as set in <code>screens.xml</code>
     */
    public String getTemplate() {
        if (screenClass != null) {
            UIController design = screenClass.getAnnotation(UIController.class);
            return design.template();
        }
        if (descriptor != null) {
            return descriptor.attributeValue("template");
        }

        throw new IllegalStateException("Neither screen class nor descriptor is set");
    }

    /**
     * JavaDoc
     */
    public boolean getMultipleOpen() {
        if (screenClass != null) {
            UIController design = screenClass.getAnnotation(UIController.class);
            return design.multipleOpen();
        }

        if (descriptor != null) {
            return Boolean.parseBoolean(descriptor.attributeValue("multipleOpen"));
        }

        throw new IllegalStateException("Neither screen class nor descriptor is set");
    }

    @Override
    public String toString() {
        String template = getTemplate();
        return "id='" + id + '\'' +
                (template != null ? ", template=" + template : "") +
                (screenClass != null ? ", screenClass=" + screenClass : "");
    }
}