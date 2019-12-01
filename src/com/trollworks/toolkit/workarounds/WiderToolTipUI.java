/*
 * Copyright (c) 1998-2019 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.workarounds;

import com.trollworks.toolkit.utility.Platform;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

public class WiderToolTipUI extends BasicToolTipUI {
    private static WiderToolTipUI sharedInstance = new WiderToolTipUI();

    public static void installIfNeeded() {
        if (Platform.isWindows()) {
            UIManager.put("ToolTipUI", WiderToolTipUI.class.getName());
        }
    }

    public static ComponentUI createUI(JComponent comp) {
        return sharedInstance;
    }

    @Override
    public Dimension getPreferredSize(JComponent comp) {
        Dimension size = super.getPreferredSize(comp);
        size.width += 4;
        return size;
    }
}