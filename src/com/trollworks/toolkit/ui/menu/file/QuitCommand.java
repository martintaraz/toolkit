/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.ui.menu.file;

import com.trollworks.toolkit.io.Log;
import com.trollworks.toolkit.ui.Fonts;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.menu.Command;
import com.trollworks.toolkit.ui.widget.BaseWindow;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.Platform;
import com.trollworks.toolkit.utility.Preferences;

import java.awt.Frame;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Provides the "Quit"/"Exit" command. */
public class QuitCommand extends Command implements QuitHandler {
    /** The action command this command will issue. */
    public static final String CMD_QUIT = "Quit";

    /** The singleton {@link QuitCommand}. */
    public static final QuitCommand INSTANCE = new QuitCommand();

    private boolean mAllowQuitIfNoSignificantWindowsOpen = true;

    private QuitCommand() {
        super(Platform.isMacintosh() ? I18n.Text("Quit") : I18n.Text("Exit"), CMD_QUIT, KeyEvent.VK_Q);
    }

    @Override
    public void adjust() {
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        attemptQuit();
    }

    /** Attempts to quit. */
    public void attemptQuit() {
        if (!UIUtilities.inModalState()) {
            if (closeFrames(true)) {
                quitIfNoSignificantWindowsOpen();
            }
        }
    }

    public void quitIfNoSignificantWindowsOpen() {
        if (mAllowQuitIfNoSignificantWindowsOpen) {
            for (Frame frame : Frame.getFrames()) {
                if (frame.isShowing()) {
                    if (frame instanceof SignificantFrame || BaseWindow.hasOwnedWindowsShowing(frame)) {
                        return;
                    }
                }
            }
            mAllowQuitIfNoSignificantWindowsOpen = false;
            if (closeFrames(false)) {
                saveState();
                System.exit(0);
            }
            mAllowQuitIfNoSignificantWindowsOpen = true;
        }
    }

    private static void saveState() {
        try {
            RecentFilesMenu.saveToPreferences();
            Fonts.saveToPreferences();
            Preferences.getInstance().save();
        } catch (Exception exception) {
            // Ignore, since preferences may not have been initialized...
        }
    }

    private static boolean closeFrames(boolean significant) {
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof SignificantFrame == significant && frame.isShowing()) {
                try {
                    if (!CloseCommand.close(frame)) {
                        return false;
                    }
                } catch (Exception exception) {
                    Log.error(exception);
                }
            }
        }
        return true;
    }

    @Override
    public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
        if (!UIUtilities.inModalState()) {
            mAllowQuitIfNoSignificantWindowsOpen = false;
            if (closeFrames(true)) {
                if (closeFrames(false)) {
                    saveState();
                    response.performQuit();
                    return;
                }
            }
            mAllowQuitIfNoSignificantWindowsOpen = true;
        }
        response.cancelQuit();
    }
}
