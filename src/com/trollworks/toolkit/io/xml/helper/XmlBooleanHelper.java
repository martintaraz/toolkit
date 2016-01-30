/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.io.xml.helper;

import com.trollworks.toolkit.annotation.XmlDefaultBoolean;
import com.trollworks.toolkit.io.xml.XmlGenerator;
import com.trollworks.toolkit.io.xml.XmlParserContext;
import com.trollworks.toolkit.utility.text.Numbers;

import java.lang.reflect.Field;

import javax.xml.stream.XMLStreamException;

public class XmlBooleanHelper implements XmlObjectHelper {
	public static final XmlBooleanHelper SINGLETON = new XmlBooleanHelper();

	private XmlBooleanHelper() {
	}

	@Override
	public boolean canHandleClass(Class<?> clazz) {
		return Boolean.class == clazz;
	}

	@Override
	public void emitAsAttribute(XmlGenerator xml, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
		Boolean value = (Boolean) field.get(obj);
		if (value != null) {
			boolean primitive = value.booleanValue();
			XmlDefaultBoolean def = field.getAnnotation(XmlDefaultBoolean.class);
			if (def != null) {
				xml.addAttributeNot(name, primitive, def.value());
			} else {
				xml.addAttribute(name, primitive);
			}
		}
	}

	@Override
	public void loadAttributeValue(XmlParserContext context, Object obj, Field field, String name) throws XMLStreamException, ReflectiveOperationException {
		XmlDefaultBoolean def = field.getAnnotation(XmlDefaultBoolean.class);
		field.set(obj, Boolean.valueOf(context.getParser().isAttributeSet(name, def != null ? def.value() : false)));
	}

	@Override
	public void emitAsTag(XmlGenerator xml, String tag, Object obj) throws XMLStreamException {
		xml.startTag(tag);
		xml.addText(Numbers.format(((Boolean) obj).booleanValue()));
		xml.endTag();
	}
}
