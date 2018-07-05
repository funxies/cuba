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

package com.haulmont.cuba.gui.exception;

import com.haulmont.cuba.gui.WindowManagerImpl;
import com.haulmont.cuba.gui.components.Frame;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.springframework.stereotype.Component;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("cuba_UnfetchedAttributeAccessExceptionHandler")
public class UnfetchedAttributeAccessExceptionHandler extends AbstractGenericExceptionHandler {

    private static final Pattern PATTERN = Pattern.compile("at (.+)\\._persistence_get_(.+)\\(");

    public UnfetchedAttributeAccessExceptionHandler() {
        super("com.haulmont.cuba.core.global.IllegalEntityStateException",
                "org.eclipse.persistence.exceptions.ValidationException");
    }

    @Override
    protected boolean canHandle(String className, String message, @Nullable Throwable throwable) {
        return className.equals("com.haulmont.cuba.core.global.IllegalEntityStateException")
                || (className.equals("org.eclipse.persistence.exceptions.ValidationException")
                    && message.contains("An attempt was made to traverse a relationship using indirection that had a null Session"));
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, WindowManagerImpl windowManager) {
        String msg = "It usually occurs when the attribute is not included into a view";
        String defaultMsg = "\n\nSee the log to find out what attribute caused the exception";

        if (throwable != null) {
            Matcher matcher = PATTERN.matcher(ExceptionUtils.getStackTrace(throwable));
            if (matcher.find()) {
                msg += "\n\nEntity: " + matcher.group(1) + "\nAttribute: " + matcher.group(2);
            } else {
                msg += defaultMsg;
            }
        } else {
            msg += defaultMsg;
        }

        windowManager.showNotification("Unfetched attribute access error", msg, Frame.NotificationType.ERROR);
    }
}