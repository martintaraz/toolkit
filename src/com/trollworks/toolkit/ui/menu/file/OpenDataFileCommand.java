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

import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.utility.FileType;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

/** A command that will open a specific data file. */
public class OpenDataFileCommand extends Command implements Runnable {
    private static final String    CMD_PREFIX    = "OpenDataFile[";	//$NON-NLS-1$
    private static final String    CMD_POSTFIX   = "]";            				//$NON-NLS-1$
    private static boolean         PASS_THROUGH  = false;
    private static ArrayList<File> PENDING_FILES = null;
    private File                   mFile;
    private boolean                mVerify;

    /** @param file The file to open. */
    public static synchronized void open(File file) {
        if (PASS_THROUGH) {
            OpenDataFileCommand opener = new OpenDataFileCommand(file);
            if (!SwingUtilities.isEventDispatchThread()) {
                EventQueue.invokeLater(opener);
            } else {
                opener.run();
            }
        } else {
            if (PENDING_FILES == null) {
                PENDING_FILES = new ArrayList<>();
            }
            PENDING_FILES.add(file);
        }
    }

    /**
     * Enables the pass-through mode so that future calls to {@link #open(File)} will no longer
     * queue files for later opening. All queued files will now be opened.
     */
    public static synchronized void enablePassThrough() {
        PASS_THROUGH = true;
        if (PENDING_FILES != null) {
            for (File file : PENDING_FILES) {
                open(file);
            }
            PENDING_FILES = null;
        }
    }

    /**
     * Creates a new {@link OpenDataFileCommand}.
     *
     * @param title The title to use.
     * @param file The file to open.
     */
    public OpenDataFileCommand(String title, File file) {
        super(title, CMD_PREFIX + file.getName() + CMD_POSTFIX, FileType.getIconsForFile(file).getImage(16));
        mFile = file;
    }

    /**
     * Creates a new {@link OpenDataFileCommand} that can only be invoked successfully if
     * {@link OpenCommand} is enabled.
     *
     * @param file The file to open.
     */
    public OpenDataFileCommand(File file) {
        super(file.getName(), CMD_PREFIX + file.getName() + CMD_POSTFIX);
        mFile = file;
        mVerify = true;
    }

    @Override
    public void adjust() {
        // Not used. Always enabled.
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        run();
    }

    @Override
    public void run() {
        if (mVerify) {
            OpenCommand.INSTANCE.adjust();
            if (!OpenCommand.INSTANCE.isEnabled()) {
                return;
            }
        }
        OpenCommand.open(mFile);
    }
}
