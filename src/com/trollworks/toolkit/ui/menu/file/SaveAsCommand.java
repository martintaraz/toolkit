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

import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.StdFileDialog;
import com.trollworks.toolkit.utility.FileType;
import com.trollworks.toolkit.utility.I18n;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/** Provides the "Save As..." command. */
public class SaveAsCommand extends Command {
    /** The action command this command will issue. */
    public static final String        CMD_SAVE_AS = "SaveAs";

    /** The singleton {@link SaveAsCommand}. */
    public static final SaveAsCommand INSTANCE    = new SaveAsCommand();

    private SaveAsCommand() {
        super(I18n.Text("Save As\u2026"), CMD_SAVE_AS, KeyEvent.VK_S, SHIFTED_COMMAND_MODIFIER);
    }

    @Override
    public void adjust() {
        setEnabled(getTarget(Saveable.class) != null);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        saveAs(getTarget(Saveable.class));
    }

    /**
     * Allows the user to save the file under another name.
     *
     * @param saveable The {@link Saveable} to work on.
     * @return The file(s) actually written to. May be empty.
     */
    public static File[] saveAs(Saveable saveable) {
        if (saveable == null) {
            return new File[0];
        }
        String path   = saveable.getPreferredSavePath();
        File   result = StdFileDialog.showSaveDialog(UIUtilities.getComponentForDialog(saveable), I18n.Text("Save As\u2026"), path != null ? new File(path) : null, FileType.getFileFilters(null, saveable.getAllowedFileTypes()));
        File[] files  = result != null ? saveable.saveTo(result) : new File[0];
        if (files != null) {
            for (File file : files) {
                RecentFilesMenu.addRecent(file);
            }
        }
        return files;
    }
}
