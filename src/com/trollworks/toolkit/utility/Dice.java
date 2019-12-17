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

package com.trollworks.toolkit.utility;

import java.util.Random;

/** Simulates dice. */
public class Dice implements Cloneable {
    private static final Random  RANDOM                = new Random();
    private static       boolean SHOW_SINGLE_DIE_COUNT = true;
    private static       int     ASSUMED_SIDE_COUNT;
    private static       boolean EXTRA_DICE_FROM_MODIFIERS;
    private              int     mCount;
    private              int     mSides;
    private              int     mModifier;
    private              int     mMultiplier;
    private              int     mAltCount;
    private              int     mAltModifier;

    /**
     * Determines whether a "1" will be shown when a single die is being displayed.<br>
     * <br>
     * By default, this is set to {@code true}.
     *
     * @param show {@code true} to show "1d6", false to show "d6".
     */
    public static final void setShowSingleDieCount(boolean show) {
        SHOW_SINGLE_DIE_COUNT = show;
    }

    /**
     * Sets the assumed number of sides per die. When this is set to a positive value greater than
     * 0, the {@link #toString()} method will emit dice notations without the dice sides if it
     * matches this value. For example, if the assumed side count is 6, then rather than {@code
     * 2d6+2}, {@link #toString()} would generate {@code 2d+2}. Likewise, input dice specifications
     * for the {@link #Dice(String)} constructor will use this value if the number of sides is
     * omitted.<br>
     * <br>
     * By default, this is set to {@code 0}.
     *
     * @param sides The number of sides to assume.
     */
    public static final void setAssumedSideCount(int sides) {
        ASSUMED_SIDE_COUNT = sides;
    }

    /**
     * By default, this is set to {@code false}.
     *
     * @param convert {@code true} if modifiers greater than or equal to the average result of the
     *                base die should be converted to extra dice. For example, {@code 1d6+8} will
     *                become {@code 3d6+1}.
     */
    public static final void setConvertModifiersToExtraDice(boolean convert) {
        EXTRA_DICE_FROM_MODIFIERS = convert;
    }

    public static final int roll(String text) {
        Dice dice = new Dice(text);
        return dice.roll();
    }

    /** Creates a new 1d6 dice object. */
    public Dice() {
        this(1, 6, 0, 1);
    }

    /**
     * Creates a new {@link Dice} based on the given text.
     *
     * @param text The text to create a {@link Dice} object from.
     */
    public Dice(String text) {
        StringBuilder buffer = new StringBuilder(text.trim().toLowerCase());
        mCount = extractValue(buffer);
        char ch = nextChar(buffer);
        if (ch == 'd') {
            buffer.deleteCharAt(0);
            mSides = extractValue(buffer);
            if (mSides == 0) {
                mSides = ASSUMED_SIDE_COUNT;
            }
            if (mCount < 1) {
                mCount = 1;
            }
            ch = nextChar(buffer);
        }
        if (ch == '+' || ch == '-') {
            boolean negative = ch == '-';
            buffer.deleteCharAt(0);
            mModifier = extractValue(buffer);
            if (negative) {
                mModifier = -mModifier;
            }
            ch = nextChar(buffer);
        }
        if (ch == 'x') {
            buffer.deleteCharAt(0);
            mMultiplier = extractValue(buffer);
        }
        if (mMultiplier == 0) {
            mMultiplier = 1;
        }
        if (mCount != 0 && mSides == 0 && mModifier == 0) {
            mModifier = mCount;
            mCount = 0;
        }
    }

    /**
     * @param text The text containing a dice specification.
     * @return A 2 element array of integers with the first element containing the starting index of
     *         the dice specification and the second element containing the last index of the dice
     *         specification. If there was no dice specification detected, then {@code null} will be
     *         returned instead.
     */
    public static final int[] extractDicePosition(String text) {
        int start = -1;
        int max   = text.length();
        int state = 0;
        for (int i = 0; i < max; i++) {
            char ch = text.charAt(i);
            switch (state) {
            case 0:
                if (ch >= '0' && ch <= '9') {
                    if (start == -1) {
                        start = i;
                    }
                } else if (ch != ' ') {
                    if (ch == 'd') {
                        state = 1;
                    } else if (ch == '+' || ch == '-') {
                        state = 2;
                    }
                }
                break;
            case 1:
                if (ch != ' ' && (ch < '0' || ch > '9')) {
                    if (ch == '+' || ch == '-') {
                        state = 2;
                    } else if (ch == 'x') {
                        state = 3;
                    } else {
                        state = 4;
                    }
                }
                break;
            case 2:
                if ((ch < '0' || ch > '9') && ch != ' ') {
                    state = ch == 'x' ? 3 : 4;
                }
                break;
            case 3:
                if ((ch < '0' || ch > '9') && ch != ' ') {
                    state = 4;
                }
                break;
            default:
                break;
            }
            if (state == 4) {
                max = i;
                break;
            }
        }
        if (start != -1) {
            while (start < max && text.charAt(start) == ' ') {
                start++;
            }
            max--;
            while (max > start && text.charAt(max) == ' ') {
                max--;
            }
            if (start < max) {
                return new int[]{start, max};
            }
        }
        return null;
    }

    private static char nextChar(StringBuilder buffer) {
        return buffer.length() > 0 ? buffer.charAt(0) : 0;
    }

    private static int extractValue(StringBuilder buffer) {
        int value = 0;
        while (buffer.length() > 0) {
            char ch = buffer.charAt(0);
            if (ch >= '0' && ch <= '9') {
                value *= 10;
                value += ch - '0';
            } else if (ch != ' ') {
                break;
            }
            buffer.deleteCharAt(0);
        }
        return value;
    }

    /**
     * Creates a new d6 dice object.
     *
     * @param count The number of dice.
     */
    public Dice(int count) {
        this(count, 6, 0, 1);
    }

    /**
     * Creates a new d6 dice object.
     *
     * @param count    The number of dice.
     * @param modifier The bonus or penalty to the roll.
     */
    public Dice(int count, int modifier) {
        this(count, 6, modifier, 1);
    }

    /**
     * Creates a new d6 dice object.
     *
     * @param count      The number of dice.
     * @param modifier   The bonus or penalty to the roll.
     * @param multiplier A multiplier for the roll.
     */
    public Dice(int count, int modifier, int multiplier) {
        this(count, 6, modifier, multiplier);
    }

    /**
     * Creates a new dice object.
     *
     * @param count      The number of dice.
     * @param sides      The number of sides on each die.
     * @param modifier   The bonus or penalty to the roll.
     * @param multiplier A multiplier for the roll.
     */
    public Dice(int count, int sides, int modifier, int multiplier) {
        mCount = Math.max(count, 0);
        mSides = Math.max(sides, 0);
        mModifier = modifier;
        mMultiplier = multiplier;
    }

    @Override
    public Dice clone() {
        try {
            return (Dice) super.clone();
        } catch (CloneNotSupportedException cnse) {
            return null; // Can't happen.
        }
    }

    /**
     * Adds a modifier to the dice.
     *
     * @param modifier The modifier to add.
     */
    public void add(int modifier) {
        mModifier += modifier;
    }

    /**
     * Multiplies all components.
     *
     * @param multiply The amount to multiply each die component.
     */
    public void multiply(int multiply) {
        mCount *= multiply;
        mModifier *= multiply;
        if (mMultiplier != 1) {
            mMultiplier *= multiply;
        }
    }

    /** @return The number of dice to roll. */
    public int getDieCount() {
        return mCount;
    }

    /** @return The number of sides on each die. */
    public int getDieSides() {
        return mSides;
    }

    /** @return The bonus or penalty to the roll. */
    public int getModifier() {
        return mModifier;
    }

    /** @return The multiplier for the roll. */
    public int getMultiplier() {
        return mMultiplier;
    }

    /** @return The result of rolling the dice. */
    public int roll() {
        return roll(RANDOM);
    }

    /**
     * @param randomizer A {@link Random} object to use.
     * @return The result of rolling the dice.
     */
    public int roll(Random randomizer) {
        int result = 0;
        updateAlt();
        if (mSides > 0) {
            for (int i = 0; i < mAltCount; i++) {
                result += 1 + randomizer.nextInt(mSides);
            }
        }
        return (result + mAltModifier) * mMultiplier;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        updateAlt();
        if (mAltCount > 0 && mSides > 0) {
            if (SHOW_SINGLE_DIE_COUNT || mAltCount > 1) {
                buffer.append(mAltCount);
            }
            buffer.append('d');
            if (mSides != ASSUMED_SIDE_COUNT) {
                buffer.append(mSides);
            }
        }
        if (mAltModifier > 0) {
            buffer.append('+');
            buffer.append(mAltModifier);
        } else if (mAltModifier < 0) {
            buffer.append(mAltModifier);
        }
        if (mMultiplier != 1) {
            buffer.append('x');
            buffer.append(mMultiplier);
        }
        if (buffer.length() == 0) {
            buffer.append('0');
        }
        return buffer.toString();
    }

    private void updateAlt() {
        mAltCount = mCount;
        mAltModifier = mModifier;
        if (EXTRA_DICE_FROM_MODIFIERS && mSides > 0) {
            int average = (mSides + 1) / 2;
            if ((mSides & 1) == 1) {
                // Odd number of sides, so average is a whole number
                mAltCount += mAltModifier / average;
                mAltModifier %= average;
            } else {
                // Even number of sides, so average has an extra half, which means we alternate
                while (mAltModifier > average) {
                    if (mAltModifier > 2 * average) {
                        mAltModifier -= 2 * average + 1;
                        mAltCount += 2;
                    } else {
                        mAltModifier -= average + 1;
                        mAltCount++;
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Dice) {
            Dice od = (Dice) obj;
            return mCount == od.mCount && mSides == od.mSides && mModifier == od.mModifier && mMultiplier == od.mMultiplier;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // Specifically want identity comparison
        return super.hashCode();
    }

    /**
     * @param count  The number of dice.
     * @param sides  The number of sides per die.
     * @param target The minimum value on one die to be considered a success.
     * @return The probability that at least one die will be equal to or greater than the target
     *         value.
     */
    public static double getDicePoolProbability(int count, int sides, int target) {
        return 1 - Math.pow(1 - (1 + sides - target) / (double) sides, count);
    }
}
