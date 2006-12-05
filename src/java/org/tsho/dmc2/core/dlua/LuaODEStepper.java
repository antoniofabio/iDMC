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

import org.tsho.dmc2.core.ODEStepper;

class LuaODEStepper extends LuaStepIntegrator implements ODEStepper {

    private long count;

    MyPoint2D currentPoint2D;
    int xIdx, yIdx;

    double[] initialValue;

    double[] tempPoint;
    double[] currentPoint;

    public LuaODEStepper(LuaModel m, double[] parValues, 
    		double[] varValues, double step_size, 
    		int step_function_code) {
        super(m, parValues, varValues, step_size, step_function_code);
        count=0;
        tempPoint = new double[m.getVar_len()];
        currentPoint = new double[m.getVar_len()];
        currentPoint2D = new MyPoint2D();
    }

    public void setAxes(int xLabelIdx, int yLabelIdx) {
        xIdx = xLabelIdx;
        yIdx = yLabelIdx;
    }

    public void initialize() {
        count = 0;
    }

    public long getCount() {
        return count;
    }

    public void getCurrentValue(double[] value) {
        getPoint(value);
    }

    public Point2D getCurrentPoint2D() {
        getPoint(tempPoint);
        currentPoint2D.x = tempPoint[xIdx];
        currentPoint2D.y = tempPoint[yIdx];
        return currentPoint2D;
    }

    public void step() {
        super.step();
        count++;
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

}
