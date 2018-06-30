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
package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.config.WindowConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(WindowManager.NAME)
public class WebWindowManager implements WindowManager {
    @Inject
    private WindowConfig windowConfig;

    @Override
    public <T> T create(Class<T> screenClass, LaunchMode launchMode, ScreenOptions options) {
        // todo get actual screen implementation

        // todo check permissions on screen

        // todo check if already opened

        // todo create the corresponding Window instance first without loader

        // todo create controller class

        // todo load XML markup if annotation present, or screen is legacy screen

        // todo legacy datasource layer

        // todo injection

        // todo start firing INIT events

        return null;
    }

    @Override
    public void show(Screen screen) {

    }

    @Override
    public void remove(Screen screen) {

    }
}