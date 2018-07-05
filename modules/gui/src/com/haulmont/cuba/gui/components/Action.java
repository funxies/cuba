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
package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.WindowManagerImpl;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.security.entity.ConstraintOperationType;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.EventObject;

/**
 * The {@code Action} interface abstracts away a function from a visual component.
 * When an action occurs, {@link #actionPerform(Component)} method is invoked.
 */
public interface Action {

    String PROP_CAPTION = "caption";
    String PROP_DESCRIPTION = "description";
    String PROP_SHORTCUT = "shortcut";
    String PROP_ICON = "icon";
    String PROP_ENABLED = "enabled";
    String PROP_VISIBLE = "visible";

    /**
     * @return  action's identifier
     */
    String getId();

    /**
     * @return  action's localized caption
     */
    String getCaption();
    void setCaption(String caption);

    /**
     * @return  action's description
     */
    String getDescription();
    void setDescription(String description);

    /**
     *
     * @return action's shortcut
     */
    KeyCombination getShortcutCombination();
    void setShortcutCombination(KeyCombination shortcut);

    /**
     * Set shortcut from string representation.
     *
     * @param shortcut string of type "Modifiers-Key", e.g. "Alt-N". Case-insensitive.
     */
    void setShortcut(String shortcut);

    /**
     * @return  action's icon
     */
    String getIcon();
    void setIcon(String icon);
    /**
     * Set an icon from an icon set.
     */
    void setIconFromSet(Icons.Icon icon);

    /**
     * @return  whether the action is currently enabled
     */
    boolean isEnabled();
    void setEnabled(boolean enabled);

    /**
     * @return  whether the action is currently visible
     */
    boolean isVisible();
    void setVisible(boolean visible);

    /**
     * Refresh internal state of the action to initialize enabled, visible, caption, icon, etc. properties depending
     * on programmatically set values and user permissions set at runtime.
     *
     * <br> For example, this method is called by visual components holding actions when they are connected to
     * datasources. At this moment the action can find out what entity it is connected to and change its state
     * according to the user permissions.
     */
    void refreshState();

    /**
     * @return  a single component owning the action. If there are several owners, first will be returned.
     */
    ActionOwner getOwner();

    /**
     * @return the collection of owners
     */
    Collection<ActionOwner> getOwners();

    /**
     * Add an owner component.
     * @param actionOwner   owner component
     */
    void addOwner(ActionOwner actionOwner);

    /**
     * Remove an owner component.
     * @param actionOwner   owner component
     */
    void removeOwner(ActionOwner actionOwner);

    /**
     * Invoked by owning component when an action occurs.
     * @param component invoking component
     */
    void actionPerform(Component component);

    /**
     * Adds a listener to be notified about Enabled, Caption or Icon property changes.
     *
     * @param listener a <code>PropertyChangeListener</code> object
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a listener.
     *
     * @param listener  a <code>PropertyChangeListener</code> object
     * @see #addPropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Indicates that the action can be assigned a {@link WindowManagerImpl.OpenType} to open a related screen.
     */
    interface HasOpenType extends Action {
        WindowManagerImpl.OpenType getOpenType();
        void setOpenType(WindowManagerImpl.OpenType openType);
    }

    /**
     * Indicates that the action can be affected by UI permissions.
     */
    interface SecuredAction extends Action {
        boolean isEnabledByUiPermissions();
        void setEnabledByUiPermissions(boolean enabledByUiPermissions);

        boolean isVisibleByUiPermissions();
        void setVisibleByUiPermissions(boolean visibleByUiPermissions);
    }

    interface HasTarget extends Action {
        ListComponent getTarget();

        void setTarget(ListComponent target);
    }

    /**
     * Callback interface which is invoked by the action before execution.
     */
    interface BeforeActionPerformedHandler {
        /**
         * Invoked by the action before execution.
         * @return true to continue execution, false to abort
         */
        boolean beforeActionPerformed();
    }

    /**
     * Interface defining methods for adding and removing {@link BeforeActionPerformedHandler}s
     */
    interface HasBeforeActionPerformedHandler extends Action {
        BeforeActionPerformedHandler getBeforeActionPerformedHandler();
        void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler);
    }

    /**
     * Interface defining constraintOperationType and constraintCode options.
     */
    interface HasSecurityConstraint {
        void setConstraintOperationType(ConstraintOperationType constraintOperationType);
        ConstraintOperationType getConstraintOperationType();

        String getConstraintCode();
        void setConstraintCode(String constraintCode);
    }

    /**
     * Used in dialogs to assign a special visual style for a button representing the action.
     */
    enum Status {
        NORMAL,
        PRIMARY
    }

    class ActionPerformedEvent extends EventObject {
        private final Component component;

        public ActionPerformedEvent(Action source, Component component) {
            super(source);
            this.component = component;
        }

        public Component getComponent() {
            return component;
        }

        @Override
        public Action getSource() {
            return (Action) super.getSource();
        }
    }
}