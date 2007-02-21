/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 *
 *
 * The software program was developed within a research project financed
 * by the Italian Ministry of Universities, the Universities of Udine and
 * Ca'Foscari of Venice, the Friuli-Venezia Giulia Region.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.tsho.dmc2.core.dlua;

import org.jfree.data.Range;
import org.tsho.dmc2.core.MapStepper;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.ModelException;

class LuaIterator implements MapStepper {

    private LuaSimpleMap model;
    private double[] parameters;
    private double[] initialValues;
    
    private int dim;
    private long count;
    private double[] lastValues, currentValues;//, parValues;
    private int xIdx, yIdx;

    private boolean stopped;

    private MyPoint2D currentPoint2D;
    private MyPoint2D lastPoint2D;

    private MyPoint2D newCurrentPoint2D;
    
    public LuaIterator(LuaSimpleMap map, double[] parameters,
    		double[] initialValue) {
        this.model = map;
        this.currentPoint2D = new MyPoint2D();
        this.lastPoint2D = new MyPoint2D();
        this.newCurrentPoint2D = new MyPoint2D();
        this.dim = model.getNVar();
        this.parameters = new double[model.getNPar()];
        this.currentValues = new double[model.getNVar()];
        this.initialValues = new double[model.getNVar()];
        this.lastValues = new double[model.getNVar()];
        setParameters(parameters);
        setInitialValue(initialValue);
    }

    public void setParameters(double[] parameters) {
        System.arraycopy(
            parameters, 0, this.parameters, 0, this.parameters.length);
    }

    public void setInitialValue(double[] initialValue) {
        System.arraycopy(
            initialValue, 0, this.initialValues, 0, dim);
        System.arraycopy(
                initialValue, 0, this.currentValues, 0, dim);
    }
    
    public void initialize(){
        System.arraycopy(
            initialValues, 0, lastValues, 0, initialValues.length);

        count = 0;

        currentValues = new double[dim];        

        copyArrays(lastValues, currentValues);
        currentPoint2D.x = currentValues[xIdx];
        currentPoint2D.y = currentValues[yIdx];
        lastPoint2D.x = lastValues[xIdx];
        lastPoint2D.y = lastValues[yIdx];    
    }

    public void setAxes(int x, int y) {
        this.xIdx = x;
        this.yIdx = y;
    }


    public void step() throws ModelException {

        System.arraycopy(currentValues, 0, lastValues, 0, dim);
        model.evaluate(parameters, lastValues, currentValues);

        count++;

        currentPoint2D.x = currentValues[xIdx];
        currentPoint2D.y = currentValues[yIdx];
        lastPoint2D.x = lastValues[xIdx];
        lastPoint2D.y = lastValues[yIdx];
    }

    public void getCurrentValue(double[] value) {
        System.arraycopy(
            currentValues, 0, value, 0, this.currentValues.length);
    }

    public VariableDoubles getLastValue() {
        VariableDoubles v = (VariableDoubles) lastValues.clone();
        VariableDoubles.fill(v, lastValues);
        return v;
    }

    public void setXVariable(final double value) {
        currentValues[xIdx] = value;
    }

    // deprecated
    public Point2D getCurrent2DPoint() {
        return currentPoint2D;
    }

    // deprecated
    public Point2D getLast2DPoint() {
        return lastPoint2D;
    }

    public Point2D getCurrentPoint2D() {
        newCurrentPoint2D.x = currentValues[xIdx];
        newCurrentPoint2D.y = currentValues[yIdx];
        return newCurrentPoint2D;
    }

    public MapStepper.Range2D calculateBounds(int iterations) 
            throws ModelException {

        //double[] pars = VariableDoubles.toArray(parameters);
        double[] initial = new double[dim]; 
        double[] result = new double[dim];

        copyArrays(currentValues, initial);

        stopped = false;

        /* xmin, xmax, ymin, ymax */
        double[] ranges = new double[4];

        for (int i = 0; i < iterations; i++) {
            if (stopped == true) {
                break;
            }

            if (i == 0) {
                ranges[0] = initial[xIdx];
                ranges[1] = initial[xIdx];
                ranges[2] = initial[yIdx];
                ranges[3] = initial[yIdx];
            }

            if (result[xIdx] < ranges[0]) {
                ranges[0] = result[xIdx];
            }
            else if (result[xIdx] > ranges[1]) {
                ranges[1] = result[xIdx];
            }
            if (result[yIdx] < ranges[2]) {
                ranges[2] = result[yIdx];
            }
            else if (result[yIdx] > ranges[3]) {
                ranges[3] = result[yIdx];
            }
            
            model.evaluate(parameters, initial, result);

            copyArrays(result, initial);
        }

        return new MyRange2D(ranges);
    }

    public void stop() {
        stopped = true;
    }

    /**
     * @return
     */
    public long getCount() {
        return count;
    }

    private void copyArrays(double[] src, double[] dest) {
        System.arraycopy(src, 0, dest, 0, dim);
    }

    private static class MyPoint2D implements Point2D {
        private double x, y;

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    private class MyRange2D implements Range2D {
        private Range horRange, verRange;
        
        MyRange2D(double range[]) {
            horRange = new Range(range[0], range[1]);
            verRange = new Range(range[2], range[3]);
        }
        
        public Range getHRange() {
            return horRange;
        }

        public Range getVRange() {
            return verRange;
        }
    }
}
