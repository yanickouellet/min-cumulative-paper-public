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

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Task {
    private int id;
    private int est;
    private int lct;
    private int processingTime;
    private int height;

    public Task(int id) {
        this(0, 0, 0, 0, id);
    }

    public Task(int est, int lct, int p, int h, int id) {
        this.est = est;
        this.lct = lct;
        this.processingTime = p;
        this.height = h;
        this.id = id;
    }

    public int getEst() {
        return est;
    }

    public void setEst(int est) {
        this.est = est;
    }

    public int getLct() {
        return lct;
    }

    public void setLct(int lct) {
        this.lct = lct;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public int getLst() {
        return getLct() - getProcessingTime();
    }

    public int getEct() {
        return getEst() + getProcessingTime();
    }

    public int getEnergy() {
        return getProcessingTime() * getHeight();
    }

    public int getFreeEnergy() {
        return getEnergy() - getHeight() * max(0, getEct() - getLst());
    }

    public int computeLeftShift(int lowerBound, int upperBound) {
        return getHeight() * max(0, min(getEct(), upperBound) - max(getEst(), lowerBound));
    }

    public int computeRightShift(int lowerBound, int upperBound) {
        return getHeight() * max(0, min(upperBound, getLct()) - max(lowerBound, getLst()));
    }

    public int computeMinimumIntersection(int lowerBound, int upperBound) {
        return min(computeLeftShift(lowerBound, upperBound), computeRightShift(lowerBound, upperBound));
    }

    public boolean canExecuteAtTimepoint(int timepoint) {
        return getEst() <= timepoint && timepoint < getLct();
    }

    public void incrementEstAndLct() {
        est++;
        lct++;
    }

    public void decrementEstAndLct() {
        est--;
        lct--;
    }
}
