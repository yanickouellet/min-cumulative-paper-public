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
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;

public class MinCumulativeSweep {
    private static class Event implements Comparable<Event> {
        public int timepoint;
        public int heightAdjustment;

        public Event(int timepoint, int heightAdjustment) {
            this.timepoint = timepoint;
            this.heightAdjustment = heightAdjustment;
        }

        @Override
        public int compareTo(Event other) {
            return Integer.compare(this.timepoint, other.timepoint);
        }
    }

    private Instance instance;
    private Task[] tasks;
    private int[] demand;

    public MinCumulativeSweep(Instance instance) {
        this.instance = instance;
        this.tasks = instance.getTasks();
        this.demand = instance.getDemand();
    }

    int[] checkAndFilterWithBruteForce() {
        int[] filteredEst = instance.getEstArray();

        for (int timepoint = 0; timepoint < instance.getHorizon(); timepoint++) {
            int maxHeight = computeMaxHeightAtTimepoint(timepoint);
            int requiredHeight = instance.getDemand()[timepoint];
            if (maxHeight < requiredHeight) {
                return new int[0];
            }

            for (Task task : instance.getTasks()) {
                if (task.canExecuteAtTimepoint(timepoint) && maxHeight - task.getHeight() < requiredHeight) {
                    filteredEst[task.getId()] = max(filteredEst[task.getId()], timepoint + 1 - task.getProcessingTime());
                }
                if (filteredEst[task.getId()] > task.getLst()) {
                    return new int[0];
                }
            }
        }

        return filteredEst;
    }

    int computeMaxHeightAtTimepoint(int timepoint) {
        int maxHeight = 0;
        for (Task task : instance.getTasks()) {
            if (task.getEst() <= timepoint && timepoint < task.getLct()) {
                maxHeight += task.getHeight();
            }
        }
        return maxHeight;
    }

    int[] checkAndFilteringEfficient() {
        int[] filteredEst = instance.getEstArray();

        int currentMaxHeight = 0;
        List<Event> events = generateAndSortEvents();

        for (int currentEventIndex = 0; events.get(currentEventIndex).timepoint < instance.getHorizon(); currentEventIndex++) {
            // The loop will never reach the last event, which has a timepoint equal to the horizon.
            // This simplifies a lot of bound check in the body of the loop

            Event currentEvent = events.get(currentEventIndex);
            int timepoint = currentEvent.timepoint;

            currentMaxHeight += currentEvent.heightAdjustment;

            if (timepoint != events.get(currentEventIndex+1).timepoint) {
                if (currentMaxHeight < demand[timepoint]) {
                    return new int[0];
                }

                for (Task task : instance.getTasks()) {
                    if (task.canExecuteAtTimepoint(timepoint)
                            && currentMaxHeight - task.getHeight() < demand[timepoint]) {
                        int nextTimepoint = events.get(currentEventIndex+1).timepoint;
                        filteredEst[task.getId()] = max(filteredEst[task.getId()], nextTimepoint - task.getProcessingTime());
                    }
                }
            }
        }

        return filteredEst;
    }

    private List<Event> generateAndSortEvents() {
        List<Event> events = new ArrayList<>(4 * instance.getTasks().length);

        addDemandEventsToList(events);
        addTasksEventsToList(events);

        events.sort(Comparator.naturalOrder());

        return events;
    }

    private void addDemandEventsToList(List<Event> events) {
        for (int timepoint : instance.getCriticalDemandTimepoints()) {
            // I only need the events to trigger the check
            events.add(new Event(timepoint, 0));
        }
        events.add(new Event(instance.getHorizon(), 0));
    }

    private void addTasksEventsToList(List<Event> events) {
        for (Task task : tasks) {
            events.add(new Event(task.getEst(), task.getHeight()));
            events.add(new Event(task.getLct(), -task.getHeight()));
        }
    }
}
