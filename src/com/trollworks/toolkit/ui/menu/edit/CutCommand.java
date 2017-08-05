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

package com.trollworks.toolkit.ui.menu.edit;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.Localization;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.text.JTextComponent;

/** Provides the "Cut" command. */
public class CutCommand extends Command {
    @Localize("Cut")
    @Localize(locale = "ru", value = "Вырезать")
    @Localize(locale = "de", value = "Ausschneiden")
    @Localize(locale = "es", value = "Cortar")
    private static String CUT;

    static {
        Localization.initialize();
    }

    /** The action command this command will issue. */
    public static final String     CMD_CUT  = "Cut";           			//$NON-NLS-1$

    /** The singleton {@link CutCommand}. */
    public static final CutCommand INSTANCE = new CutCommand();

    private CutCommand() {
        super(CUT, CMD_CUT, KeyEvent.VK_X);
    }

    @Override
    public void adjust() {
        boolean enable = false;
        Component comp = getFocusOwner();
        if (comp instanceof JTextComponent && comp.isEnabled()) {
            JTextComponent textComp = (JTextComponent) comp;
            if (textComp.isEditable()) {
                enable = textComp.getSelectionStart() != textComp.getSelectionEnd();
            }
        } else {
            Cutable cutable = getTarget(Cutable.class);
            if (cutable != null) {
                enable = cutable.canCutSelection();
            }
        }
        setEnabled(enable);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Component comp = getFocusOwner();
        if (comp instanceof JTextComponent) {
            ((JTextComponent) comp).cut();
        } else {
            Cutable cutable = getTarget(Cutable.class);
            if (cutable != null && cutable.canCutSelection()) {
                cutable.cutSelection();
            }
        }
    }
}
