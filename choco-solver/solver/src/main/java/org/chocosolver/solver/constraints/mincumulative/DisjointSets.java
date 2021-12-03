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

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class DisjointSets {
    private List<Integer> sets;
    private List<Integer> maximums;

    public DisjointSets() {
        sets = new ArrayList<>();
        maximums = new ArrayList<>();
    }

    public void initialize(int n) {
        sets.clear();
        maximums.clear();
        for (int i = 0; i < n; i++) {
            sets.add(-1);
            maximums.add(i);
        }
    }

    public int findMax(int value) {
        return maximums.get(find(value));
    }

    private int find(int value) {
        int current = value;
        while (sets.get(current) > 0) {
            current = sets.get(current);
        }
        int parent = current;

        current = value;
        while (sets.get(current) > 0) {
            int temp = current;
            current = sets.get(current);
            sets.set(temp, parent);
        }

        return parent;
    }

    public void merge(int a, int b) {
        int parentA = find(a);
        int parentB = find(b);

        if (sets.get(parentA) < sets.get(parentB)) {
            sets.set(parentB, parentA);
            maximums.set(parentA, max(maximums.get(parentA), maximums.get(parentB)));
        } else if (sets.get(parentA) > sets.get(parentB)) {
            sets.set(parentA, parentB);
            maximums.set(parentB, max(maximums.get(parentA), maximums.get(parentB)));
        } else {
            sets.set(parentA, parentB);
            maximums.set(parentB, max(maximums.get(parentA), maximums.get(parentB)));
            sets.set(parentB, sets.get(parentB) - 1);
        }
    }
}
