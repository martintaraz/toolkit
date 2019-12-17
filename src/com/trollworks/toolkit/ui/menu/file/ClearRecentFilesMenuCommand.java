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

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.I18n;

import java.awt.event.ActionEvent;

/** Provides the "Clear" command in the {@link RecentFilesMenu}. */
public class ClearRecentFilesMenuCommand extends Command {

    /** The action command this command will issue. */
    public static final String CMD_CLEAR_RECENT_FILES_MENU = "ClearRecentFilesMenu";

    /** The singleton {@link ClearRecentFilesMenuCommand}. */
    public static final ClearRecentFilesMenuCommand INSTANCE = new ClearRecentFilesMenuCommand();

    private ClearRecentFilesMenuCommand() {
        super(I18n.Text("Clear"), CMD_CLEAR_RECENT_FILES_MENU);
    }

    @Override
    public void adjust() {
        setEnabled(RecentFilesMenu.getRecentCount() > 0);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        RecentFilesMenu.clearRecents();
    }
}
