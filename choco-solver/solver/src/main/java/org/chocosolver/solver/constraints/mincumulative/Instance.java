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
import java.util.Arrays;
import java.util.List;

public class Instance {
    private Task[] tasks;
    private int[] demand;
    private int[] partialSum;
    private List<Integer> criticalTimepoints;

    public Instance(Task[] tasks, int[] demand) {
        this.tasks = tasks;
        this.demand = demand;

        precomputeCriticalTimepoints();
        precomputePartialSum();
    }

    private void precomputePartialSum() {
        partialSum = new int[demand.length+1];

        partialSum[0] = 0;
        for (int i = 0; i < demand.length; i++) {
            partialSum[i+1] = partialSum[i] + demand[i];
        }
    }

    private void precomputeCriticalTimepoints() {
        criticalTimepoints = new ArrayList<>(5 * tasks.length);

        criticalTimepoints.add(0);
        int currentDemand = demand[0];

        for (int currentTimepoint = 1;  currentTimepoint < demand.length; currentTimepoint++) {
            if (currentDemand != demand[currentTimepoint]) {
                criticalTimepoints.add(currentTimepoint);
                currentDemand = demand[currentTimepoint];
            }
        }
    }

    public Task[] getTasks() {
        return tasks;
    }

    public int[] getDemand() {
        return demand;
    }

    public int getHorizon() {
        return demand.length;
    }

    public int computeDemandInInterval(int lowerBound, int upperBound) {
        return partialSum[upperBound] - partialSum[lowerBound];
    }

    public int computeTotalTaskEnergy() {
        return Arrays.stream(tasks).mapToInt(Task::getEnergy).sum();
    }

    public List<Integer> getCriticalDemandTimepoints() {
        return criticalTimepoints;
    }

    public int[] getEstArray() {
        int[] est = new int[tasks.length];
        for (Task task : tasks) {
            est[task.getId()] = task.getEst();
        }
        return est;
    }
}
