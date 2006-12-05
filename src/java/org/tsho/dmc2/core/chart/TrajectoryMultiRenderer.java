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
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.model.ModelException;

public class TrajectoryMultiRenderer extends TrajectoryRenderer {

    private DmcRenderablePlot plot;
    private Stepper[] stepperList;

    // parameters

    private int transients;
    private int iterations;
    private int rangeIterations;

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
    private int prevX[];
    private int prevY[];
    private Paint[] paintList;

    private int state;

    public TrajectoryMultiRenderer(
            final DmcRenderablePlot plot,
            final Stepper[] list) {

        super(plot, null, null);

        this.plot = plot;
        this.stepperList = list;
        computeRanges = true;
        
        prevX = new int[list.length];
        prevY = new int[list.length];
        
        paintList = new Paint[list.length];
        DrawingSupplier mySupplier = new DefaultDrawingSupplier();
        for (int i = 0; i < list.length; i++) {
            paintList[i] = mySupplier.getNextPaint();
        }
    }

    public void initialize() {
        stopped = false;
        if (!plot.isNoData() && computeRanges) {
            state = STATE_RANGES;
            computeRanges();
        }
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {

        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();

        /* transients */
        if (!continua) {
            state = STATE_TRANSIENTS;

            for (int i = 0; i < stepperList.length; i++) {
                stepperList[i].initialize();
                prevX[i] = 0;
                prevY[i] = 0;
            }
            
            for (index = 0; index < transients; index++) {
                for (int i = 0; i < stepperList.length; i++) {
                    stepperList[i].step();
                }
                if (stopped) {
                    state = STATE_STOPPED;
                    return;
                }
            }

//            for (int i = 0; i < stepperList.length; i++) {
//                stepperList[i].initialize();
//                for (index = 0; index < transients; index++) {
//                    stepperList[i].step();
//                    if (stopped) {
//                        state = STATE_STOPPED;
//                        return;
//                    }
//                }
//                prevX[i] = 0;
//                prevY[i] = 0;
//            }

            index = 0;
        }

        state = STATE_POINTS;

        Stepper.Point2D point;
        int x, y;
        int start = index;
        int end = 0;
        if (index == 0) {
            end = start + iterations + 1;
        }
        else {
            end = start + iterations;
        }

        Stroke stroke = new BasicStroke(7f);
        Stroke origStroke = g2.getStroke();
        Color color = Color.BLACK;

        for (; index < end; index++) {

            for (int i = 0; i < stepperList.length; i++) {
                point = stepperList[i].getCurrentPoint2D();
    
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

                g2.setPaint(paintList[i]);

   
                if (connectWithLines) {
                    if (index > 0) {
                        g2.drawLine(x, y, prevX[i], prevY[i]);
                    }
    
                    prevX[i] = x;
                    prevY[i] = y;
                }
    
                if (bigDots) {
                    g2.fillRect(x - 1, y - 1, 3, 3);
                }
                else {
                    g2.fillRect(x, y, 1, 1);
                }
    
                stepperList[i].step();
            }

            if (stopped) {
                state = STATE_STOPPED;
                return;
            }

            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                }
                catch (final InterruptedException e) {
                }
                finally {
                }
            }

        }
        state = STATE_FINISHED;
    }

    private void computeRanges() {

        Stepper.Point2D point;
        double xLower, xUpper;
        double yLower, yUpper;

        for (int i = 0; i < stepperList.length; i++) {
            stepperList[i].initialize();            
        }

        point = stepperList[0].getCurrentPoint2D();
        xLower = point.getX();
        xUpper = point.getX();
        yLower = point.getY();
        yUpper = point.getY();

        for (index = 0; index < rangeIterations; index++) {

            for (int i = 0; i < stepperList.length; i++) {

                point = stepperList[i].getCurrentPoint2D();

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

                stepperList[i].step();

                if (stopped) {
                    break;
                }
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

//    public Stepper getStepper() {
//        return stepper;
//    }

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

    public LegendItemCollection getLegendItems() {

        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                                              BasicStroke.JOIN_BEVEL);
        Shape shape = new Rectangle2D.Double(-3, -3, 6, 6);

        LegendItemCollection legendItems = new LegendItemCollection();

        for (int i = 0; i < stepperList.length; i++) {
            legendItems.add(new LegendItem(
                Integer.toString(i), "", shape, true, paintList[i], stroke,
                Color.yellow, stroke));
        }

        return legendItems;
    }

}
