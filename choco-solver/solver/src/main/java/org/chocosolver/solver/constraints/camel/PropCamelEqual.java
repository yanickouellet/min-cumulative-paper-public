/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.camel;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class PropCamelEqual extends Propagator<IntVar> {
    private IntVar x, y;

    public PropCamelEqual(IntVar x, IntVar y) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, false);
        this.x = x;
        this.y = y;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (isEntailed() == ESat.FALSE) {
            throw new ContradictionException();
        }
    }

    @Override
    public ESat isEntailed() {
        if (!x.isInstantiated() || !x.isInstantiated()) {
            return ESat.UNDEFINED;
        }
        if (x.isInstantiatedTo(y.getValue())) {
            return ESat.TRUE;
        }
        return ESat.FALSE;
    }
}
