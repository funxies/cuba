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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.mainwindow.TimeZoneIndicator;

public class TimeZoneIndicatorLoader extends AbstractComponentLoader<TimeZoneIndicator> {
    @Override
    public void createComponent() {
        resultComponent = (TimeZoneIndicator) factory.createComponent(TimeZoneIndicator.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);

        loadStyleName(resultComponent, element);
        loadAlign(resultComponent, element);

        loadIcon(resultComponent, element);
        loadCaption(resultComponent, element);
        loadDescription(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);
    }
}