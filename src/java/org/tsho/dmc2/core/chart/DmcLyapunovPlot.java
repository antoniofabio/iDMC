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

/*
 * Derived from JFreeChart FastScatterPlot.java
 * by Daniele Pizzoni <auouo@tin.it>
 */
package org.tsho.dmc2.core.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;

public class DmcLyapunovPlot extends AbstractDmcPlot 
                            implements ValueAxisPlot {

    private Model model;
    private double lower;
    private double upper;
    private VariableDoubles parameters;
    private VariableDoubles initialPoint;
    private String firstParLabel;
    private String secondParLabel;
    private int iterations;
    private double epsilon;

    private boolean bigDots;
    private boolean connectWithLines;

    private final byte NONE = 0;
    private final byte VSTIME = 1;
    private final byte VSPAR = 2;
    private final byte AREA = 3;
    private byte type;

    private Paint[] paintSequence;

    private boolean stopped;

    private DmcLyapunovPlot() {
        super(null, null);
    }

    /**
     * @param domainAxis  the domain (x) axis.
     * @param rangeAxis  the range (y) axis.
     */
    public DmcLyapunovPlot(Model model, ValueAxis domainAxis, ValueAxis rangeAxis) {
        super(domainAxis, rangeAxis);
        this.model = model;
        this.parameters = model.getParameters();
        this.initialPoint = model.getVariables();
        this.type = NONE;
        
        paintSequence = new Paint[model.getNVar()];
        for (int i = 0; i < model.getNVar(); i++) {
            paintSequence[i] = getDrawingSupplier().getNextPaint();
        }
    }


    /**
     * Draws a representation of the data within the dataArea region.
     * <P>
     * The <code>info</code> and <code>crosshairInfo</code> arguments may be <code>null</code>.
     *
     * @param g2  the graphics device.
     * @param dataArea  the region in which the data is to be drawn.
     * @param info  an optional object for collection dimension information.
     * @param crosshairInfo  an optional object for collecting crosshair info.
     */
    public void render(Graphics2D g2, Rectangle2D dataArea,
                       PlotRenderingInfo info) {

        stopped = false;
        CoreStatusEvent statusEv = new CoreStatusEvent(this);
        statusEv.setStatusString("plotting...");
        statusEv.setType(CoreStatusEvent.STARTED | CoreStatusEvent.STRING);
        notifyCoreStatusListeners(statusEv);

        boolean status;
        
        
        if (type == VSTIME)
            status = renderVsTime(g2, dataArea);
        else if (type == VSPAR)
            status = renderVsParameter(g2, dataArea);
        else if (type == AREA)
            status = renderArea(g2, dataArea);
        else {
            throw new InternalError("Invalid plot type");
        }

        if (status == true) {
            statusEv.setPercent(100);
            statusEv.setStatusString("done");
            statusEv.setType(CoreStatusEvent.STRING
                           | CoreStatusEvent.PERCENT);
            notifyCoreStatusListeners(statusEv);
        }
        statusEv.setType(CoreStatusEvent.FINISHED);
        notifyCoreStatusListeners(statusEv);
    }

    public boolean renderVsTime(
            Graphics2D g2,
            Rectangle2D dataArea) {

        final int imageWidth = (int)dataArea.getWidth();

        g2.setPaint(Color.red);

        final int timeStep;
        if (Math.abs(upper - lower) >= imageWidth) {
            timeStep = ((int)upper - (int)lower) / imageWidth;
        }
        else {
            timeStep = 1;
        }

        CoreStatusEvent statusEv = new CoreStatusEvent(this);

        int prevX[] = new int[model.getNVar()];
        int prevY[] = new int[model.getNVar()];

        int ratio = (int)upper / 100;
        for (int i = (int)lower; i < (int)upper; i += timeStep) {
            double[] result;
            int transX, transY;

            result = Lua.evaluateLyapunovExponents(
                    model, parameters, initialPoint, i);

            for (int j = 0; j < result.length; j++) {
                transX = (int) this.domainAxis.valueToJava2D(
                                          i,
                                          dataArea,
                                          RectangleEdge.BOTTOM);
                transY = (int) this.rangeAxis.valueToJava2D(
                                          result[j],
                                          dataArea,
                                          RectangleEdge.LEFT);
//                        System.out.println("time: " + i);
//                        System.out.println("ly[" + j + "] = " + result[j]);
//                        System.out.println("x: " + transX + " y: " + transY);
                g2.setPaint(paintSequence[j]);
                //g2.fillRect(transX, transY, 1, 1);

                if (bigDots) {
                    g2.fillRect(transX - 1, transY - 1, 3, 3);

                }
                else {
                    g2.fillRect(transX, transY, 1, 1);
                }

                if (connectWithLines) {
                    if (i > (int)lower) {
                        g2.drawLine(transX, transY, prevX[j], prevY[j]);
                    }

                    prevX[j] = transX;
                    prevY[j] = transY;
                }
            }

            if (stopped == true) {
                return false;
            }
            else if (ratio > 0) {
                statusEv.setPercent(i / ratio);
                statusEv.setType(CoreStatusEvent.COUNT 
                             | CoreStatusEvent.PERCENT);
                notifyCoreStatusListeners(statusEv);
            }

        }

        return true;
    }

    public boolean renderVsParameter(
            Graphics2D g2,
            Rectangle2D dataArea) {

        final int imageWidth = (int)dataArea.getWidth();

        g2.setPaint(Color.red);

        final double parStep;
        parStep = Math.abs(upper - lower) / imageWidth;

        CoreStatusEvent statusEv = new CoreStatusEvent(this);

        //int ratio = upper / 100;

        int prevX[] = new int[model.getNVar()];
        int prevY[] = new int[model.getNVar()];

        for (double i = lower; i < upper; i += parStep) {
            double[] result;
            int transX, transY;
            
            parameters.put(firstParLabel, i);

            try {
                result = Lua.evaluateLyapunovExponents(model,
                                                       parameters,
                                                       initialPoint,
                                                       iterations);
            } catch (ModelException e) {
                String mess = "Exception while:\n"
                    +  dumpVariableDoubles(parameters)
                    +  dumpVariableDoubles(initialPoint);
                throw new ModelException(mess, e);
            }

            for (int j = 0; j < result.length; j++) {

                transX = (int) this.domainAxis.valueToJava2D(
                                          i,
                                          dataArea,
                                          RectangleEdge.BOTTOM);
       
                transY = (int) this.rangeAxis.valueToJava2D(
                                          result[j],
                                          dataArea,
                                          RectangleEdge.LEFT);


//                System.out.println("ly[" + j + "] = " + result[j]);
//                System.out.println("x: " + transX + " y: " + transY);

                g2.setPaint(paintSequence[j]);
                //g2.fillRect(transX, transY, 1, 1);
                if (bigDots) {
                    g2.fillRect(transX - 1, transY - 1, 3, 3);

                }
                else {
                    g2.fillRect(transX, transY, 1, 1);
                }

                if (connectWithLines) {
                    if (i != lower) {
                        g2.drawLine(transX, transY, prevX[j], prevY[j]);
                    }

                    prevX[j] = transX;
                    prevY[j] = transY;
                }
            }

          if (stopped == true) {
              return false;
          }
          //else if (ratio > 0 && i % ratio == 0) {
              statusEv.setPercent(0);
              statusEv.setType(CoreStatusEvent.COUNT 
                             | CoreStatusEvent.PERCENT);
              notifyCoreStatusListeners(statusEv);
          //}

        }

        return true;
    }
    

    public boolean renderArea(
            Graphics2D g2,
            Rectangle2D dataArea) {

        CoreStatusEvent statusEv = new CoreStatusEvent(this);
        g2.setPaint(paint);

        final double parHStep, parVStep;
        double parHLower = domainAxis.getRange().getLowerBound();
        double parHUpper = domainAxis.getRange().getUpperBound();
        double parVLower = rangeAxis.getRange().getLowerBound();
        double parVUpper = rangeAxis.getRange().getUpperBound();

        parHStep = Math.abs(parHUpper - parHLower) / dataArea.getWidth();
        parVStep = Math.abs(parVUpper - parVLower) / dataArea.getHeight();

        final BufferedImage image = new BufferedImage(
                                            (int)dataArea.getWidth(),
                                            (int)dataArea.getHeight(),
                                            BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        DataBufferInt dataBuffer = (DataBufferInt)raster.getDataBuffer();
        int[] data = dataBuffer.getData();

        final double parHStart = parHLower + parHStep / 2;
        final double  parVStart = parVUpper - parVStep / 2;

        for (int i = 0; i < (int)dataArea.getWidth(); i++) {
            for (int j = 0; j < (int)dataArea.getHeight(); j++) {

            parameters.put(firstParLabel, parHStart + i * parHStep);
            parameters.put(secondParLabel, parVStart - j * parVStep);

            double[] result;
            int color;

            try {
                result = Lua.evaluateLyapunovExponents(model,
                                                       parameters,
                                                       initialPoint,
                                                       iterations);
            } catch (ModelException e) {
                String mess = "Exception while:\n"
                    +  dumpVariableDoubles(parameters)
                    +  dumpVariableDoubles(initialPoint);
                throw new ModelException(mess, e);
            }

            if (result == null) {
                System.out.println("i: " + i + " j: " + j);
                System.out.println("par1: " + parHStart + i * parHStep);
                System.out.println("par2: " + parVStart + j * parVStep);
                g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                statusEv.setStatusString("exception");
                statusEv.setType(CoreStatusEvent.STRING);
                notifyCoreStatusListeners(statusEv);
                return false;
            }
            
            // both zero
            if (Math.abs(result[0]) < epsilon && Math.abs(result[1]) < epsilon) {
                color = Color.black.getRGB();
            }
            // one zero one positive
            else if (Math.abs(result[0]) < epsilon && result[1] > 0 ||
                     Math.abs(result[1]) < epsilon && result[0] > 0) {
                color = Color.red.getRGB();
            }
            // one zero one negative
            else if (Math.abs(result[0]) < epsilon && result[1] < 0 ||
                     Math.abs(result[1]) < epsilon && result[0] < 0) {
                color = Color.blue.getRGB();
            }
            // one positive one negative
            else if (result[0] < 0 && result[1] > 0 ||
                     result[1] < 0 && result[0] > 0) {
                color = Color.green.getRGB();
            }
            // both positive
            else if (result[0] > 0 && result[1] > 0) {
                color = Color.orange.getRGB();
            }
            // both negative
            else if (result[0] < 0 && result[1] < 0) {
                color = Color.pink.getRGB();
            }
            else { // impossible
                color = Color.yellow.getRGB();
            }
            
            data[i + j * (int)dataArea.getWidth()] = color;

            if (stopped == true) {
                return false;
            }
            if (j == (int)dataArea.getHeight() - 1) {
                g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                statusEv.setPercent(0);
                statusEv.setType(CoreStatusEvent.COUNT 
                                 | CoreStatusEvent.PERCENT);
                notifyCoreStatusListeners(statusEv);
            }
            }
        }

        return true;
    }

    private String dumpVariableDoubles(VariableDoubles v) {
        VariableDoubles.Iterator i = v.iterator();
        
        String string = "";
        while (i.hasNext()) {
            string = string + i.nextLabel() +  " :  " + i.value() + "\n";
        }
        
        return string;
    }


    public void stopRendering() {
        stopped = true;
    }

    public void initializeVsTime(
            VariableDoubles parameters,
            VariableDoubles initialValues,
            Range timeRange) {

        this.parameters.putAll(parameters);
        this.initialPoint.putAll(initialValues);
        this.lower = (int)timeRange.getLowerBound();
        this.upper = (int)timeRange.getUpperBound();

        this.type = VSTIME;
    }

    public void initializeVsParameter(
            VariableDoubles parameters,
            VariableDoubles initialValues,
            String parLabel,
            Range parameterRange,
            int iterations) {

        this.parameters.putAll(parameters);
        this.initialPoint.putAll(initialValues);
        this.firstParLabel = parLabel;
        this.lower = parameterRange.getLowerBound();
        this.upper = parameterRange.getUpperBound();
        this.iterations = iterations;

        this.type = VSPAR;
    }

    public void initializeParArea(
            VariableDoubles initialValues,
            VariableDoubles par, // fixed ones
            String firstParLabel,
            String secondParLabel,
            double epsilon,
            int iterations) {

        this.parameters.putAll(par);
        this.initialPoint.putAll(initialValues);
        this.firstParLabel = firstParLabel;
        this.secondParLabel = secondParLabel;
        this.epsilon = epsilon;
        this.iterations = iterations;

        domainAxis.setLabel(firstParLabel);
        rangeAxis.setLabel(secondParLabel);

        this.type = AREA;
    }
    
    public LegendItemCollection getLegendItems() {
        if (type != AREA)
            return null;

        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                                              BasicStroke.JOIN_BEVEL);
        Shape shape = new Rectangle2D.Double(-3, -3, 6, 6);
        
        LegendItemCollection legendItems = new LegendItemCollection();
        legendItems.add(new LegendItem("both zero",
                                       "",
                                       shape,
                                       true,
                                       Color.black,
                                        stroke,
                                       Color.yellow,
                                       stroke));

        legendItems.add(new LegendItem("zero, positive",
                                       "",
                                       shape,
                                       true,
                                       Color.red,
                                       stroke,
                                       Color.yellow,
                                       stroke));

        legendItems.add(new LegendItem("zero, negative",
                                       "",
                                       shape,
                                       true,
                                       Color.blue,
                                       stroke,
                                       Color.yellow,
                                       stroke));

        legendItems.add(new LegendItem("positive, negative",
                                       "",
                                       shape,
                                       true,
                                       Color.green,
                                       stroke,
                                       Color.yellow,
                                       stroke));

        legendItems.add(new LegendItem("both positive",
                                       "",
                                       shape,
                                       true,
                                       Color.orange,
                                       stroke,
                                       Color.yellow,
                                       stroke));

        legendItems.add(new LegendItem("both negative",
                                       "",
                                       shape,
                                       true,
                                       Color.pink,
                                       stroke,
                                       Color.yellow,
                                       stroke));


        return legendItems;
    }

    /* (non-Javadoc)
     * @see org.jfree.chart.plot.Plot#getPlotType()
     */
    public String getPlotType() {
        return "LyapunovPlot";
    }

    /**
     * @return
     */
    public boolean isBigDots() {
        return bigDots;
    }

    /**
     * @param b
     */
    public void setBigDots(boolean b) {
        bigDots = b;
    }

    /**
     * @return
     */
    public boolean isConnectWithLines() {
        return connectWithLines;
    }

    /**
     * @param b
     */
    public void setConnectWithLines(boolean b) {
        connectWithLines = b;
    }

}
