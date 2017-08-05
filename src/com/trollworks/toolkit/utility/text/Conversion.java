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

package com.trollworks.toolkit.utility.text;

import java.awt.Dimension;
import java.awt.Point;

/** Provides conversion routines from one type to another. */
public class Conversion {
    private static final String COMMA = ","; //$NON-NLS-1$

    /**
     * @param dim The dimension.
     * @return A string version of the {@link Dimension} that can be extracted using
     *         {@link #extractDimension(String)}.
     */
    public static String createString(Dimension dim) {
        return dim.width + COMMA + dim.height;
    }

    /**
     * Extracts a {@link Dimension}from the string.
     *
     * @param buffer The string to extract from.
     * @return The extracted {@link Dimension}, or <code>null</code> if valid data can't be found.
     */
    public static Dimension extractDimension(String buffer) {
        int[] values = extractIntegers(buffer);
        if (values.length == 2) {
            return new Dimension(values[0], values[1]);
        }
        return null;
    }

    /**
     * @param pt The point.
     * @return A string version of the {@link Point} that can be extracted using
     *         {@link #extractPoint(String)}.
     */
    public static String createString(Point pt) {
        return pt.x + COMMA + pt.y;
    }

    /**
     * Extracts a {@link Point}from the string.
     *
     * @param buffer The string to extract from.
     * @return The extracted {@link Point}, or <code>null</code> if valid data can't be found.
     */
    public static Point extractPoint(String buffer) {
        int[] values = extractIntegers(buffer);
        if (values.length == 2) {
            return new Point(values[0], values[1]);
        }
        return null;
    }

    /**
     * Extracts an <code>int</code> array from the string.
     *
     * @param buffer The buffer to extract from.
     * @return An array of integers.
     */
    public static int[] extractIntegers(String buffer) {
        if (buffer != null && buffer.length() > 0) {
            String[] buffers = buffer.split(COMMA);
            int[] values = new int[buffers.length];

            for (int i = 0; i < buffers.length; i++) {
                try {
                    values[i] = Integer.parseInt(buffers[i].trim());
                } catch (Exception exception) {
                    values[i] = 0;
                }
            }
            return values;
        }
        return new int[0];
    }
}
