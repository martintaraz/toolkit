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

package com.trollworks.toolkit.ui.widget.tree;

import java.util.Comparator;

/** Compares {@link TreeRow} indexes. */
public class TreeRowIndexComparator implements Comparator<TreeRow> {
    /** The one and only instance. */
    public static final TreeRowIndexComparator INSTANCE = new TreeRowIndexComparator();

    private TreeRowIndexComparator() {
        // Here just to prevent multiple copies.
    }

    @Override
    public int compare(TreeRow o1, TreeRow o2) {
        int i1 = o1.getIndex();
        int i2 = o2.getIndex();
        if (i1 < i2) {
            return -1;
        }
        if (i1 > i2) {
            return 1;
        }
        return 0;
    }
}
