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
package com.haulmont.cuba.gui;

/**
 * JavaDoc
 */
public interface WindowManager {

    String NAME = "cuba_WindowManager";

    default <T> T create(Class<T> screenClass, LaunchMode launchMode) {
        return create(screenClass, launchMode, NO_OPTIONS);
    }

    /**
     * JavaDoc
     */
    <T> T create(Class<T> screenClass, LaunchMode launchMode, ScreenOptions options);

    /**
     * JavaDoc
     */
    void show(Screen screen);

    /**
     * JavaDoc
     *
     * Releases all the resources of screen.
     *
     * @param screen screen
     */
    void remove(Screen screen);

    /**
     * JavaDoc
     */
    interface ScreenOptions {
    }

    /**
     * JavaDoc
     */
    interface LaunchMode {
    }

    /**
     * JavaDoc
     */
    enum OpenMode implements LaunchMode {
        /**
         * Open a screen in new tab of the main window.
         * <br> In Web Client with {@code AppWindow.Mode.SINGLE} the new screen replaces current screen.
         */
        NEW_TAB,
        /**
         * Open a screen on top of the current tab screens stack.
         */
        THIS_TAB,
        /**
         * Open a screen as modal dialog.
         */
        DIALOG,
        /**
         * In Desktop Client open a screen in new main window, in Web Client the same as new {@link #NEW_TAB}
         */
        NEW_WINDOW,
        /**
         * In Web Client opens a screen as main
         */
        TOP_LEVEL
    }

    ScreenOptions NO_OPTIONS = new ScreenOptions() {
        @Override
        public String toString() {
            return "{NO OPTIONS}";
        }
    };
}