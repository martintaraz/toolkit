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

package com.trollworks.toolkit.ui.print;

import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.layout.PrecisionLayout;
import com.trollworks.toolkit.ui.layout.PrecisionLayoutData;
import com.trollworks.toolkit.ui.widget.Commitable;
import com.trollworks.toolkit.ui.widget.EditorField;
import com.trollworks.toolkit.ui.widget.LinkedLabel;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.text.DoubleFormatter;
import com.trollworks.toolkit.utility.units.LengthUnits;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.NumberUp;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.Sides;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;

/** Provides the basic page setup panel. */
public class PageSetupPanel extends JPanel implements ActionListener {
    private PrintService                        mService;
    private JComboBox<WrappedPrintService>      mServices;
    private JComboBox<PageOrientation>          mOrientation;
    private JComboBox<WrappedMediaSizeName>     mPaperType;
    private EditorField                         mTopMargin;
    private EditorField                         mLeftMargin;
    private EditorField                         mRightMargin;
    private EditorField                         mBottomMargin;
    private JComboBox<InkChromaticity>          mChromaticity;
    private JComboBox<PageSides>                mSides;
    private JComboBox<WrappedNumberUp>          mNumberUp;
    private JComboBox<Quality>                  mPrintQuality;
    private JComboBox<WrappedPrinterResolution> mResolution;

    /**
     * Creates a new page setup panel.
     *
     * @param service The {@link PrintService} to use.
     * @param set     The {@link PrintRequestAttributeSet} to use.
     */
    public PageSetupPanel(PrintService service, PrintRequestAttributeSet set) {
        super(new PrecisionLayout().setColumns(2));
        rebuild(service, set);
    }

    /** @return The service. */
    public PrintService getService() {
        return mService;
    }

    private void rebuild(PrintService service, PrintRequestAttributeSet set) {
        removeAll();
        mService = service;
        createPrinterCombo();
        rebuildSelf(set);
        revalidate();
        Window window = WindowUtils.getWindowForComponent(this);
        if (window != null) {
            window.setSize(window.getPreferredSize());
            GraphicsUtilities.forceOnScreen(window);
        }
    }

    /** @param set The current {@link PrintRequestAttributeSet}. */
    protected void rebuildSelf(PrintRequestAttributeSet set) {
        createPaperTypeCombo(set);
        createOrientationCombo(set);
        createSidesCombo(set);
        createNumberUpCombo(set);
        createChromaticityCombo(set);
        createPrintQualityCombo(set);
        createResolutionCombo(set);
        createMarginFields(set);
    }

    // This is only here because the compiler wouldn't let me do this:
    // new ObjectWrapper<PrintService>[0]
    class WrappedPrintService extends ObjectWrapper<PrintService> {
        WrappedPrintService(String title, PrintService object) {
            super(title, object);
        }
    }

    private void createPrinterCombo() {
        PrintService[] services = PrinterJob.lookupPrintServices();
        if (services.length == 0) {
            services = new PrintService[] { new DummyPrintService() };
        }
        WrappedPrintService[] serviceWrappers = new WrappedPrintService[services.length];
        int                   selection       = 0;
        for (int i = 0; i < services.length; i++) {
            serviceWrappers[i] = new WrappedPrintService(services[i].getName(), services[i]);
            if (services[i] == mService) {
                selection = i;
            }
        }
        mServices = new JComboBox<>(serviceWrappers);
        mServices.setSelectedIndex(selection);
        UIUtilities.setToPreferredSizeOnly(mServices);
        mServices.addActionListener(this);
        mService = services[selection];
        LinkedLabel label = new LinkedLabel(I18n.Text("Printer"), mServices);
        add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
        add(mServices);
    }

    // This is only here because the compiler wouldn't let me do this:
    // new ObjectWrapper<MediaSizeName>[0]
    class WrappedMediaSizeName extends ObjectWrapper<MediaSizeName> {
        WrappedMediaSizeName(String title, MediaSizeName object) {
            super(title, object);
        }
    }

    private void createPaperTypeCombo(PrintRequestAttributeSet set) {
        Media[] possible = (Media[]) mService.getSupportedAttributeValues(Media.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (possible != null && possible.length > 0) {
            Media current = (Media) PrintUtilities.getSetting(mService, set, Media.class, true);
            if (current == null) {
                current = MediaSizeName.NA_LETTER;
            }
            ArrayList<WrappedMediaSizeName> types     = new ArrayList<>();
            int                             selection = 0;
            int                             index     = 0;
            for (Media one : possible) {
                if (one instanceof MediaSizeName) {
                    MediaSizeName name = (MediaSizeName) one;
                    types.add(new WrappedMediaSizeName(cleanUpMediaSizeName(name), name));
                    if (name == current) {
                        selection = index;
                    }
                    index++;
                }
            }
            mPaperType = new JComboBox<>(types.toArray(new WrappedMediaSizeName[0]));
            mPaperType.setSelectedIndex(selection);
            UIUtilities.setToPreferredSizeOnly(mPaperType);
            LinkedLabel label = new LinkedLabel(I18n.Text("Paper Type"), mPaperType);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mPaperType);
        } else {
            mPaperType = null;
        }
    }

    private static String cleanUpMediaSizeName(MediaSizeName msn) {
        StringBuilder   builder   = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(msn.toString(), "- ", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (token.equalsIgnoreCase("na")) {
                builder.append("US");
            } else if (token.equalsIgnoreCase("iso")) {
                builder.append("ISO");
            } else if (token.equalsIgnoreCase("jis")) {
                builder.append("JIS");
            } else if (token.equals("-")) {
                builder.append(" ");
            } else if (token.length() > 1) {
                builder.append(Character.toUpperCase(token.charAt(0)));
                builder.append(token.substring(1));
            } else {
                builder.append(token.toUpperCase());
            }
        }

        return builder.toString();
    }

    private void createOrientationCombo(PrintRequestAttributeSet set) {
        OrientationRequested[] orientations = (OrientationRequested[]) mService.getSupportedAttributeValues(OrientationRequested.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (orientations != null && orientations.length > 0) {
            HashSet<OrientationRequested> possible = new HashSet<>();
            for (OrientationRequested one : orientations) {
                possible.add(one);
            }
            ArrayList<PageOrientation> choices = new ArrayList<>();
            for (PageOrientation orientation : PageOrientation.values()) {
                if (possible.contains(orientation.getOrientationRequested())) {
                    choices.add(orientation);
                }
            }
            mOrientation = new JComboBox<>(choices.toArray(new PageOrientation[0]));
            mOrientation.setSelectedItem(PrintUtilities.getPageOrientation(mService, set));
            UIUtilities.setToPreferredSizeOnly(mOrientation);
            LinkedLabel label = new LinkedLabel(I18n.Text("Orientation"), mOrientation);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mOrientation);
        } else {
            mOrientation = null;
        }
    }

    private void createSidesCombo(PrintRequestAttributeSet set) {
        Sides[] sides = (Sides[]) mService.getSupportedAttributeValues(Sides.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (sides != null && sides.length > 0) {
            HashSet<Sides> possible = new HashSet<>();
            for (Sides one : sides) {
                possible.add(one);
            }
            ArrayList<PageSides> choices = new ArrayList<>();
            for (PageSides side : PageSides.values()) {
                if (possible.contains(side.getSides())) {
                    choices.add(side);
                }
            }
            mSides = new JComboBox<>(choices.toArray(new PageSides[0]));
            mSides.setSelectedItem(PrintUtilities.getSides(mService, set));
            UIUtilities.setToPreferredSizeOnly(mSides);
            LinkedLabel label = new LinkedLabel(I18n.Text("Sides"), mSides);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mSides);
        } else {
            mSides = null;
        }
    }

    // This is only here because the compiler wouldn't let me do this:
    // new ObjectWrapper<NumberUp>[0]
    class WrappedNumberUp extends ObjectWrapper<NumberUp> {
        WrappedNumberUp(String title, NumberUp object) {
            super(title, object);
        }
    }

    private void createNumberUpCombo(PrintRequestAttributeSet set) {
        NumberUp[] numUp = (NumberUp[]) mService.getSupportedAttributeValues(NumberUp.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (numUp != null && numUp.length > 0) {
            NumberUp          current   = PrintUtilities.getNumberUp(mService, set);
            WrappedNumberUp[] wrappers  = new WrappedNumberUp[numUp.length];
            int               selection = 0;
            for (int i = 0; i < numUp.length; i++) {
                wrappers[i] = new WrappedNumberUp(numUp[i].toString(), numUp[i]);
                if (numUp[i] == current) {
                    selection = i;
                }
            }
            mNumberUp = new JComboBox<>(wrappers);
            mNumberUp.setSelectedIndex(selection);
            UIUtilities.setToPreferredSizeOnly(mNumberUp);
            LinkedLabel label = new LinkedLabel(I18n.Text("Number Up"), mNumberUp);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mNumberUp);
        } else {
            mNumberUp = null;
        }
    }

    private void createChromaticityCombo(PrintRequestAttributeSet set) {
        Chromaticity[] chromacities = (Chromaticity[]) mService.getSupportedAttributeValues(Chromaticity.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (chromacities != null && chromacities.length > 0) {
            HashSet<Chromaticity> possible = new HashSet<>();
            for (Chromaticity one : chromacities) {
                possible.add(one);
            }
            ArrayList<InkChromaticity> choices = new ArrayList<>();
            for (InkChromaticity chromaticity : InkChromaticity.values()) {
                if (possible.contains(chromaticity.getChromaticity())) {
                    choices.add(chromaticity);
                }
            }
            mChromaticity = new JComboBox<>(choices.toArray(new InkChromaticity[0]));
            mChromaticity.setSelectedItem(PrintUtilities.getChromaticity(mService, set, true));
            UIUtilities.setToPreferredSizeOnly(mChromaticity);
            LinkedLabel label = new LinkedLabel(I18n.Text("Color"), mChromaticity);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mChromaticity);
        } else {
            mChromaticity = null;
        }
    }

    private void createPrintQualityCombo(PrintRequestAttributeSet set) {
        PrintQuality[] qualities = (PrintQuality[]) mService.getSupportedAttributeValues(PrintQuality.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (qualities != null && qualities.length > 0) {
            HashSet<PrintQuality> possible = new HashSet<>();
            for (PrintQuality one : qualities) {
                possible.add(one);
            }
            ArrayList<Quality> choices = new ArrayList<>();
            for (Quality quality : Quality.values()) {
                if (possible.contains(quality.getPrintQuality())) {
                    choices.add(quality);
                }
            }
            mPrintQuality = new JComboBox<>(choices.toArray(new Quality[0]));
            mPrintQuality.setSelectedItem(PrintUtilities.getPrintQuality(mService, set, true));
            UIUtilities.setToPreferredSizeOnly(mPrintQuality);
            LinkedLabel label = new LinkedLabel(I18n.Text("Quality"), mPrintQuality);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mPrintQuality);
        } else {
            mPrintQuality = null;
        }
    }

    // This is only here because the compiler wouldn't let me do this:
    // new ObjectWrapper<PrinterResolution>[0]
    class WrappedPrinterResolution extends ObjectWrapper<PrinterResolution> {
        WrappedPrinterResolution(String title, PrinterResolution object) {
            super(title, object);
        }
    }

    private void createResolutionCombo(PrintRequestAttributeSet set) {
        PrinterResolution[] resolutions = (PrinterResolution[]) mService.getSupportedAttributeValues(PrinterResolution.class, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        if (resolutions != null && resolutions.length > 0) {
            PrinterResolution          current   = PrintUtilities.getResolution(mService, set, true);
            WrappedPrinterResolution[] wrappers  = new WrappedPrinterResolution[resolutions.length];
            int                        selection = 0;
            for (int i = 0; i < resolutions.length; i++) {
                wrappers[i] = new WrappedPrinterResolution(generateResolutionTitle(resolutions[i]), resolutions[i]);
                if (resolutions[i] == current) {
                    selection = i;
                }
            }
            mResolution = new JComboBox<>(wrappers);
            mResolution.setSelectedIndex(selection);
            UIUtilities.setToPreferredSizeOnly(mResolution);
            LinkedLabel label = new LinkedLabel(I18n.Text("Resolution"), mResolution);
            add(label, new PrecisionLayoutData().setEndHorizontalAlignment());
            add(mResolution);
        } else {
            mResolution = null;
        }
    }

    private static String generateResolutionTitle(PrinterResolution res) {
        StringBuilder buffer = new StringBuilder();
        int           x      = res.getCrossFeedResolution(ResolutionSyntax.DPI);
        int           y      = res.getFeedResolution(ResolutionSyntax.DPI);

        buffer.append(Integer.toString(x));
        if (x != y) {
            buffer.append(" x ");
            buffer.append(Integer.toString(y));
        }
        buffer.append(I18n.Text(" dpi"));
        return buffer.toString();
    }

    private void createMarginFields(PrintRequestAttributeSet set) {
        JLabel label = new JLabel(I18n.Text("<html>Margins<br>(inches)"), SwingConstants.RIGHT);
        add(label, new PrecisionLayoutData().setEndHorizontalAlignment().setMiddleVerticalAlignment());
        JPanel   wrapper = new JPanel(new PrecisionLayout().setEqualColumns(true).setColumns(3));
        double[] margins = PrintUtilities.getPageMargins(mService, set, LengthUnits.IN);
        wrapper.add(new JPanel());
        mTopMargin = createMarginField(margins[0], wrapper);
        wrapper.add(new JPanel());
        mLeftMargin = createMarginField(margins[1], wrapper);
        wrapper.add(new JPanel());
        mRightMargin = createMarginField(margins[3], wrapper);
        wrapper.add(new JPanel());
        mBottomMargin = createMarginField(margins[2], wrapper);
        wrapper.add(new JPanel());
        add(wrapper);
    }

    private static EditorField createMarginField(double margin, JPanel wrapper) {
        EditorField field = new EditorField(new DefaultFormatterFactory(new DoubleFormatter(0, 999.999, false)), null, SwingConstants.RIGHT, Double.valueOf(margin), Double.valueOf(999.999), null);
        UIUtilities.setToPreferredSizeOnly(field);
        wrapper.add(field);
        return field;
    }

    /**
     * Accepts the changes made by the user and incorporates them into the specified attribute set.
     *
     * @param set The set to modify.
     * @return The {@link PrintService} selected by the user.
     */
    public PrintService accept(PrintRequestAttributeSet set) {
        Commitable.sendCommitToFocusOwner();
        mService = UIUtilities.getTypedSelectedItemFromCombo(mServices).getObject();
        if (mOrientation != null) {
            PrintUtilities.setPageOrientation(set, (PageOrientation) mOrientation.getSelectedItem());
        }
        if (mPaperType != null) {
            PrintUtilities.setPaperSize(mService, set, PrintUtilities.getMediaDimensions(((WrappedMediaSizeName) mPaperType.getSelectedItem()).getObject(), LengthUnits.IN), LengthUnits.IN);
        }
        PrintUtilities.setPageMargins(mService, set, new double[] { ((Double) mTopMargin.getValue()).doubleValue(), ((Double) mLeftMargin.getValue()).doubleValue(), ((Double) mBottomMargin.getValue()).doubleValue(), ((Double) mRightMargin.getValue()).doubleValue() }, LengthUnits.IN);
        if (mChromaticity != null) {
            PrintUtilities.setChromaticity(set, (InkChromaticity) mChromaticity.getSelectedItem());
        }
        if (mSides != null) {
            PrintUtilities.setSides(set, (PageSides) mSides.getSelectedItem());
        }
        if (mNumberUp != null) {
            PrintUtilities.setNumberUp(set, UIUtilities.getTypedSelectedItemFromCombo(mNumberUp).getObject());
        }
        if (mPrintQuality != null) {
            PrintUtilities.setPrintQuality(set, (Quality) mPrintQuality.getSelectedItem());
        }
        if (mResolution != null) {
            PrintUtilities.setResolution(set, UIUtilities.getTypedSelectedItemFromCombo(mResolution).getObject());
        }
        return mService instanceof DummyPrintService ? null : mService;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == mServices) {
            HashPrintRequestAttributeSet set = new HashPrintRequestAttributeSet();
            rebuild(accept(set), set);
        }
    }
}
