/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>.
 *
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
package org.tsho.dmc2.core;


public interface Stepper {

    interface Point2D {
        double getX();
        double getY();
    }

    void setParameters(double[] parameters);
    void setInitialValue(double[] initialValue);
    void setAxes(int xLabelIdx, int yLabelIdx);

    /**
     * Reinitializes the initial point to initialValues and
     * resets the iterations count to 0.
     * currentValue equals initialValue
     */
    void initialize();

    /**
     * Make an iteration.
     */
    void step();

    /**
     * Returns the number of iterations so far.
     */
    long getCount();

    /**
     * Fills value with the current value
     */
    void getCurrentValue(double[] value);

    /*
     *  The Point2D object returned remains valid as long
     *  as no other calls to this method are done.
     */
    Point2D getCurrentPoint2D();
}
