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


public class StepperUtil {
    boolean stopped;

    public interface Range2D {
        Range getHRange();
        Range getVRange();
    }

    Range2D calculateBounds(Stepper stepper, int iterations) {

        Stepper.Point2D point;
        double xLower, xUpper;
        double yLower, yUpper;

        stopped = false;
        stepper.initialize();

        point = stepper.getCurrentPoint2D();
        xLower = point.getX();
        xUpper = point.getX();
        yLower = point.getY();
        yUpper = point.getY();

        for (int i = 0; i < iterations; i++) {
            if (stopped) {
                break;
            }
            
            stepper.step();
            point = stepper.getCurrentPoint2D();

            if (xLower > point.getX()) {
                xLower = point.getX();
            }
            else if (xUpper < point.getX()) {
                xUpper = point.getX();
            }

            if (yLower > point.getY()) {
                yLower = point.getY();
            }
            else if (yUpper < point.getY()) {
                yUpper = point.getY();
            }
        }

        return new MyRange2D(new Range(xLower, xUpper), new Range(yLower, yUpper));
    }

    void stop() {
        stopped = true;
    }

    private class MyRange2D implements Range2D {
        private Range horRange, verRange;
        
        MyRange2D(Range hRange, Range vRange) {
            horRange = hRange;
            verRange = vRange;
        }

        public Range getHRange() {
            return horRange;
        }

        public Range getVRange() {
            return verRange;
        }
    }
}
