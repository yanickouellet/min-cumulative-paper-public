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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PropMinCumulative extends Propagator<IntVar> {
    private IntVar[] starts;
    private IntVar[] processingTimes;
    private IntVar[] heights;
    private int[] demand;
    private Task[] tasks;
    private Instance instance;

    private MinCumulativeEnergetic energeticAlgorithm;
    private MinCumulativeSweep sweepAlgorithm;
    private MinCumulativeUnderload underloadAlgorithm;

    public PropMinCumulative(IntVar[] starts, IntVar[] processingTimes, IntVar[] heights, int[] demand) {
        super(ArrayUtils.append(starts, processingTimes, heights), PropagatorPriority.QUADRATIC, false);
        this.starts = starts;
        this.processingTimes = processingTimes;
        this.heights = heights;
        this.demand = demand;

        tasks = IntStream.range(0, starts.length)
                .mapToObj(Task::new)
                .toArray(Task[]::new);

        instance = new Instance(tasks, demand);
        energeticAlgorithm = new MinCumulativeEnergetic(instance);
        sweepAlgorithm = new MinCumulativeSweep(instance);
        underloadAlgorithm = new MinCumulativeUnderload(instance);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateTasks();

        //propagateWithEnergetic();
        propagateWithUnderload();
        propagateWithSweep();
    }

    private void propagateWithUnderload() throws ContradictionException {
        if (!underloadAlgorithm.runCheckBruteForce()) {
            throw new ContradictionException();
        }

//        int[] filteredEst = underloadAlgorithm.filter();
//        if (filteredEst.length == 0) {
//            throw new ContradictionException();
//        }
//
//        for (int i = 0; i < tasks.length; i++) {
//            starts[i].updateLowerBound(filteredEst[i], this);
//        }
    }

    private void propagateWithSweep() throws ContradictionException {
        int[] filteredEst = sweepAlgorithm.checkAndFilterWithBruteForce();

        if (filteredEst.length == 0) {
            throw new ContradictionException();
        }

        for (int i = 0; i < tasks.length; i++) {
            starts[i].updateLowerBound(filteredEst[i], this);
        }
    }

    private void propagateWithSweepCheck() throws ContradictionException {
        int[] filteredEst = sweepAlgorithm.checkAndFilterWithBruteForce();

        if (filteredEst.length == 0) {
            throw new ContradictionException();
        }
    }

    private void propagateWithEnergetic() throws ContradictionException {
        if (!energeticAlgorithm.runChecker()) {
            throw new ContradictionException();
        }

        int[] filteredEst = energeticAlgorithm.filter();
        if (filteredEst.length == 0) {
            throw new ContradictionException();
        }

        for (int i = 0; i < tasks.length; i++) {
            starts[i].updateLowerBound(filteredEst[i], this);
        }
    }

    private void updateTasks() {
        for (int i = 0; i < tasks.length; i++) {
            tasks[i].setEst(starts[i].getLB());
            tasks[i].setLct(min(demand.length, starts[i].getUB() + processingTimes[i].getUB()));
            tasks[i].setProcessingTime(processingTimes[i].getUB());
            tasks[i].setHeight(heights[i].getUB());
        }
    }

    private boolean isUnsat() {
        return sweepAlgorithm.checkAndFilterWithBruteForce().length == 0;
    }

    @Override
    public ESat isEntailed() {
        if (isUnsat())  {
            return ESat.FALSE;
        }

        for (int i = 0; i < starts.length; i++) {
            if (!starts[i].isInstantiated() || !processingTimes[i].isInstantiated() || !heights[i].isInstantiated()) {
                return ESat.UNDEFINED;
            }
        }

        return ESat.TRUE;
    }
}
