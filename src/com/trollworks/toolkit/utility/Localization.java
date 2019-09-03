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

package com.trollworks.toolkit.utility;

import com.trollworks.toolkit.annotation.Localizations;
import com.trollworks.toolkit.annotation.Localize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Loads localized messages into classes. This provides similar functionality to the
 * org.eclipse.osgi.util.NLS class, but this way we can get localized strings without requiring any
 * part of Eclipse. Prior to this class being used, you can set a system property named
 * "locale.file" to cause the locale used to be determined based upon the contents of the file
 * rather than the regular mechanism. The first non-blank line in the file will be used as the
 * locale name.
 */
public class Localization implements PrivilegedAction<Object> {
    private static final int      MOD_EXPECTED = Modifier.STATIC;
    private static final int      MOD_MASK     = MOD_EXPECTED | Modifier.FINAL;
    private static final String[] LOCALES;
    private Class<?>              mClass;
    private String                mBundleName;
    private boolean               mIsAccessible;

    static {
        Locale override = getLocaleOverride();
        if (override != null) {
            Locale.setDefault(override);
        }
        String            nl     = Locale.getDefault().toString();
        ArrayList<String> result = new ArrayList<>(4);
        while (true) {
            result.add(nl);
            int lastSeparator = nl.lastIndexOf('_');
            if (lastSeparator == -1) {
                break;
            }
            nl = nl.substring(0, lastSeparator);
        }
        result.add("");
        LOCALES = result.toArray(new String[result.size()]);
    }

    /** @return The file a locale will be read from, or <code>null</code> if no property was set. */
    public static final File getLocaleOverrideFile() {
        String localeFile = System.getProperty("locale.file", "");
        if (!localeFile.isEmpty()) {
            File base = new File(System.getProperty("user.home", "."));
            if (Platform.isMacintosh()) {
                base = new File(base, "Library/Preferences");
            } else if (Platform.isWindows()) {
                base = new File(base, "Local Settings/Application Data");
            }
            base.mkdirs();
            return new File(base, localeFile);
        }
        return null;
    }

    /** @return The locale override, or <code>null</code> if no property was set. */
    public static final Locale getLocaleOverride() {
        File localeFile = getLocaleOverrideFile();
        if (localeFile != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(localeFile)))) {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        return new Locale(line);
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
        return null;
    }

    /**
     * Initialize the calling class with the values from its message bundle. <b>NOTE</b>: This can
     * only be called if the calling class shares the same {@link ClassLoader} as the
     * {@link Localization} class. Classes from different Eclipse plugins, for example, do
     * <b>NOT</b> share class loaders and cannot use this method unless they are in the same plugin
     * as the {@link Localization} class.
     */
    public static void initialize() {
        try {
            initialize(Class.forName(new Exception().getStackTrace()[1].getClassName()));
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
        }
    }

    /**
     * Initialize the specified class with the values from its message bundle.
     *
     * @param theClass The class to process.
     */
    public static void initialize(final Class<?> theClass) {
        Localization action = new Localization(theClass);
        if (System.getSecurityManager() == null) {
            action.run();
        } else {
            AccessController.doPrivileged(action);
        }
    }

    private Localization(Class<?> theClass) {
        mClass        = theClass;
        mBundleName   = theClass.getName();
        mIsAccessible = (mClass.getModifiers() & Modifier.PUBLIC) != 0;
    }

    @Override
    public Object run() {
        for (Field field : mClass.getDeclaredFields()) {
            if ((field.isAnnotationPresent(Localize.class) || field.isAnnotationPresent(Localizations.class)) && (field.getModifiers() & MOD_MASK) == MOD_EXPECTED) {
                try {
                    if (!mIsAccessible || (field.getModifiers() & Modifier.PUBLIC) == 0) {
                        field.setAccessible(true);
                    }
                    field.set(null, getMessage(field));
                } catch (Exception e) {
                    System.err.println("Unable to set value of localized message for '" + field.getName() + "' in " + mBundleName);
                }
            }
        }
        return null;
    }

    private static String getMessage(Field field) {
        Localize[] annotations = field.getAnnotationsByType(Localize.class);
        if (annotations.length > 0) {
            for (String locale : LOCALES) {
                for (Localize one : annotations) {
                    if (locale.equals(one.locale())) {
                        return one.value();
                    }
                }
            }
        }
        return "*!*" + field.getName() + "*!*";
    }
}
