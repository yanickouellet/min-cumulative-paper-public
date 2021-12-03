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
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

public class MinCumulativeUnderload {
    private Instance instance;
    private int[] demand;
    private Task[] tasks;
    private List<Integer> criticalTimepoints;
    private List<Integer> tempCriticalTimepoints;
    private List<Integer> remainingEnergyAtTimepoint;
    private List<Integer> overflowAtTimepoint;

    private List<Integer> tasksToEstIndex;
    private DisjointSets disjointSets;


    public MinCumulativeUnderload(Instance instance) {
        this.instance = instance;
        this.tasks = instance.getTasks();
        this.demand = instance.getDemand();
        this.criticalTimepoints = new ArrayList<>(tasks.length * 3);
        this.tempCriticalTimepoints = new ArrayList<>(tasks.length * 3);
        this.remainingEnergyAtTimepoint = new ArrayList<>(tasks.length * 3);
        this.overflowAtTimepoint = new ArrayList<>(tasks.length * 3);

        this.disjointSets = new DisjointSets();
        this.tasksToEstIndex = new ArrayList<>(tasks.length);
        for (int i = 0; i < tasks.length; i++) {
            tasksToEstIndex.add(-1);
        }
    }

    public boolean runCheckEfficiently() {
        initializeTimepointsAndEnergy();

        return runCheckEfficientlyWithoutInit();
    }

    private void initializeTimepointsAndEnergy() {
        initializeTimepoints();
        initializeTaskIndexes();
        initializeEnergy();
    }

    private void initializeTimepoints() {
        tempCriticalTimepoints.clear();
        tempCriticalTimepoints.addAll(instance.getCriticalDemandTimepoints());
        for (Task task : tasks) {
            tempCriticalTimepoints.add(task.getEst());
            tempCriticalTimepoints.add(task.getLct());
        }

        tempCriticalTimepoints.sort(Comparator.naturalOrder());

        criticalTimepoints.clear();
        for (int timepoint : tempCriticalTimepoints) {
            if (criticalTimepoints.isEmpty() || timepoint != criticalTimepoints.get(criticalTimepoints.size()-1)) {
                criticalTimepoints.add(timepoint);
            }
        }
    }

    private void initializeTaskIndexes() {
        Task[] tasksByEst = sortTasksByEst();

        int taskIndex = 0;
        int timepointIndex = 0;

        while (taskIndex < tasks.length) {
            Task task = tasksByEst[taskIndex];
            if (task.getEst() == criticalTimepoints.get(timepointIndex)) {
                tasksToEstIndex.set(task.getId(), timepointIndex);
                taskIndex++;
            } else {
                timepointIndex++;
            }
        }
    }

    private void initializeEnergy() {
        remainingEnergyAtTimepoint.clear();
        for (int i = 0; i < criticalTimepoints.size()-1; i++) {
            int demandAtTimepoint = instance.computeDemandInInterval(criticalTimepoints.get(i), criticalTimepoints.get(i+1));
            remainingEnergyAtTimepoint.add(demandAtTimepoint);
            overflowAtTimepoint.add(0);
        }
    }

    private boolean runCheckEfficientlyWithoutInit() {
        if (true) {
            throw new IllegalStateException("Should not be called in this experiment.");
        }
        int totalDemand = instance.computeDemandInInterval(0, instance.getHorizon());

        disjointSets.initialize(criticalTimepoints.size());

        for (Task task : sortTasksByLct()) {
            int energy = task.getEnergy();

            int currentTimepointIndex = tasksToEstIndex.get(task.getId());
            currentTimepointIndex = disjointSets.findMax(currentTimepointIndex);

            while (energy > 0 && currentTimepointIndex < criticalTimepoints.size() &&
                    criticalTimepoints.get(currentTimepointIndex) < task.getLct()) {

                int delta = min(energy, remainingEnergyAtTimepoint.get(currentTimepointIndex));
                remainingEnergyAtTimepoint.set(currentTimepointIndex, remainingEnergyAtTimepoint.get(currentTimepointIndex) - delta);
                energy -= delta;
                totalDemand -= delta;

                if (remainingEnergyAtTimepoint.get(currentTimepointIndex) == 0) {
                    disjointSets.merge(currentTimepointIndex, currentTimepointIndex+1);
                }
                currentTimepointIndex = disjointSets.findMax(currentTimepointIndex);
            }
        }

        return totalDemand == 0;
    }

    public boolean runCheckBruteForce() {
        initializeEnergyAndOverflowForBruteCheck();

        int totalDemand = instance.computeDemandInInterval(0, instance.getHorizon());

        for (Task task : sortTasksByLct()) {
            int energy = task.getEnergy();
            int currentTimepoint = task.getEst();

            while (energy > 0 && currentTimepoint < task.getLct()) {
                int delta = min(energy, remainingEnergyAtTimepoint.get(currentTimepoint));
                remainingEnergyAtTimepoint.set(currentTimepoint,
                        remainingEnergyAtTimepoint.get(currentTimepoint) - delta);
                energy -= delta;
                totalDemand -= delta;

                currentTimepoint++;
            }

            if (energy > 0) {
                overflowAtTimepoint.set(task.getEst(), overflowAtTimepoint.get(task.getEst()) + energy);
            }
        }

        return totalDemand == 0;
    }

    private void initializeEnergyAndOverflowForBruteCheck() {
        remainingEnergyAtTimepoint.clear();
        overflowAtTimepoint.clear();

        for (int i = 0; i < instance.getDemand().length; i++) {
            remainingEnergyAtTimepoint.add(instance.getDemand()[i]);
            overflowAtTimepoint.add(0);
        }
    }

    private Task[] sortTasksByLct() {
        Task[] tasks = instance.getTasks().clone();
        Arrays.sort(tasks, Comparator.comparingInt(Task::getLct));
        return tasks;
    }

    private Task[] sortTasksByEst() {
        Task[] tasks = instance.getTasks().clone();
        Arrays.sort(tasks, Comparator.comparingInt(Task::getEst));
        return tasks;
    }

    static int calls = 0;
    public int[] filter() {
        int[] filteredEst = instance.getEstArray();

        for (Task task : instance.getTasks()) {
            calls++;
            int oldEst = task.getEst();
            int oldLct = task.getLct();

            task.setLct(task.getEct());
            while (task.getLct() <= oldLct && !runCheckBruteForce()) {
//                task.setEst(task.getEst() + 1);
//                task.setLct(task.getLct() + 1);
                int overflowTimepoint = max(task.getEst(), findFirstTimepointWithOverflowAfterOrAt(oldEst));
                int missingDemand = computedMissingDemand();
                int increment = (int) ceil(missingDemand * 1f / task.getHeight());
                task.setEst(overflowTimepoint + increment);
                task.setLct(task.getEst() + task.getProcessingTime());
            }

            if (task.getLct() > oldLct) {
                return new int[0];
            }
            filteredEst[task.getId()] = task.getEst();

            task.setEst(oldEst);
            task.setLct(oldLct);
        }

        return filteredEst;
    }

    public int findFirstTimepointWithOverflowAfterOrAt(int afterTimepoint) {
        for (int i = afterTimepoint; i < instance.getHorizon(); i++) {
            if (overflowAtTimepoint.get(i) > 0) {
                return i;
            }
        }
        return afterTimepoint;
        //throw new IllegalStateException("This should not happen. There must be overflow after est_i.");
    }

    public int computedMissingDemand() {
        int missingDemand = 0;
        for (int i = 0; i < remainingEnergyAtTimepoint.size(); i++) {
            missingDemand += remainingEnergyAtTimepoint.get(i);
        }

        return missingDemand;
    }
}
