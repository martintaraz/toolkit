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

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.AppWindow;
import com.trollworks.toolkit.ui.widget.StdFileDialog;
import com.trollworks.toolkit.utility.FileProxy;
import com.trollworks.toolkit.utility.FileType;
import com.trollworks.toolkit.utility.Localization;

import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileNameExtensionFilter;

/** Provides the "Open..." command. */
public class OpenCommand extends Command implements OpenFilesHandler {
    @Localize("Open\u2026")
    @Localize(locale = "ru", value = "Открыть\u2026")
    @Localize(locale = "de", value = "Öffnen\u2026")
    @Localize(locale = "es", value = "Abrir\u2026")
    private static String OPEN;
    @Localize("All Readable Files")
    private static String ALL_READABLE_FILES;

    static {
        Localization.initialize();
    }

    /** The action command this command will issue. */
    public static final String      CMD_OPEN = "Open"; //$NON-NLS-1$

    /** The singleton {@link OpenCommand}. */
    public static final OpenCommand INSTANCE = new OpenCommand();

    private OpenCommand() {
        super(OPEN, CMD_OPEN, KeyEvent.VK_O);
    }

    @Override
    public void adjust() {
        // Do nothing. Always enabled.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        open();
    }

    /** Ask the user to open a file. */
    public static void open() {
        open(StdFileDialog.showOpenDialog(getFocusOwner(), OPEN, FileType.getOpenableFileFilters(ALL_READABLE_FILES)));
    }

    /** @param file The file to open. */
    public static void open(File file) {
        if (file != null) {
            try {
                FileProxy proxy = AppWindow.findFileProxy(file);
                if (proxy == null) {
                    for (FileType type : FileType.getOpenable()) {
                        if (new FileNameExtensionFilter(type.getDescription(), type.getExtension()).accept(file)) {
                            proxy = type.getFileProxyCreator().create(file);
                            break;
                        }
                    }
                } else {
                    proxy.toFrontAndFocus();
                }
                if (proxy != null) {
                    RecentFilesMenu.addRecent(file);
                } else {
                    throw new IOException("Unknown file extension"); //$NON-NLS-1$
                }
            } catch (Exception exception) {
                Log.error(exception);
                StdFileDialog.showCannotOpenMsg(getFocusOwner(), file.getName(), exception);
            }
        }
    }

    @Override
    public void openFiles(OpenFilesEvent event) {
        for (File file : event.getFiles()) {
            // We call this rather than directly to open(File) above to allow the file opening to be
            // deferred until startup has finished
            OpenDataFileCommand.open(file);
        }
    }
}
