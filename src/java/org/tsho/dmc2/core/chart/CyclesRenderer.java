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
package org.tsho.dmc2.core.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.AlgorithmFailedException;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.SimpleMap;


public class CyclesRenderer implements DmcPlotRenderer {

    private DmcRenderablePlot plot;
    private SimpleMap map;

    // parameters

    private VariableDoubles parameters;
    private VariableDoubles initialPoint;
    private String xLabel;
    private String yLabel;
    private double epsilon;
    private int period;
    private int maxPoints;

    // options

    private boolean bigDots;

    // flags

    private boolean stopped;

    // internal state

    private int state;


    public CyclesRenderer(final SimpleMap map, final DmcRenderablePlot plot) {

        this.map = map;
        this.plot = plot;
    }

    public void initialize() {
        stopped = false;
    }

    public LegendItemCollection getLegendItems() {
        return null;
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();

        state = STATE_RUNNING;

        g2.setColor(Color.BLACK);

        VariableDoubles initialValues = map.getVariables();
        int xLabelIndex = VariableDoubles.indexOf(initialValues, xLabel);
        int yLabelIndex = VariableDoubles.indexOf(initialValues, yLabel);

        int x, y;
        int dim = map.getNVar();
        double tmp[] = new double[dim];
        double[] cycle;
    	double[] cycleModulus = new double[dim];
        
        double[][] list = new double[period][dim];

        int counter = 0;
        main:
        while (counter < maxPoints) {

            if (stopped) {
                state = STATE_STOPPED;
                return;
            }

            /*
             *FIXME: ranges should be explicit algorithm inputs (even for systems with >2 variables!)
             */
            double xInit = domainAxis.getLowerBound()
                           - (domainAxis.getLowerBound() - domainAxis.getUpperBound())
                           * Math.random();
            double yInit = rangeAxis.getLowerBound()
                           - (rangeAxis.getLowerBound() - rangeAxis.getUpperBound())
                           * Math.random();
            initialValues.put(xLabel, xInit);
            initialValues.put(yLabel, yInit);

            counter++;

            try {
                cycle = Lua.findCycles(
                            map, parameters, period, initialValues,
					epsilon, 100, cycleModulus);
            }
            catch (AlgorithmFailedException e) {
                continue;
            }

            if (period > 1) {
                System.arraycopy(cycle, 0, list[0], 0, dim);                

                for (int i = 0; i < period - 1; i++) { /*fill cycle data*/
                    map.evaluate(
                            VariableDoubles.toArray(parameters),
                            list[i], list[i + 1]);
                }

                // compare i + 1 with all previous
                for (int j = 0; j < period; j++) {
                    for (int i = j + 1; i < period; i++) {
                        if (equal(list[j], list[i], epsilon)) {
                            continue main;
                        }
                    }
                }
                
                // check if it's a real solution
                map.evaluate(
                        VariableDoubles.toArray(parameters),
                        list[period-1], tmp);
                if (!equal(tmp, list[0], epsilon))
                            continue main;
                
                checkStability(cycleModulus, g2);
                
                for (int i = 0; i < period; i++) {
                    x = (int) domainAxis.valueToJava2D(
                            list[i][xLabelIndex], dataArea, RectangleEdge.BOTTOM);

                    y = (int) rangeAxis.valueToJava2D(
                            list[i][yLabelIndex], dataArea, RectangleEdge.LEFT);
                    if (bigDots) {
                        g2.fillRect(x - 1, y - 1, 3, 3);
                    }
                    else {
                        g2.fillRect(x, y, 1, 1);
                    }
                }
            }
            else { // period == 1
                x = (int) domainAxis.valueToJava2D(
                        cycle[xLabelIndex], dataArea, RectangleEdge.BOTTOM);

                y = (int) rangeAxis.valueToJava2D(
                        cycle[yLabelIndex], dataArea, RectangleEdge.LEFT);
                
                // check if it's a real solution
                map.evaluate(VariableDoubles.toArray(parameters),
                            cycle, tmp);
                if (!equal(tmp, cycle, epsilon))
                            continue main;
                
                checkStability(cycleModulus, g2);

                if (bigDots) {
                    g2.fillRect(x - 1, y - 1, 3, 3);
                }
                else {
                    g2.fillRect(x, y, 1, 1);
                }
            }
            
        }

        state = STATE_FINISHED;
    }
    
    private void checkStability(double cycleModulus[], Graphics2D g2) {
        /*check eigval modulus. Depending on it, set color.*/
        boolean stable=false;
        boolean unstable=false;
        int dim=cycleModulus.length;
        for(int i=0; i<dim; i++) {
                if(stable)
                        if(cycleModulus[i]>=1){
                                stable=false;
                                break;
                        } else continue;
                if(unstable)
                        if(cycleModulus[i]<=1){
                                unstable=false;
                                break;
                        } else continue;
                if(cycleModulus[i]<1)
                        stable=true;
                else if(cycleModulus[i]>1)
                        unstable=true;
                else break;
        }
        if(stable)
                g2.setColor(Color.GREEN);
        else if(unstable)
                g2.setColor(Color.RED);
        else
                g2.setColor(Color.BLUE);
    }


    static boolean _equal(double[] a, double[] b, double epsilon) {
        for (int k = 0; k < a.length; k++) {
            if (Math.abs(b[k] - a[k]) > epsilon) {
                return false;
            }
        }
        return true;
    }

    // euclidean
    static boolean equal(double[] a, double[] b, double epsilon) {
        double norm = 0;
        for (int k = 0; k < a.length; k++) {
            norm += Math.pow(b[k] - a[k], 2);
        }
        if (norm > epsilon) {
            return false;
        }
        else {
            return true;
        }
    }


    public void stop() {
        stopped = true;
    }

    public void initialize(
            VariableDoubles parameters, VariableDoubles initialPoint,
            String xLabel, String yLabel, double epsilon,
            int period, int maxPoints) {

        this.parameters = parameters;
        this.initialPoint = initialPoint;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.epsilon = epsilon;
        this.period = period;
        this.maxPoints = maxPoints;
    }
    
    public double getEpsilon() {
        return epsilon;
    }

    public VariableDoubles getInitialPoint() {
        return initialPoint;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public VariableDoubles getParameters() {
        return parameters;
    }

    public int getPeriod() {
        return period;
    }

    public int getState() {
        return state;
    }

    public String getXLabel() {
        return xLabel;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setEpsilon(double d) {
        epsilon = d;
    }

    public void setInitialPoint(VariableDoubles doubles) {
        initialPoint = doubles;
    }

    public void setMaxPoints(int i) {
        maxPoints = i;
    }

    public void setParameters(VariableDoubles doubles) {
        parameters = doubles;
    }

    public void setPeriod(int i) {
        period = i;
    }

    public void setState(byte b) {
        state = b;
    }

    public void setXLabel(String string) {
        xLabel = string;
    }

    public void setYLabel(String string) {
        yLabel = string;
    }

    public void setState(int i) {
        state = i;
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public void setBigDots(boolean b) {
        bigDots = b;
    }

}
