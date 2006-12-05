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

import org.jfree.data.Range;
import org.tsho.dmc2.core.model.ModelException;


// TODO this contains the kludges and is deprecated
public interface MapStepper extends Stepper {

    interface Range2D {
        Range getHRange();
        Range getVRange();
    }

    /**
     * This is the kludge used by coweb
     * @param value
     */
    void setXVariable(double value);

    /**
     * Does not need start and does not modify the current and
     * last points. Loops from the current point
     * and reports the ranges obtained.
     * Currently you have no way to know if it has been stopped
     * by stop().
     *
     * @param iterations
     * @return (minX, maxX, minY, maxY)
     */
    Range2D calculateBounds(int iterations)
            throws ModelException;

    /**
     * Stops any calculus being done (applies to calculateBounds only)
     */
    void stop();

    /**
     * Return the projection on the requested axes of the result
     * of the last iteration. This is an handle: you only need to
     * get its reference once.
     */
    Point2D getCurrent2DPoint();
}
