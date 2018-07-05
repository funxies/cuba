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

package com.haulmont.cuba.desktop.gui.components;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.desktop.DetachedFrame;
import com.haulmont.cuba.desktop.sys.DesktopWindowManager;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.FrameContext;
import com.haulmont.cuba.gui.WindowManagerImpl;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.events.sys.UiEventsMulticaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.*;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

public class DesktopFrame
        extends DesktopVBox
        implements DetachableFrame, WrappedFrame, Component.HasXmlDescriptor {

    private final Logger log = LoggerFactory.getLogger(DesktopFrame.class);

    private String messagePack;
    private FrameContext context;
    private DsContext dsContext;
    private Frame wrapper;
    private Map<String, Component> allComponents = new HashMap<>();

    private boolean detached;
    private DetachedFrame detachedFrame;
    private int componentPosition = -1;
    private HierarchyListener hierarchyListener;

    private List<DetachListener> detachListeners = new ArrayList<>();
    
    private WindowConfig windowConfig = AppBeans.get(WindowConfig.NAME);
    protected Messages messages = AppBeans.get(Messages.NAME);

    private DesktopFrameActionsHolder actionsHolder;

    protected boolean isAttachedToRoot = false;

    public DesktopFrame() {
        actionsHolder = new DesktopFrameActionsHolder(this, impl);

        impl.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(() -> {
                    if (!isAttachedToRoot) {
                        if (SwingUtilities.getRoot(event.getComponent()) != null) {
                            enableEventListeners();
                            isAttachedToRoot = true;
                        }
                    }
                });
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                SwingUtilities.invokeLater(() -> {
                    if (isAttachedToRoot) {
                        if (SwingUtilities.getRoot(event.getComponent()) == null) {
                            disableEventListeners();
                            isAttachedToRoot = false;
                        }
                    }
                });
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    protected void disableEventListeners() {
        Frame wrapper = getWrapper();
        if (wrapper != null) {
            List<ApplicationListener> uiEventListeners = ((AbstractFrame) wrapper).getUiEventListeners();
            if (uiEventListeners != null) {
                for (ApplicationListener listener : uiEventListeners) {
                    UiEventsMulticaster multicaster = App.getInstance().getUiEventsMulticaster();
                    multicaster.removeApplicationListener(listener);
                }
            }
        }
    }

    protected void enableEventListeners() {
        Frame wrapper = getWrapper();
        if (wrapper != null) {
            List<ApplicationListener> uiEventListeners = ((AbstractFrame) wrapper).getUiEventListeners();
            if (uiEventListeners != null) {
                for (ApplicationListener listener : uiEventListeners) {
                    UiEventsMulticaster multicaster = App.getInstance().getUiEventsMulticaster();
                    multicaster.addApplicationListener(listener);
                }
            }
        }
    }

    @Override
    public FrameContext getContext() {
        return context == null ? getFrame().getContext() : context;
    }

    @Override
    public void setContext(FrameContext ctx) {
        this.context = ctx;
    }

    @Override
    public DsContext getDsContext() {
        return dsContext == null ? getFrame().getDsContext() : dsContext;
    }

    @Override
    public void setDsContext(DsContext dsContext) {
        this.dsContext = dsContext;
    }

    @Override
    public String getMessagesPack() {
        return messagePack;
    }

    @Override
    public void setMessagesPack(String name) {
        messagePack = name;
    }

    @Override
    protected void attachToFrame(Component component) {
        registerComponent(component);
    }

    @Override
    public void registerComponent(Component component) {
        if (component.getId() != null) {
            allComponents.put(component.getId(), component);
        }
    }

    @Override
    public void unregisterComponent(Component component) {
        if (component.getId() != null) {
            allComponents.remove(component.getId());
        }
    }

    @Nullable
    @Override
    public Component getRegisteredComponent(String id) {
        return allComponents.get(id);
    }

    @Override
    public boolean isValid() {
        Collection<Component> components = ComponentsHelper.getComponents(this);
        for (Component component : components) {
            if (component instanceof Validatable) {
                Component.Validatable validatable = (Component.Validatable) component;
                if (validatable.isValidateOnCommit() && !validatable.isValid())
                    return false;
            }
        }
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Collection<Component> components = ComponentsHelper.getComponents(this);
        for (Component component : components) {
            if (component instanceof Validatable) {
                Component.Validatable validatable = (Component.Validatable) component;
                if (validatable.isValidateOnCommit()) {
                    validatable.validate();
                }
            }
        }
    }

    @Override
    public boolean validate(List<Validatable> fields) {
        ValidationErrors errors = new ValidationErrors();

        for (Validatable field : fields) {
            try {
                field.validate();
            } catch (ValidationException e) {
                if (log.isTraceEnabled())
                    log.trace("Validation failed", e);
                else if (log.isDebugEnabled())
                    log.debug("Validation failed: " + e);

                ComponentsHelper.fillErrorMessages(field, e, errors);
            }
        }

        return handleValidationErrors(errors);
    }

    @Override
    public boolean validateAll() {
        ValidationErrors errors = new ValidationErrors();

        Collection<Component> components = ComponentsHelper.getComponents(this);
        for (Component component : components) {
            if (component instanceof Validatable) {
                Validatable validatable = (Validatable) component;
                if (validatable.isValidateOnCommit()) {
                    try {
                        validatable.validate();
                    } catch (ValidationException e) {
                        if (log.isTraceEnabled())
                            log.trace("Validation failed", e);
                        else if (log.isDebugEnabled())
                            log.debug("Validation failed: " + e);

                        ComponentsHelper.fillErrorMessages(validatable, e, errors);
                    }
                }
            }
        }

        return handleValidationErrors(errors);
    }

    protected boolean handleValidationErrors(ValidationErrors errors) {
        if (errors.isEmpty())
            return true;

        DesktopComponentsHelper.focusProblemComponent(errors);

        Window window = ComponentsHelper.getWindow(wrapper);
        if (window instanceof AbstractFrame) {
            SwingUtilities.invokeLater(() -> {
                ((AbstractFrame) window).showValidationErrors(errors);
            });
        }

        return false;
    }

    private DesktopWindowManager getWindowManager() {
        return DesktopComponentsHelper.getTopLevelFrame((Frame) this).getWindowManager();
    }

    @Override
    public Window openWindow(String windowAlias, WindowManagerImpl.OpenType openType, Map<String, Object> params) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openWindow(windowInfo, openType, params);
    }

    @Override
    public Window.Editor openEditor(Entity item, WindowManagerImpl.OpenType openType) {
        WindowInfo editorScreen = windowConfig.getEditorScreen(item);
        return getWindowManager().openEditor(editorScreen, item, openType);
    }

    @Override
    public Window.Editor openEditor(Entity item, WindowManagerImpl.OpenType openType, Map<String, Object> params) {
        WindowInfo editorScreen = windowConfig.getEditorScreen(item);
        return getWindowManager().openEditor(editorScreen, item, openType, params);
    }

    @Override
    public Window.Editor openEditor(Entity item, WindowManagerImpl.OpenType openType, Map<String, Object> params, Datasource parentDs) {
        WindowInfo editorScreen = windowConfig.getEditorScreen(item);
        return getWindowManager().openEditor(editorScreen, item, openType, params, parentDs);
    }

    @Override
    public Window.Editor openEditor(String windowAlias, Entity item, WindowManagerImpl.OpenType openType, Map<String, Object> params, Datasource parentDs) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openEditor(windowInfo, item, openType, params, parentDs);
    }

    @Override
    public Window.Editor openEditor(String windowAlias, Entity item, WindowManagerImpl.OpenType openType, Map<String, Object> params) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openEditor(windowInfo, item, openType, params);
    }

    @Override
    public Window.Editor openEditor(String windowAlias, Entity item, WindowManagerImpl.OpenType openType, Datasource parentDs) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openEditor(windowInfo, item, openType, parentDs);
    }

    @Override
    public Window.Editor openEditor(String windowAlias, Entity item, WindowManagerImpl.OpenType openType) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openEditor(windowInfo, item, openType);
    }

    @Override
    public Window openWindow(String windowAlias, WindowManagerImpl.OpenType openType) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openWindow(windowInfo, openType);
    }

    @Override
    public Window.Lookup openLookup(Class<? extends Entity> entityClass, Window.Lookup.Handler handler, WindowManagerImpl.OpenType openType) {
        WindowInfo lookupScreen = windowConfig.getLookupScreen(entityClass);
        return getWindowManager().openLookup(lookupScreen, handler, openType);
    }

    @Override
    public Window.Lookup openLookup(Class<? extends Entity> entityClass, Window.Lookup.Handler handler, WindowManagerImpl.OpenType openType, Map<String, Object> params) {
        WindowInfo lookupScreen = windowConfig.getLookupScreen(entityClass);
        return getWindowManager().openLookup(lookupScreen, handler, openType, params);
    }

    @Override
    public Window.Lookup openLookup(String windowAlias, Window.Lookup.Handler handler, WindowManagerImpl.OpenType openType, Map<String, Object> params) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openLookup(windowInfo, handler, openType, params);
    }

    @Override
    public Window.Lookup openLookup(String windowAlias, Window.Lookup.Handler handler, WindowManagerImpl.OpenType openType) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        return getWindowManager().openLookup(windowInfo, handler, openType);
    }

    @Override
    public Frame openFrame(Component parent, String windowAlias) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        Frame wrappedFrame = ((Frame.Wrapper) wrapper).getWrappedFrame();
        return getWindowManager().openFrame(wrappedFrame, parent, windowInfo);
    }

    @Override
    public Frame openFrame(Component parent, String windowAlias, Map<String, Object> params) {
        WindowInfo windowInfo = windowConfig.getWindowInfo(windowAlias);
        Frame wrappedFrame = ((Frame.Wrapper) wrapper).getWrappedFrame();
        return getWindowManager().openFrame(wrappedFrame, parent, windowInfo, params);
    }

    @Override
    public void detachFrame(String caption) {
        if (isDetached()) {
            throw new RuntimeException("Frame already detached");
        }
        final java.awt.Container parent = impl.getParent();

        detachedFrame = new DetachedFrame(caption, parent);

        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (impl == parent.getComponent(i)) {
                componentPosition = i;
                break;
            }
        }

        hierarchyListener = new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) == HierarchyEvent.DISPLAYABILITY_CHANGED
                        && !parent.isDisplayable()) {
                    parent.removeHierarchyListener(this);
                    attachFrame();
                }
            }
        };

        detachedFrame.setLocationRelativeTo(DesktopComponentsHelper.getTopLevelFrame(impl));
        detachedFrame.setSize(impl.getSize());
        detachedFrame.add(impl);
        detachedFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parent.removeHierarchyListener(hierarchyListener);
                attachFrame();
            }
        });
        parent.revalidate();
        parent.repaint();
        parent.addHierarchyListener(hierarchyListener);
        detachedFrame.setVisible(true);
        detached = true;
        for (DetachListener listener : detachListeners) {
            listener.frameDetached(this);
        }
    }

    @Override
    public void attachFrame() {
        if (!isDetached()) {
            throw new RuntimeException("Frame is already attached");
        }
        java.awt.Container parent = detachedFrame.getParentContainer();
        parent.add(impl, componentPosition);
        detachedFrame.dispose();
        parent.removeHierarchyListener(hierarchyListener);
        detachedFrame = null;
        detached = false;
        parent.revalidate();
        parent.repaint();
        for (DetachListener listener : detachListeners) {
            listener.frameAttached(this);
        }
    }

    @Override
    public void addDetachListener(DetachListener listener) {
        if (!detachListeners.contains(listener)) {
            detachListeners.add(listener);
        }
    }

    @Override
    public void removeDetachListener(DetachListener listener) {
        detachListeners.remove(listener);
    }

    @Override
    public boolean isDetached() {
        return detached;
    }

    @Override
    public void showMessageDialog(String title, String message, MessageType messageType) {
        getWindowManager().showMessageDialog(title, message, messageType);
    }

    @Override
    public void showOptionDialog(String title, String message, MessageType messageType, Action[] actions) {
        getWindowManager().showOptionDialog(title, message, messageType, actions);
    }

    @Override
    public void showOptionDialog(String title, String message, MessageType messageType, java.util.List<Action> actions) {
        getWindowManager().showOptionDialog(title, message, messageType, actions.toArray(new Action[actions.size()]));
    }

    @Override
    public void showNotification(String caption, String description, NotificationType type) {
        getWindowManager().showNotification(caption, description, type);
    }

    @Override
    public void showWebPage(String url, @Nullable Map<String, Object> params) {
        getWindowManager().showWebPage(url, params);
    }

    @Override
    public void showNotification(String caption) {
        getWindowManager().showNotification(caption);
    }

    @Override
    public void showNotification(String caption, NotificationType type) {
        getWindowManager().showNotification(caption, type);
    }

    @Override
    public Frame wrapBy(Class<?> aClass) {
        try {
            // First try to find an old-style constructor with Frame parameter
            Constructor<?> constructor = null;
            try {
                constructor = aClass.getConstructor(Frame.class);
            } catch (NoSuchMethodException e) {
                //
            }
            if (constructor != null) {
                wrapper = (Frame) constructor.newInstance(this);
            } else {
                // If not found, get the default constructor
                constructor = aClass.getConstructor();
                wrapper = (Frame) constructor.newInstance();
                ((AbstractFrame) wrapper).setWrappedFrame(this);
            }
            return wrapper;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to init frame controller", e);
        }
    }

    @Override
    public Frame getWrapper() {
        return wrapper;
    }

    @Override
    public Component getComponent(String id) {
        return ComponentsHelper.getFrameComponent(this, id);
    }

    @Override
    public boolean expandsWidth() {
        return true;
    }

    @Override
    public boolean expandsHeight() {
        return false;
    }

    @Override
    public void addAction(final Action action) {
        checkNotNullArgument(action, "action must be non null");

        actionsHolder.addAction(action);
    }

    @Override
    public void addAction(Action action, int index) {
        checkNotNullArgument(action, "action must be non null");

        actionsHolder.addAction(action, index);
    }

    @Override
    public void removeAction(@Nullable Action action) {
        actionsHolder.removeAction(action);
    }

    @Override
    public void removeAction(@Nullable String id) {
        actionsHolder.removeAction(id);
    }

    @Override
    public void removeAllActions() {
        actionsHolder.removeAllActions();
    }

    @Override
    public Collection<Action> getActions() {
        return actionsHolder.getActions();
    }

    @Override
    @Nullable
    public Action getAction(String id) {
        return actionsHolder.getAction(id);
    }
}