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

package com.trollworks.toolkit.io.xml;

import java.util.HashMap;

import org.eclipse.collections.api.stack.primitive.MutableIntStack;
import org.eclipse.collections.impl.stack.mutable.primitive.IntArrayStack;

/** Provides temporary storage when loading an object from XML. */
public class XmlParserContext extends HashMap<String, Object> {
    private XmlParser mParser;
    private MutableIntStack mVersionStack = new IntArrayStack();

    /** @param parser The {@link XmlParser} being used. */
    public XmlParserContext(XmlParser parser) {
        mParser = parser;
    }

    /** @return The {@link XmlParser} being used. */
    public XmlParser getParser() {
        return mParser;
    }

    /** @return The current version on the stack. */
    public int getVersion() {
        return mVersionStack.peek();
    }

    /** @param version The version to push onto the stack. */
    public void pushVersion(int version) {
        mVersionStack.push(version);
    }

    /** Removes the current version from the stack, restoring whatever was before it. */
    public void popVersion() {
        mVersionStack.pop();
    }
}
