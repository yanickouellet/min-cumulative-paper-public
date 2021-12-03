/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.mincumulative;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

public class MinCumulative extends Constraint {
    public MinCumulative(IntVar[] starts, IntVar[] processingTimes, IntVar[] heights, int[] demand) {
        super("MinCumulative",
                new PropMinCumulative(starts, processingTimes, heights, demand)
        );
    }
}
