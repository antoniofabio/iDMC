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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.model.ModelException;


public class CowebRenderer implements DmcPlotRenderer {

    private DmcRenderablePlot plot;
    private Stepper stepper;
    private ValueAxis domainAxis;
    private ValueAxis rangeAxis;

    // parameters

    private double[] initialValue;
    private int transients;
    private int power;

    // options

    private boolean connectWithLines;
    private boolean bigDots;
    private long delay;
    private boolean animate;

    // flags

    private boolean stopped;

    // internal state

    private int state;
    private Graphics2D g2blink;    


    public CowebRenderer(
            final DmcRenderablePlot plot,
            final Stepper stepper) {

        this.plot = plot;
        this.domainAxis = plot.getDomainAxis();
        this.rangeAxis = plot.getRangeAxis();
        this.stepper = stepper;

    }

    public void initialize() {
      //throw new ModelException();
        stopped = false;
    }

    public LegendItemCollection getLegendItems() {
        return null;
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {

        state = STATE_RUNNING;

        if (plot.isAlpha()) {
            g2.setComposite(AlphaComposite.SrcOver);

        }

        Stepper.Point2D result = stepper.getCurrentPoint2D();

        int transX, transY;

//        double start = Math.floor(dataArea.getMinX() + 0.5);
//        double end = Math.floor(dataArea.getMaxX() + 0.5);

        double start = (int) dataArea.getMinX();
        double end = (int) dataArea.getMaxX();

        double[] value = new double[1];

        int prevY = 0;

        label:
        for (double i = start; i <= end; i += 1) {
            value[0] = this.domainAxis.valueToJava2D(
                    i, dataArea, RectangleEdge.BOTTOM);

            stepper.setInitialValue(value);
            stepper.initialize();

            for (int j = 0; j < power; j++) {
                stepper.step();
            }

            result = stepper.getCurrentPoint2D();

            transX = (int) i;
            transY = (int) rangeAxis.valueToJava2D(
                                result.getX(), dataArea, RectangleEdge.LEFT);

//            transX = (int) Math.round(i);
//            transY = (int) Math.round(rangeAxis.translateValueToJava2D(
//                                result.getX(), dataArea, RectangleEdge.LEFT));


//            System.out.println(
//                "i = " + i + "; transX = " + transX + "; value = " + value[0]
//                + "; j = " + transY + "; y = " + result.getX());

            if (bigDots) {
                g2.fillRect(transX - 1, transY - 1, 3, 3);
            }
            else {
                g2.fillRect(transX, transY, 1, 1);
            }

            if (connectWithLines) {
                if (i > start) {
                    g2.drawLine(transX, transY, transX - 1, prevY);
                }

                prevY = transY;
            }

            if (stopped) {
                state = STATE_STOPPED;
                return;
            }
           
        }
        
        if (animate) {
            animateCowebPlot(g2, dataArea);
        }

        state = STATE_FINISHED;
    }


    private void animateCowebPlot(Graphics2D g2, Rectangle2D dataArea) {

        Graphics2D g2bisec = (Graphics2D) g2.create();
        g2bisec.setColor(Color.blue);

        Stroke stroke = new BasicStroke(3f);
        Stroke origStroke = g2.getStroke();
        Color color = Color.BLACK;
        g2blink = (Graphics2D) g2.create();
        g2blink.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);

        g2blink.setXORMode(color);
        g2blink.setStroke(stroke);

        if (plot.isAlpha()) {
            g2bisec.setComposite(AlphaComposite.SrcOver);
            g2.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                plot.getForegroundAlpha()));
        }

        /* transients */
        stepper.setInitialValue(initialValue);
        stepper.initialize();

        state = STATE_TRANSIENTS;

        for (int index = 0; index < transients; index++) {

            stepper.step();

            if (stopped) {
                state = STATE_STOPPED;
                return;
            }
        }

        state = STATE_RUNNING;

        Range xDataRange = plot.getDataRange(domainAxis);
        int transX, transY, transX1, transY1;
        
        transX = (int) this.domainAxis.valueToJava2D(
                xDataRange.getLowerBound(), dataArea, RectangleEdge.BOTTOM);
        transY = (int) this.rangeAxis.valueToJava2D(
                xDataRange.getLowerBound(), dataArea, RectangleEdge.LEFT);
        transX1 = (int) this.domainAxis.valueToJava2D(
                xDataRange.getUpperBound(), dataArea, RectangleEdge.BOTTOM);
        transY1 = (int) this.rangeAxis.valueToJava2D(
                xDataRange.getUpperBound(), dataArea, RectangleEdge.LEFT);

        g2bisec.drawLine(transX, transY, transX1, transY1);

        //renderer.reset();

        recurseCoweb(g2, dataArea);
    }

    private void recurseCoweb(final Graphics2D g2, final Rectangle2D dataArea) {

        Stepper.Point2D result = stepper.getCurrentPoint2D();
        double x1, x2;
        int transX, transY, transX1, transY1;

        for (;;) {

            if (stopped) {
                state = STATE_STOPPED;
                return;
            }

            x1 = result.getX();

            transX = (int) domainAxis.valueToJava2D(
                    x1, dataArea, RectangleEdge.BOTTOM);
            transY = (int) rangeAxis.valueToJava2D(
                    x1, dataArea, RectangleEdge.LEFT);

            for (int i = 0; i < power; i++) {
                stepper.step();
            }

            result = stepper.getCurrentPoint2D();

            x2 = result.getX();

            transY1 = (int) this.rangeAxis.valueToJava2D(
                    x2, dataArea, RectangleEdge.LEFT);

            drawTrace(g2, transX, transY, transX, transY1);

            transX1 = (int) domainAxis.valueToJava2D(
                x2, dataArea, RectangleEdge.BOTTOM);

            drawTrace(g2, transX, transY1, transX1, transY1);

            // fixed point found?
            if (Math.abs(x1 - x2) < 10e-6) {
                return;
            }
        }
    }

    private void drawTrace(Graphics2D g2, int x, int y, int x1, int y1) {

//        if (x == x1 && y == y1) {
//            return false;
//        }

        if (x == x1) { // vertical
            if (y <= y1) {
                for (int i = y; i <= y1; i++) {
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }

                    drawPoint(g2, x, i);
                }
            }
            else {
                for (int i = y; i >= y1; i--) {
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }

                    drawPoint(g2, x, i);
                }
            }
        }
        
        if (y == y1) { // horizontal
            if (x <= x1) {
                for (int i = x; i <= x1; i++) {
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }

                    drawPoint(g2, i, y);
                }
            }
            else {
                for (int i = x; i >= x1; i--) {
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }

                    drawPoint(g2, i, y);
                }
            }
        }
        
        return;
    }

    private void drawPoint(Graphics2D g2, int x, int y) {

        CoreStatusEvent statusEv = new CoreStatusEvent(this);
        statusEv.setType(CoreStatusEvent.REPAINT);

        if (delay >= 0) {

            try {
                g2.fillRect(x, y, 1, 1);

                g2blink.drawRect(x - 8, y - 8, 16, 16);

                Thread.sleep(delay / 3);
            }
            catch (final InterruptedException e) {
            }
            finally {
                g2blink.drawRect(x - 8, y - 8, 16, 16);
            }
        }
        else {
            g2.fillRect(x, y, 1, 1);
        }
    }


    public void stop() {
        stopped = true;
    }


    public int getState() {
        return state;
    }

    public void setState(int s) {
        state = s;
    }

    public int getPower() {
        return power;
    }

    public int getTransients() {
        return transients;
    }

    public void setPower(int i) {
        power = i;
    }

    public void setTransients(int i) {
        transients = i;
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public boolean isConnectWithLines() {
        return connectWithLines;
    }

    public long getDelay() {
        return delay;
    }

    public void setBigDots(boolean b) {
        bigDots = b;
    }

    public void setConnectWithLines(boolean b) {
        connectWithLines = b;
    }

    public void setDelay(long l) {
        delay = l;
    }

    public boolean isAnimate() {
        return animate;
    }

    public void setAnimate(boolean b) {
        animate = b;
    }

    public double[] getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(double[] ds) {
        initialValue = ds;
    }

}
