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

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.utility.Localization;

import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.SwingConstants;

/** Provides text manipulation. */
public class Text {
    @Localize("a")
    private static String A;
    @Localize("an")
    private static String AN;
    @Localize("was")
    @Localize(locale = "ru", value = "был")
    @Localize(locale = "de", value = "wurde")
    @Localize(locale = "es", value = "era")
    private static String WAS;
    @Localize("were")
    @Localize(locale = "ru", value = "где")
    @Localize(locale = "de", value = "wurden")
    @Localize(locale = "es", value = "eran")
    private static String WERE;

    static {
        Localization.initialize();
    }

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static final String SPACE      = " ";                                                                               																					//$NON-NLS-1$
    private static final String NEWLINE    = "\n";                                                                              																					//$NON-NLS-1$
    private static final char   ELLIPSIS   = '\u2026';

    /**
     * @param text The text to check.
     * @return "a" or "an", as appropriate for the text that will be following it.
     */
    public static final String aAn(String text) {
        return Text.startsWithVowel(text) ? AN : A;
    }

    /**
     * @param amount The number of items.
     * @return "was" or "were", as appropriate for the number of items.
     */
    public static final String wasWere(int amount) {
        return amount == 1 ? WAS : WERE;
    }

    /**
     * @param ch The character to check.
     * @return <code>true</code> if the character is a vowel.
     */
    public static final boolean isVowel(char ch) {
        ch = Character.toLowerCase(ch);
        return ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u';
    }

    /**
     * @param text The text to check.
     * @return <code>true</code> if the text starts with a vowel.
     */
    public static final boolean startsWithVowel(String text) {
        if (text != null && !text.isEmpty()) {
            return isVowel(text.charAt(0));
        }
        return false;
    }

    /**
     * @param data The data to create a hex string for.
     * @return A string of two character hexadecimal values for each byte.
     */
    public final static String bytesToHex(byte[] data) {
        return bytesToHex(data, 0, data.length);
    }

    /**
     * @param data The data to create a hex string for.
     * @param offset The starting index.
     * @param length The number of bytes to use.
     * @return A string of two character hexadecimal values for each byte.
     */
    public final static String bytesToHex(byte[] data, int offset, int length) {
        StringBuilder buffer = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            byte b = data[i + offset];
            buffer.append(HEX_DIGITS[b >>> 4 & 15]);
            buffer.append(HEX_DIGITS[b & 15]);
        }
        return buffer.toString();
    }

    /**
     * @param text The text to reflow.
     * @return The revised text.
     */
    @SuppressWarnings("nls")
    public static final String reflow(String text) {
        if (text == null) {
            return "";
        }
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(text.replaceAll("[\\x00-\\x08]", "").replaceAll("[\\x0b\\x0c]", "").replaceAll("[\\x0e-\\x1f]", "").replaceAll("[\\x7f-\\x9f]", "").replaceAll("\r\n", "\n").replace('\r', '\n').replaceAll("[ \t\f]+", " ").trim(), "\n", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("\n")) {
                count++;
            } else {
                if (count == 1) {
                    buffer.append(" ");
                } else if (count > 1) {
                    buffer.append("\n\n");
                }
                count = 0;
                buffer.append(token);
            }
        }
        return buffer.toString();
    }

    /**
     * If the text doesn't fit in the specified character count, it will be shortened and an ellipse
     * ("...") will be added.
     *
     * @param text The text to work on.
     * @param count The maximum character count.
     * @param truncationPolicy One of {@link SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or
     *            {@link SwingConstants#RIGHT}.
     * @return The adjusted text.
     */
    public static final String truncateIfNecessary(String text, int count, int truncationPolicy) {
        int tCount = text.length();

        count = tCount - count;
        if (count > 0) {
            count++; // Count is now the amount to remove from the string
            if (truncationPolicy == SwingConstants.LEFT) {
                return ELLIPSIS + text.substring(count);
            }
            if (truncationPolicy == SwingConstants.CENTER) {
                int remaining = tCount - count;
                int left = remaining / 2;
                int right = remaining - left;
                StringBuilder buffer = new StringBuilder(remaining + 1);

                if (left > 0) {
                    buffer.append(text.substring(0, left));
                }
                buffer.append(ELLIPSIS);
                if (right > 0) {
                    buffer.append(text.substring(tCount - right));
                }
                return buffer.toString();
            }
            return text.substring(0, tCount - count) + ELLIPSIS;
        }
        return text;
    }

    /**
     * Convert text from other line ending formats into our internal format.
     *
     * @param data The text to convert.
     * @return The converted text.
     */
    public static final String standardizeLineEndings(String data) {
        return standardizeLineEndings(data, NEWLINE);
    }

    /**
     * Convert text from other line ending formats into a specific format.
     *
     * @param data The text to convert.
     * @param lineEnding The desired line ending.
     * @return The converted text.
     */
    public static final String standardizeLineEndings(String data, String lineEnding) {
        int length = data.length();
        StringBuilder buffer = new StringBuilder(length);
        char ignoreCh = 0;

        for (int i = 0; i < length; i++) {
            char ch = data.charAt(i);

            if (ch == ignoreCh) {
                ignoreCh = 0;
            } else if (ch == '\r') {
                ignoreCh = '\n';
                buffer.append(lineEnding);
            } else if (ch == '\n') {
                ignoreCh = '\r';
                buffer.append(lineEnding);
            } else {
                ignoreCh = 0;
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }

    /**
     * Extracts lines of text from the specified data.
     *
     * @param data The text to extract lines from.
     * @param tabWidth The width (in spaces) of a tab character. Pass in <code>0</code> or less to
     *            leave tab characters in place.
     * @return The lines of text.
     */
    public static final ArrayList<String> extractLines(String data, int tabWidth) {
        int length = data.length();
        StringBuilder buffer = new StringBuilder(length);
        char ignoreCh = 0;
        ArrayList<String> lines = new ArrayList<>();
        int column = 0;

        for (int i = 0; i < length; i++) {
            char ch = data.charAt(i);

            if (ch == ignoreCh) {
                ignoreCh = 0;
            } else if (ch == '\r') {
                ignoreCh = '\n';
                column = 0;
                lines.add(buffer.toString());
                buffer.setLength(0);
            } else if (ch == '\n') {
                ignoreCh = '\r';
                column = 0;
                lines.add(buffer.toString());
                buffer.setLength(0);
            } else if (ch == '\t' && tabWidth > 0) {
                int spaces = tabWidth - column % tabWidth;

                ignoreCh = 0;
                while (--spaces >= 0) {
                    buffer.append(' ');
                    column++;
                }
            } else {
                ignoreCh = 0;
                column++;
                buffer.append(ch);
            }
        }
        if (buffer.length() > 0) {
            lines.add(buffer.toString());
        }

        return lines;
    }

    /**
     * @param amt The size of the string to create.
     * @param filler The character to fill it with.
     * @return A string filled with a specific character.
     */
    public static String makeFiller(int amt, char filler) {
        StringBuilder buffer = new StringBuilder(amt);

        for (int i = 0; i < amt; i++) {
            buffer.append(filler);
        }
        return buffer.toString();
    }

    /**
     * Creates a "note" whose second and subsequent lines are indented by the amount of the marker,
     * which is prepended to the first line.
     *
     * @param marker The prefix to use on the first line.
     * @param note The text of the note.
     * @return The formatted note.
     */
    public static String makeNote(String marker, String note) {
        StringBuilder buffer = new StringBuilder(note.length() * 2);
        String indent = makeFiller(marker.length() + 1, ' ');
        StringTokenizer tokenizer = new StringTokenizer(note, NEWLINE);

        if (tokenizer.hasMoreTokens()) {
            buffer.append(marker);
            buffer.append(SPACE);
            buffer.append(tokenizer.nextToken());
            buffer.append(NEWLINE);

            while (tokenizer.hasMoreTokens()) {
                buffer.append(indent);
                buffer.append(tokenizer.nextToken());
                buffer.append(NEWLINE);
            }
        }

        return buffer.toString();
    }

    /**
     * @param text The text to wrap.
     * @param charCount The maximum character width to allow.
     * @return A new, wrapped version of the text.
     */
    public static String wrapToCharacterCount(String text, int charCount) {
        StringBuilder buffer = new StringBuilder(text.length() * 2);
        StringBuilder lineBuffer = new StringBuilder(charCount + 1);
        StringTokenizer tokenizer = new StringTokenizer(text + NEWLINE, NEWLINE, true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (token.equals(NEWLINE)) {
                buffer.append(token);
            } else {
                StringTokenizer tokenizer2 = new StringTokenizer(token, " \t", true); //$NON-NLS-1$
                int length = 0;

                lineBuffer.setLength(0);
                while (tokenizer2.hasMoreTokens()) {
                    String token2 = tokenizer2.nextToken();
                    int tokenLength = token2.length();

                    if (length == 0 && token2.equals(SPACE)) {
                        continue;
                    }
                    if (length == 0 || length + tokenLength <= charCount) {
                        lineBuffer.append(token2);
                        length += tokenLength;
                    } else {
                        buffer.append(lineBuffer);
                        buffer.append(NEWLINE);
                        lineBuffer.setLength(0);
                        if (!token2.equals(SPACE)) {
                            lineBuffer.append(token2);
                            length = tokenLength;
                        } else {
                            length = 0;
                        }
                    }
                }
                if (length > 0) {
                    buffer.append(lineBuffer);
                }
            }
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    public static String wrapPlainTextForToolTip(String text) {
        if (text != null && !text.isEmpty() && !text.startsWith("<html>")) { //$NON-NLS-1$
            StringBuilder buffer = new StringBuilder();
            buffer.append("<html><body>"); //$NON-NLS-1$
            buffer.append(htmlEscape(wrapToCharacterCount(text, 40)).replaceAll(NEWLINE, "<br>")); //$NON-NLS-1$
            buffer.append("</body></html>"); //$NON-NLS-1$
            return buffer.toString();
        }
        return text;
    }

    public static String htmlEscape(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length == 0) {
            return str;
        }
        StringBuilder buffer = new StringBuilder(length + 16);
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '&':
                    buffer.append("&amp;"); //$NON-NLS-1$
                    break;
                case '<':
                    buffer.append("&lt;"); //$NON-NLS-1$
                    break;
                case '>':
                    buffer.append("&gt;"); //$NON-NLS-1$
                    break;
                case '"':
                    buffer.append("&quot;"); //$NON-NLS-1$
                    break;
                case '\'':
                    buffer.append("&#39;"); //$NON-NLS-1$
                    break;
                case '/':
                    buffer.append("&#47;"); //$NON-NLS-1$
                    break;
                default:
                    buffer.append(ch);
            }
        }
        return buffer.toString();
    }
}
