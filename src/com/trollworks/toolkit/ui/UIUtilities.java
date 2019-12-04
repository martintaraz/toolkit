/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.image.StdImage;
import com.trollworks.toolkit.utility.Platform;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RepaintManager;

/** Various utility methods for the UI. */
public class UIUtilities {
    /**
     * Selects the tab with the specified title.
     *
     * @param pane  The {@link JTabbedPane} to use.
     * @param title The title to select.
     */
    public static void selectTab(JTabbedPane pane, String title) {
        int count = pane.getTabCount();
        for (int i = 0; i < count; i++) {
            if (pane.getTitleAt(i).equals(title)) {
                pane.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Disables all controls in the specified component and all its children.
     *
     * @param comp The {@link Component} to work on.
     */
    public static void disableControls(Component comp) {
        if (comp instanceof Container) {
            Container container = (Container) comp;
            int       count     = container.getComponentCount();

            for (int i = 0; i < count; i++) {
                disableControls(container.getComponent(i));
            }
        }

        if (comp instanceof AbstractButton || comp instanceof JComboBox || comp instanceof JTextField) {
            comp.setEnabled(false);
        }
    }

    /**
     * Sets a {@link Component}'s min, max & preferred sizes to a specific size.
     *
     * @param comp The {@link Component} to work on.
     * @param size The size to set the component to.
     */
    public static void setOnlySize(Component comp, Dimension size) {
        comp.setMinimumSize(size);
        comp.setMaximumSize(size);
        comp.setPreferredSize(size);
    }

    /**
     * Sets a {@link Component}'s min & max sizes to its preferred size. Note that this applies a
     * fudge factor to the width on the Windows platform to attempt to get around highly inaccurate
     * font measurements.
     *
     * @param comp The {@link Component} to work on.
     */
    public static void setToPreferredSizeOnly(Component comp) {
        Dimension size = comp.getPreferredSize();
        if (Platform.isWindows()) {
            size.width += 4; // Fudge the width on Windows, since its text measurement seems to be
                             // off in many cases
        }
        setOnlySize(comp, size);
    }

    /** @param comps The {@link Component}s to set to the same size. */
    public static void adjustToSameSize(Component... comps) {
        Dimension best = new Dimension();
        for (Component comp : comps) {
            Dimension size = comp.getPreferredSize();
            if (size.width > best.width) {
                best.width = size.width;
            }
            if (size.height > best.height) {
                best.height = size.height;
            }
        }
        for (Component comp : comps) {
            setOnlySize(comp, best);
        }
    }

    /**
     * Converts a {@link Point} from one component's coordinate system to another's.
     *
     * @param pt   The point to convert.
     * @param from The component the point originated in.
     * @param to   The component the point should be translated to.
     */
    public static void convertPoint(Point pt, Component from, Component to) {
        convertPointToScreen(pt, from);
        convertPointFromScreen(pt, to);
    }

    /**
     * Converts a {@link Point} from on the screen to a position within the component.
     *
     * @param pt        The point to convert.
     * @param component The component the point should be translated to.
     */
    public static void convertPointFromScreen(Point pt, Component component) {
        while (component != null) {
            pt.x -= component.getX();
            pt.y -= component.getY();
            if (component instanceof Window) {
                break;
            }
            component = component.getParent();
        }
    }

    /**
     * Converts a {@link Point} in a component to its position on the screen.
     *
     * @param pt        The point to convert.
     * @param component The component the point originated in.
     */
    public static void convertPointToScreen(Point pt, Component component) {
        while (component != null) {
            pt.x += component.getX();
            pt.y += component.getY();
            if (component instanceof Window) {
                break;
            }
            component = component.getParent();
        }
    }

    /**
     * Converts a {@link Rectangle} from one component's coordinate system to another's.
     *
     * @param bounds The rectangle to convert.
     * @param from   The component the rectangle originated in.
     * @param to     The component the rectangle should be translated to.
     */
    public static void convertRectangle(Rectangle bounds, Component from, Component to) {
        convertRectangleToScreen(bounds, from);
        convertRectangleFromScreen(bounds, to);
    }

    /**
     * Converts a {@link Rectangle} from on the screen to a position within the component.
     *
     * @param bounds    The rectangle to convert.
     * @param component The component the rectangle should be translated to.
     */
    public static void convertRectangleFromScreen(Rectangle bounds, Component component) {
        while (component != null) {
            bounds.x -= component.getX();
            bounds.y -= component.getY();
            if (component instanceof Window) {
                break;
            }
            component = component.getParent();
        }
    }

    /**
     * Converts a {@link Rectangle} in a component to its position on the screen.
     *
     * @param bounds    The rectangle to convert.
     * @param component The component the rectangle originated in.
     */
    public static void convertRectangleToScreen(Rectangle bounds, Component component) {
        while (component != null) {
            bounds.x += component.getX();
            bounds.y += component.getY();
            if (component instanceof Window) {
                break;
            }
            component = component.getParent();
        }
    }

    /**
     * @param parent The parent {@link Container}.
     * @param child  The child {@link Component}.
     * @return The index of the specified {@link Component}. -1 will be returned if the
     *         {@link Component} isn't a direct child.
     */
    public static int getIndexOf(Container parent, Component child) {
        if (parent != null) {
            int count = parent.getComponentCount();
            for (int i = 0; i < count; i++) {
                if (child == parent.getComponent(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Clones a {@link MouseEvent}.
     *
     * @param event The event to clone.
     * @return The new {@link MouseEvent}.
     */
    public static final MouseEvent cloneMouseEvent(MouseEvent event) {
        if (event instanceof MouseWheelEvent) {
            MouseWheelEvent old = (MouseWheelEvent) event;
            return new MouseWheelEvent((Component) old.getSource(), old.getID(), System.currentTimeMillis(), old.getModifiersEx(), old.getX(), old.getY(), old.getClickCount(), old.isPopupTrigger(), old.getScrollType(), old.getScrollAmount(), old.getWheelRotation());
        }
        return new MouseEvent((Component) event.getSource(), event.getID(), System.currentTimeMillis(), event.getModifiersEx(), event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger());
    }

    /**
     * Clones a {@link MouseEvent}.
     *
     * @param event       The event to clone.
     * @param refreshTime Pass in <code>true</code> to generate a new time stamp.
     * @return The new {@link MouseEvent}.
     */
    public static final MouseEvent cloneMouseEvent(MouseEvent event, boolean refreshTime) {
        if (event instanceof MouseWheelEvent) {
            MouseWheelEvent old = (MouseWheelEvent) event;
            return new MouseWheelEvent((Component) old.getSource(), old.getID(), refreshTime ? System.currentTimeMillis() : event.getWhen(), old.getModifiersEx(), old.getX(), old.getY(), old.getClickCount(), old.isPopupTrigger(), old.getScrollType(), old.getScrollAmount(), old.getWheelRotation());
        }
        return new MouseEvent((Component) event.getSource(), event.getID(), refreshTime ? System.currentTimeMillis() : event.getWhen(), event.getModifiersEx(), event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger());
    }

    /**
     * Clones a {@link MouseEvent}.
     *
     * @param event       The event to clone.
     * @param source      Pass in a new source.
     * @param where       Pass in a new location.
     * @param refreshTime Pass in <code>true</code> to generate a new time stamp.
     * @return The new {@link MouseEvent}.
     */
    public static final MouseEvent cloneMouseEvent(MouseEvent event, Component source, Point where, boolean refreshTime) {
        if (event instanceof MouseWheelEvent) {
            MouseWheelEvent old = (MouseWheelEvent) event;
            return new MouseWheelEvent(source, old.getID(), refreshTime ? System.currentTimeMillis() : event.getWhen(), old.getModifiersEx(), where.x, where.y, old.getClickCount(), old.isPopupTrigger(), old.getScrollType(), old.getScrollAmount(), old.getWheelRotation());
        }
        return new MouseEvent(source, event.getID(), refreshTime ? System.currentTimeMillis() : event.getWhen(), event.getModifiersEx(), where.x, where.y, event.getClickCount(), event.isPopupTrigger());
    }

    /**
     * Forwards a {@link MouseEvent} from one component to another.
     *
     * @param event The event to forward.
     * @param from  The component that originally received the event.
     * @param to    The component the event should be forwarded to.
     */
    static public void forwardMouseEvent(MouseEvent event, Component from, Component to) {
        translateMouseEvent(event, from, to);
        to.dispatchEvent(event);
    }

    /**
     * Translates a {@link MouseEvent} from one component to another.
     *
     * @param event The event that will be forwarded.
     * @param from  The component that originally received the event.
     * @param to    The component the event should be forwarded to.
     */
    static public void translateMouseEvent(MouseEvent event, Component from, Component to) {
        Point evtPt = event.getPoint();
        Point pt    = new Point(evtPt);
        UIUtilities.convertPoint(pt, from, to);
        event.setSource(to);
        event.translatePoint(pt.x - evtPt.x, pt.y - evtPt.y);
    }

    /**
     * @param comp The component to work with.
     * @return Whether the component should be expanded to fit.
     */
    public static boolean shouldTrackViewportWidth(Component comp) {
        Container parent = comp.getParent();
        if (parent instanceof JViewport) {
            Dimension available = parent.getSize();
            Dimension prefSize  = comp.getPreferredSize();
            return prefSize.width < available.width;
        }
        return false;
    }

    /**
     * @param comp The component to work with.
     * @return Whether the component should be expanded to fit.
     */
    public static boolean shouldTrackViewportHeight(Component comp) {
        Container parent = comp.getParent();
        if (parent instanceof JViewport) {
            Dimension available = parent.getSize();
            Dimension prefSize  = comp.getPreferredSize();
            return prefSize.height < available.height;
        }
        return false;
    }

    /** @param comp The component to revalidate. */
    public static void revalidateImmediately(Component comp) {
        if (comp != null) {
            RepaintManager mgr = RepaintManager.currentManager(comp);
            mgr.validateInvalidComponents();
            mgr.paintDirtyRegions();
        }
    }

    /**
     * @param component The component to be looked at.
     * @param type      The type of component being looked for.
     * @return The first object that matches, starting with the component itself and working up
     *         through its parents, or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSelfOrAncestorOfType(Component component, Class<T> type) {
        if (component != null) {
            if (type.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
            return getAncestorOfType(component, type);
        }
        return null;
    }

    /**
     * @param component The component whose ancestor chain is to be looked at.
     * @param type      The type of ancestor being looked for.
     * @return The ancestor, or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAncestorOfType(Component component, Class<T> type) {
        if (component == null) {
            return null;
        }
        Container parent = component.getParent();
        while (parent != null && !type.isAssignableFrom(parent.getClass())) {
            parent = parent.getParent();
        }
        return (T) parent;
    }

    /**
     * Since JComboBox.getSelectedItem() returns a plain Object, this allows us to get the
     * appropriate type of object instead.
     */
    public static <E> E getTypedSelectedItemFromCombo(JComboBox<E> combo) {
        int index = combo.getSelectedIndex();
        return index != -1 ? combo.getItemAt(index) : null;
    }

    /**
     * @param component The component to generate an image of.
     * @return The newly created image.
     */
    public static StdImage getImage(JComponent component) {
        StdImage offscreen = null;
        synchronized (component.getTreeLock()) {
            Graphics2D gc = null;
            try {
                Rectangle bounds = component.getVisibleRect();
                offscreen = StdImage.createTransparent(component.getGraphicsConfiguration(), bounds.width, bounds.height);
                gc        = offscreen.getGraphics();
                gc.translate(-bounds.x, -bounds.y);
                component.paint(gc);
            } catch (Exception exception) {
                Log.error(exception);
            } finally {
                if (gc != null) {
                    gc.dispose();
                }
            }
        }
        return offscreen;
    }

    /**
     * @param obj The object to extract a {@link Component} for.
     * @return The {@link Component} to use for the dialog, or <code>null</code>.
     */
    public static Component getComponentForDialog(Object obj) {
        return obj instanceof Component ? (Component) obj : null;
    }

    /** @return Whether or not the application is currently in a modal state. */
    public static boolean inModalState() {
        for (Window window : Window.getWindows()) {
            if (window instanceof Dialog) {
                Dialog dialog = (Dialog) window;
                if (dialog.isShowing()) {
                    ModalityType type = dialog.getModalityType();
                    if (type == ModalityType.APPLICATION_MODAL || type == ModalityType.TOOLKIT_MODAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Point convertDropTargetDragPointTo(DropTargetDragEvent dtde, Component comp) {
        Point pt = dtde.getLocation();
        convertPoint(pt, dtde.getDropTargetContext().getComponent(), comp);
        return pt;
    }

    public static void updateDropTargetDragPointTo(DropTargetDragEvent dtde, Component comp) {
        convertPoint(dtde.getLocation(), dtde.getDropTargetContext().getComponent(), comp);
    }

    public static Point convertDropTargetDropPointTo(DropTargetDropEvent dtde, Component comp) {
        Point pt = dtde.getLocation();
        convertPoint(pt, dtde.getDropTargetContext().getComponent(), comp);
        return pt;
    }

    public static void updateDropTargetDropPointTo(DropTargetDropEvent dtde, Component comp) {
        convertPoint(dtde.getLocation(), dtde.getDropTargetContext().getComponent(), comp);
    }
}
