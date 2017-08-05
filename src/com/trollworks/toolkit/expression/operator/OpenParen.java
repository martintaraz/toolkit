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

package com.trollworks.toolkit.expression.operator;

import com.trollworks.toolkit.expression.EvaluationException;

public class OpenParen extends Operator {
    public OpenParen() {
        super("(", 0); //$NON-NLS-1$
    }

    @Override
    public final Object evaluate(Object left, Object right) throws EvaluationException {
        return null;
    }

    @Override
    public final Object evaluate(Object operand) throws EvaluationException {
        return null;
    }
}
