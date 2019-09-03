/*
 * Copyright (c) 1998-2019 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.widget;

import com.trollworks.toolkit.ui.Colors;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.TextDrawing;
import com.trollworks.toolkit.ui.border.LineBorder;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.task.Tasks;
import com.trollworks.toolkit.utility.text.Numbers;
import com.trollworks.toolkit.utility.text.Text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Displays the current memory usage. */
public class MemoryPanel extends JPanel implements Runnable, HierarchyListener, MouseListener {
    private static final long MB            = 1024 * 1024;
    private MemoryMXBean      mMemoryMXBean = ManagementFactory.getMemoryMXBean();
    private long              mUsed;
    private long              mMax;
    private boolean           mPending;

    /** Creates a new {@link MemoryPanel}. */
    public MemoryPanel() {
        super(null);
        setBorder(new LineBorder());
        setToolTipText(Text.wrapPlainTextForToolTip(I18n.Text("Click to run garbage collection")));
        addMouseListener(this);
        addHierarchyListener(this);
        refresh();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets    insets = getInsets();
        Dimension size   = TextDrawing.getPreferredSize(getFont(), getText());
        size.width  += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        return size;
    }

    @Override
    public synchronized void run() {
        refresh();
        mPending = false;
        schedule();
    }

    @Override
    public void hierarchyChanged(HierarchyEvent event) {
        schedule();
    }

    private synchronized void schedule() {
        if (!mPending && isDisplayable()) {
            mPending = true;
            Tasks.scheduleOnUIThread(this, 1, TimeUnit.SECONDS, this);
        }
    }

    private void refresh() {
        MemoryUsage heapMemoryUsage = mMemoryMXBean.getHeapMemoryUsage();
        mUsed = heapMemoryUsage.getUsed();
        mMax  = heapMemoryUsage.getMax();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics gc) {
        super.paintComponent(gc);
        Rectangle bounds = GraphicsUtilities.getLocalInsetBounds(this);
        gc.setColor(Colors.adjustSaturation(Color.YELLOW, -0.75f));
        int width = (int) (bounds.width * mUsed / mMax);
        gc.fillRect(bounds.x, bounds.y, width, bounds.height);
        gc.setColor(Colors.adjustBrightness(Color.YELLOW, -0.1f));
        gc.drawLine(bounds.x + width - 1, bounds.y, bounds.x + width - 1, bounds.y + bounds.height - 1);
        gc.setColor(Color.BLACK);
        TextDrawing.draw(gc, bounds, getText(), SwingConstants.CENTER, SwingConstants.CENTER);
    }

    private String getText() {
        return MessageFormat.format(I18n.Text("{0}MB used of {1}MB"), formatMB(mUsed), formatMB(mMax));
    }

    private static String formatMB(long amt) {
        return Numbers.format((amt + MB / 2) / MB);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        // Not used
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        // Not used
    }

    @Override
    public void mouseExited(MouseEvent event) {
        // Not used
    }

    @Override
    public void mousePressed(MouseEvent event) {
        mMemoryMXBean.gc();
        refresh();
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        // Not used
    }
}
