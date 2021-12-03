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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DisjointSetsTest {
    private DisjointSets sets;

    @Test
    public void itWorks() {
        sets = new DisjointSets();
        sets.initialize(5);

        expectInitial();

        sets.merge(1, 2);
        assertEquals(2, sets.findMax(1));
        assertEquals(2, sets.findMax(2));
        assertEquals(0, sets.findMax(0));
        assertEquals(3, sets.findMax(3));

        sets.merge(0, 2);
        sets.merge(1, 3);
        sets.merge(0, 4);
    }

    private void expectInitial() {
        for (int i = 0; i < 5; i++) {
            assertEquals(i, sets.findMax(i));
        }
    }

    private void expectAllMerged() {
        for (int i = 0; i < 5; i++) {
            assertEquals(4, sets.findMax(i));
        }
    }
}