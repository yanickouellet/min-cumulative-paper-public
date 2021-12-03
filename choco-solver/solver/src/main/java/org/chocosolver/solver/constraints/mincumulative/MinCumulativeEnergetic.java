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
import java.util.Collections;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MinCumulativeEnergetic {
    private static class Interval {
        public int lowerBound;
        public int upperBound;
        public int extraEnergy;
        public int minimumIntersection;

        public Interval(int lowerBound, int upperBound, int extraEnergy, int minimumIntersection) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.extraEnergy = extraEnergy;
            this.minimumIntersection = minimumIntersection;
        }
    }

    private Instance instance;
    private int[] criticalTimepoints;
    private int[] extraEnergy;

    public MinCumulativeEnergetic(Instance instance) {
        this.instance = instance;
    }

    public boolean runChecker() {
        int horizon = instance.getHorizon();

        int extraEnergy = computeTotalExtraEnergyEfficiently();
        int requiredEnergy = computeDemandInInterval(0, horizon);
        int totalTasksEnergy = instance.computeTotalTaskEnergy();

        return totalTasksEnergy - extraEnergy >= requiredEnergy;
    }

    private int computeTotalExtraEnergy() {
        if (true)
            throw new IllegalStateException("Should not happen anymore.");
        int horizon = instance.getHorizon();
        int[] extraEnergy = new int[horizon+1];
        Arrays.fill(extraEnergy, 0);

        for (int upperBound = 1; upperBound <= horizon; upperBound++) {
            for (int lowerBound = 0; lowerBound < upperBound; lowerBound++) {
                extraEnergy[upperBound] = max(extraEnergy[upperBound],
                        extraEnergy[lowerBound] + computeExtraEnergyInInterval(lowerBound, upperBound));
            }
        }

        return extraEnergy[extraEnergy.length-1];
    }

    private int computeTotalExtraEnergyEfficiently() {
        criticalTimepoints = computeCriticalTimepoints();
        extraEnergy = new int[criticalTimepoints.length];
        Arrays.fill(extraEnergy, 0);

        for (int upperBoundIndex = 1; upperBoundIndex < criticalTimepoints.length; upperBoundIndex++) {
            for (int lowerBoundIndex = 0; lowerBoundIndex < upperBoundIndex; lowerBoundIndex++) {
                int lowerBound = criticalTimepoints[lowerBoundIndex];
                int upperBound = criticalTimepoints[upperBoundIndex];

                extraEnergy[upperBoundIndex] = max(extraEnergy[upperBoundIndex],
                        extraEnergy[lowerBoundIndex] + computeExtraEnergyInInterval(lowerBound, upperBound));
            }
        }

        return extraEnergy[extraEnergy.length-1];
    }

    private int[] computeCriticalTimepoints() {
        Task[] tasks = instance.getTasks();
        ArrayList<Integer>  timepoints = new ArrayList<>(5 * tasks.length);

        timepoints.add(instance.getHorizon());
        for (Task task : tasks) {
            timepoints.add(task.getEst());
            timepoints.add(task.getEct());
            timepoints.add(task.getLst());
            timepoints.add(task.getLct());
        }

        timepoints.addAll(instance.getCriticalDemandTimepoints());
//
//        timepoints.sort(Comparator.naturalOrder());
//
//        int distinctTimepoints = 0;
//        for (int i = 1; i < timepoints.size(); i++) {
//            if (!timepoints.get(i).equals(timepoints.get(i - 1))) {
//                distinctTimepoints++;
//            }
//        }
//
//        int currentIndex = 0;
//        int[] timepointsArray = new int[distinctTimepoints];
//
//        for (int timepoint : timepoints) {
//            if (currentIndex == distinctTimepoints) {
//                break;
//            }
//            if (currentIndex == 0 || timepoint != timepointsArray[currentIndex]) {
//                timepointsArray[currentIndex] = timepoint;
//                currentIndex++;
//            }
//        }

        return timepoints.stream().mapToInt(i -> i).sorted().distinct().toArray();
    }

    private int computeExtraEnergyInInterval(int lowerBound, int upperBound) {
        return max(0,
                computeMinimumEnergyInInterval(lowerBound, upperBound) -
                        computeDemandInInterval(lowerBound, upperBound));
    }

    private int computeMinimumEnergyInInterval(int lowerBound, int upperBound) {
        int energy = 0;
        for (Task task : instance.getTasks()) {
            energy += task.computeMinimumIntersection(lowerBound, upperBound);
        }
        return energy;
    }

    private int computeDemandInInterval(int lowerBound, int upperBound) {
        return instance.computeDemandInInterval(lowerBound, upperBound);
    }

    private List<Interval> computeLongestPath() {
        List<Interval> intervals = new ArrayList<>();

        int currentUpperBoundIndex = criticalTimepoints.length-1;
        while (currentUpperBoundIndex > 0) {
            int currentLowerBoundIndex = currentUpperBoundIndex-1;
            int currentLowerBound = criticalTimepoints[currentLowerBoundIndex];
            int currentUpperBound = criticalTimepoints[currentUpperBoundIndex];

            while (extraEnergy[currentUpperBoundIndex] !=
                    extraEnergy[currentLowerBoundIndex] + computeExtraEnergyInInterval(currentLowerBound, currentUpperBound)) {
                currentLowerBound = criticalTimepoints[--currentLowerBoundIndex];
            }

            int extraEnergyInInterval = extraEnergy[currentUpperBoundIndex] - extraEnergy[currentLowerBoundIndex];
            intervals.add(new Interval(currentLowerBound, currentUpperBound, extraEnergyInInterval,
                    computeMinimumEnergyInInterval(currentLowerBound, currentUpperBound)));
            currentUpperBoundIndex = currentLowerBoundIndex;
        }

        Collections.reverse(intervals);
        return intervals;
    }

    public int[] filter() {
        if (false) {
            throw new IllegalStateException("Should not happen right now.");
        }
        Task[] tasks = instance.getTasks();
        int[] filteredEst = instance.getEstArray();

        int extraEnergy = computeTotalExtraEnergyEfficiently();
        int requiredEnergy = computeDemandInInterval(0, instance.getHorizon());
        int totalTasksEnergy = instance.computeTotalTaskEnergy();

        int minimumEnergyToMatter = totalTasksEnergy - extraEnergy - requiredEnergy;
        assert(minimumEnergyToMatter > 0);

        for (Task task : tasks) {
            if (task.getFreeEnergy() < minimumEnergyToMatter) {
                continue;
            }
            int oldEst = task.getEst();
            int oldLct = task.getLct();

            task.setLct(task.getEct());
            while (task.getLct() <= oldLct && computeTotalExtraEnergyEfficiently() - extraEnergy > minimumEnergyToMatter) {
                task.setEst(task.getEst() + 1);
                task.setLct(task.getLct() + 1);
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

    public int[] filterWithPath() {
        Task[] tasks = instance.getTasks();
        int[] filteredEst = instance.getEstArray();

        int extraEnergy = computeTotalExtraEnergyEfficiently();
        int requiredEnergy = computeDemandInInterval(0, instance.getHorizon());
        int totalTasksEnergy = instance.computeTotalTaskEnergy();

        int minimumEnergyToMatter = totalTasksEnergy - extraEnergy - requiredEnergy + 1;
        assert(minimumEnergyToMatter > 0);

        for (Task task : tasks) {
            if (task.getFreeEnergy() < minimumEnergyToMatter) {
                continue;
            }
            int filteredValue = filterTaskWithPath(task, totalTasksEnergy, requiredEnergy);
            if (filteredValue > task.getLst()) {
                return new int[] {};
            }

            filteredEst[task.getId()] = filteredValue;
        }

        return filteredEst;
    }

    static int count = 0;
    private int filterTaskWithPath(Task task, int totalTaskEnergy, int requiredEnergy) {
        count++;
        int oldEst = task.getEst();
        int oldLct = task.getLct();
        task.setLct(task.getEct());

        int extraEnergy = computeTotalExtraEnergyEfficiently();
        List<Interval> intervals = computeLongestPath();

        int estIntervalIndex = 0;
        int lctIntervalIndex = 0;

        for (int intervalIndex = 0; intervalIndex < intervals.size(); intervalIndex++) {
            Interval interval = intervals.get(intervalIndex);
            if (isTimepointInInterval(task.getEst(), interval)) {
                estIntervalIndex = intervalIndex;
            }
            if (isTimepointInInterval(task.getLct()-1, interval)) {
                lctIntervalIndex = intervalIndex;
                break; // If we found the lctInterval, we necessarily found the estInterval.
            }
        }

        int delta = 0;
        while (totalTaskEnergy - extraEnergy - delta  < requiredEnergy && task.getLct() <= oldLct) {
            delta = computeDeltaInInterval(task, intervals.get(estIntervalIndex))
                    + computeDeltaInInterval(task, intervals.get(lctIntervalIndex));
            if (estIntervalIndex + 1 < intervals.size()) {
                delta += computeDeltaInInterval(task, intervals.get(estIntervalIndex+1));
            }
            if (lctIntervalIndex + 1 < intervals.size()) {
                delta += computeDeltaInInterval(task, intervals.get(lctIntervalIndex+1));
            }
            task.incrementEstAndLct();

            if (!isTimepointInInterval(task.getEst(), intervals.get(estIntervalIndex))) {
                estIntervalIndex++;
            }
            if (!isTimepointInInterval(task.getLct()-1, intervals.get(lctIntervalIndex))) {
                lctIntervalIndex++;
            }
        }

        int filteredValue = task.getEst();
        task.setEst(oldEst);
        task.setLct(oldLct);

        return filteredValue;
    }

    private boolean isTimepointInInterval(int timepoint, Interval interval) {
        return interval.lowerBound <= timepoint && timepoint < interval.upperBound;
    }

    private int computeDeltaInInterval(Task task, Interval interval) {
        int energyBeforeIncrement = task.computeMinimumIntersection(interval.lowerBound, interval.upperBound);
        task.incrementEstAndLct();
        int energyAfterIncrement = task.computeMinimumIntersection(interval.lowerBound, interval.upperBound);
        task.decrementEstAndLct();

        int energyInInterval = interval.minimumIntersection - energyBeforeIncrement + energyAfterIncrement;
        int newExtraEnergy = max(0, energyInInterval - computeDemandInInterval(interval.lowerBound, interval.upperBound));

        return newExtraEnergy - interval.extraEnergy;
    }
}
