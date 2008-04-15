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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import java.util.logging.Level;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.ui.trajectory.TrajectoryComponent;
import org.tsho.dmc2.DmcDue;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.core.util.*;


public class TrajectoryRenderer implements DmcPlotRenderer {

    private DmcRenderablePlot plot;
    private TrajectoryComponent plotComponent;
    private Stepper stepper;

    // parameters

    private int transients;
    private int iterations;
    private int rangeIterations;
    private double[] startPoint;

    // options

    private boolean timePlot;
    private boolean connectWithLines;
    private boolean bigDots;
    private long delay;

    // flags

    private boolean stopped;
    private boolean continua;
    private boolean computeRanges;

    // internal state

    private int index;
    private int prevX;
    private int prevY;

    private int state;
    
    private Dataset dataset;

    public TrajectoryRenderer(final DmcRenderablePlot plot,
            final Stepper stepper, TrajectoryComponent component) {

        this.plot = plot;
        this.stepper = stepper;
        computeRanges = true;
        this.dataset = (Dataset) component.getDataobject();
        this.plotComponent = component;
    }

    public void initialize() {
        stopped = false;
        if (!plot.isNoData() && computeRanges) {
            state = STATE_RANGES;
            computeRanges();
        }
    }

    public LegendItemCollection getLegendItems() {
        return null;
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();

        Stroke stroke = new BasicStroke(7f);
        Stroke origStroke = g2.getStroke();
        Color color = Color.BLACK;

        this.startPoint = new double[dataset.getNcol()];
        stepper.setInitialValue(startPoint);

        /* transients */
        if (!continua) {
            stepper.initialize();

            state = STATE_TRANSIENTS;
            
            try{
                for (index = 0; index < transients; index++) {
                    stepper.step();
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }
                }
            } catch(RuntimeException re) {
                //plotComponent.showRuntimeErrorDialog(re.getMessage());
                throw new RuntimeException(re);
            }

            index = 0;
            prevX = 0;
            prevY = 0;
        }

        state = STATE_POINTS;

        Stepper.Point2D point;
        double[] fullPoint = new double[dataset.getNcol()];
        dataset.clearRows();
        int x, y;
        int start = index;
        int end = 0;
        if (index == 0) {
            end = start + iterations + 1;
        }
        else {
            end = start + iterations;
        }

        for (; index < end; index++) {
            point = stepper.getCurrentPoint2D();
            stepper.getCurrentValue(fullPoint);
            try{
            	dataset.addRow((double[]) fullPoint.clone());
            } catch (DatasetException e) {
            	System.err.println(e);
            }

            if (!timePlot) {
                x = (int) domainAxis.valueToJava2D(
                            point.getX(), dataArea, RectangleEdge.BOTTOM);
            }
            else {
                x = (int) domainAxis.valueToJava2D(
                            index + transients, dataArea, RectangleEdge.BOTTOM);
            }

            y = (int) rangeAxis.valueToJava2D(
                        point.getY(), dataArea, RectangleEdge.LEFT);

            if (delay > 0) {
                boolean flag = false;

                try {

                    Thread.sleep(delay * 5);

                    drawItem(g2, index, x, y);

                    g2.setXORMode(color);
                    g2.setStroke(stroke);
                    g2.drawRect(x - 1, y - 1, 3, 3);
                        
                    flag = true;

                    Thread.sleep(delay * 5);
                }
                catch (final InterruptedException e) {
                }

                if (flag) {
                    g2.drawRect(x - 1, y - 1, 3, 3);
                    g2.setPaintMode();
                    g2.setStroke(origStroke);
                }
                else {
                    drawItem(g2, index, x, y);
                }
            }
            else {
                drawItem(g2, index, x, y);
            }

            try{
                stepper.step();
            } catch(RuntimeException re) {
                //plotComponent.showRuntimeErrorDialog(re.getMessage());
                throw new RuntimeException(re);
            }


            if (stopped) {
                state = STATE_STOPPED;
                return;
            }
        }

        state = STATE_FINISHED;
    }

    private void drawItem(
            Graphics2D g2, int item, int x, int y) {

        if (connectWithLines) {
            if (item > 0) {
                g2.drawLine(x, y, prevX, prevY);
            }
            prevX = x;
            prevY = y;
        }

        if (bigDots) {
            g2.fillRect(x - 1, y - 1, 3, 3);
        }
        else {
            g2.fillRect(x, y, 1, 1);
        }
    }

    private void computeRanges() {

        Stepper.Point2D point;
        double xLower, xUpper;
        double yLower, yUpper;

        stepper.initialize();

        point = stepper.getCurrentPoint2D();
        this.startPoint = new double[dataset.getNcol()];
        stepper.getCurrentValue(startPoint);
        xLower = point.getX();
        xUpper = point.getX();
        yLower = point.getY();
        yUpper = point.getY();

        for (index = 0; index < rangeIterations; index++) {
            if (stopped) {
                state = STATE_STOPPED;
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

        if (Double.isInfinite(xLower) || Double.isInfinite(xUpper)
            || Double.isInfinite(yLower) || Double.isInfinite(yUpper)
            || Double.isNaN(xLower) || Double.isNaN(xUpper)
            || Double.isNaN(yLower) || Double.isNaN(yUpper)) {

            throw new ModelException(
                "Exception during range calculations: (infinite or not-a-number value found)");
        }

        if (!timePlot) {
            plot.setDataRanges(
                    new Range(xLower, xUpper), new Range(yLower, yUpper));
        }
        else {
            plot.setDataRanges(
                    new Range(transients, transients + iterations),
                    new Range(yLower, yUpper));
        }

        computeRanges = false;
    }


    public void stop() {
        stopped = true;
    }

    public boolean isConnectWithLines() {
        return connectWithLines;
    }

    public boolean isContinua() {
        return continua;
    }

    public int getIterations() {
        return iterations;
    }

    public int getRangeIterations() {
        return rangeIterations;
    }

    public int getState() {
        return state;
    }

    public int getTransients() {
        return transients;
    }

    public void setConnectWithLines(boolean b) {
        connectWithLines = b;
    }

    public void setContinua(boolean b) {
        continua = b;
    }

    public void setIterations(int i) {
        iterations = i;
    }

    public void setRangeIterations(int i) {
        rangeIterations = i;
    }

    public void setState(int b) {
        state = b;
    }

    public void setTransients(int i) {
        transients = i;
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public void setBigDots(boolean b) {
        bigDots = b;
    }

    public boolean isComputeRanges() {
        return computeRanges;
    }

    public void setComputeRanges(boolean b) {
        computeRanges = b;
    }

    public Stepper getStepper() {
        return stepper;
    }

    public int getIndex() {
        return index;
    }
    public long getDelay() {
        return delay;
    }

    public void setDelay(long i) {
        delay = i;
    }

    public boolean isTimePlot() {
        return timePlot;
    }

    public void setTimePlot(boolean b) {
        timePlot = b;
    }

}
