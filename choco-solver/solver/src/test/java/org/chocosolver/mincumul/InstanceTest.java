/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.mincumul;

import org.chocosolver.solver.constraints.mincumulative.Instance;
import org.chocosolver.solver.constraints.mincumulative.Task;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InstanceTest {
    @Test
    public void computeDemandInInterval_shouldWork() {
        //                                                   T: 0  1  2  3 4
        Instance instance = new Instance(new Task[0], new int[]{1, 2, 2, 3});

        assertEquals(8, instance.computeDemandInInterval(0, 4));
        assertEquals(7, instance.computeDemandInInterval(1, 4));
        assertEquals(4, instance.computeDemandInInterval(1, 3));
        assertEquals(2, instance.computeDemandInInterval(1, 2));
    }

    @Test
    public void computeCriticalDemandTimepoints_shouldWork() {
        //                                                      0     2     4     6
        Instance instance = new Instance(new Task[0], new int[]{1, 1, 0, 0, 1, 1, 2});

        assertArrayEquals(new int[]{0, 2, 4, 6}, instance.getCriticalDemandTimepoints().stream().mapToInt(i -> i).toArray());
    }
}
