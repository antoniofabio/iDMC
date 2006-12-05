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
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;


public class Bifurcation2DRenderer implements DmcPlotRenderer {
    
    private DmcRenderablePlot plot;
    private Stepper stepper;
    
    // parameters
    
    private double[] fixedParameters;
    private double[] initialValue;
    private int firstParameterIdx;
    private int secondParameterIdx;
    private double epsilon;
    private double infinity;
    private int transients;
    private int period;
    
    private double time;//maximal time allowed so that ODE trajectory crosses Poincare section the needed number of times
    private double step;
    private double [] hyperplaneCoeff;
    private Model model;
    // flags
    
    private boolean stopped;
    // internal state
    
    private int state;
    
    public Bifurcation2DRenderer(
    final DmcRenderablePlot plot,
    final Stepper stepper,
    final Model model) {
        this.model=model;
        this.plot = plot;
        this.stepper = stepper;
    }
    
    public void initialize() {
        stopped = false;
    }
    
    public void stop() {
        stopped = true;
    }
    
    
    public int getState() {
        return state;
    }
    
    public void setState(int i) {
        state = i;
    }
    
    public void render(
    final Graphics2D g2, final Rectangle2D dataArea,
    final PlotRenderingInfo info) {
        
        int numVar=model.getNVar();
        
        if (model instanceof ODE){
            
            boolean pointBeyondPoincareSection;
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();
            
            int dim = initialValue.length;
            
            final int colorArrayLen =
            DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length;
            final Paint[] colorArray = new Color[colorArrayLen];
            for (int i = 0; i < colorArrayLen; i++) {
                colorArray[i] =
                DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
            }
            
            double[] fPars = new double[fixedParameters.length];
            System.arraycopy(fixedParameters, 0, fPars,0, fixedParameters.length);
            double[] initVars = new double[initialValue.length];
            System.arraycopy(initialValue, 0, initVars, 0, initialValue.length);
            
            double[] result = new double[dim];
            double[][] periodArray = new double[period][dim];
            
            for (double i = dataArea.getMinX(); i <= dataArea.getMaxX(); i += 1) {
                
                fPars[firstParameterIdx] = domainAxis.java2DToValue(
                i, dataArea, RectangleEdge.BOTTOM);
                
                for (double j = dataArea.getMinY();j < dataArea.getMaxY(); j += 1) {
                    
                    fPars[secondParameterIdx] = rangeAxis.java2DToValue(
                    j, dataArea, RectangleEdge.LEFT);
                    
                    stepper.setParameters(fPars);
                    stepper.setInitialValue(initVars);
                    stepper.initialize();
                    stepper.step();
                    stepper.getCurrentValue(result);
                    
                    int h=0;
                    int transX, transY;
                    
                    double [] currentPoint=new double[numVar];
                    double [] previousPoint=new double[numVar];
                    stepper.getCurrentValue(currentPoint);
                    stepper.getCurrentValue(previousPoint);
                    pointBeyondPoincareSection=positionWrtPoincareSection(currentPoint);
                    
                    
                    Paint color=Color.white;
                    double[] cycleStartPoint=new double[numVar];
                    int actualPeriod=period+1;
                    
                    for (int jj = 0; jj < time/step; jj++) {
                        stepper.step();
                        stepper.getCurrentValue(currentPoint);
                        
                        if (positionWrtPoincareSection(currentPoint) ==pointBeyondPoincareSection){
                            stepper.getCurrentValue(previousPoint);
                            continue;
                        }
                        
                        
                        pointBeyondPoincareSection=!pointBeyondPoincareSection;
                        double[] pointOnSection=pointOnPoincareSection(previousPoint,currentPoint);
                        stepper.setInitialValue(pointOnSection);
                        stepper.initialize();
                        stepper.getCurrentValue(currentPoint);
                        stepper.getCurrentValue(previousPoint);
                        
                        h++;
                        
                        if (h==transients){
                            for (int kk=0;kk<numVar;kk++)
                                cycleStartPoint[kk]=currentPoint[kk];
                        }
                        if (h>transients){
                            if (distance(currentPoint,cycleStartPoint)<epsilon){
                                actualPeriod=h-transients;
                                break;
                            }
                            if (h>=transients+period)
                                break;
                        }
                    }
                    
                    stepper.getCurrentValue(result);
                    
                    for (h = 0; h < dim; h++) {
                        if (Math.abs(result[h]) > infinity || Double.isNaN(result[h])) {
                            color = Color.black; // black == infinity
                        }
                    }
                    
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }
                    
                    
                    if (actualPeriod <= period) { // found period
                        color = colorArray[actualPeriod - 1];
                    }
                    
                    g2.setPaint(color);
                    g2.drawRect((int) i, (int) j, 1, 1);
                    
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }
                }
                
            }
            state = STATE_FINISHED;
            
        }
        else{
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();
            
            int dim = initialValue.length;
            
            final int colorArrayLen =
            DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length;
            final Paint[] colorArray = new Color[colorArrayLen];
            for (int i = 0; i < colorArrayLen; i++) {
                colorArray[i] =
                DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
            }
            
            double[] fPars = new double[fixedParameters.length];
            System.arraycopy(fixedParameters, 0, fPars,0, fixedParameters.length);
            double[] initVars = new double[initialValue.length];
            System.arraycopy(initialValue, 0, initVars, 0, initialValue.length);
            
            double[] result = new double[dim];
            double[][] periodArray = new double[period+1][dim];
            
            for (double i = dataArea.getMinX(); i <= dataArea.getMaxX(); i += 1) {
                
                fPars[firstParameterIdx] = domainAxis.java2DToValue(
                i, dataArea, RectangleEdge.BOTTOM);
                
                for (double j = dataArea.getMinY();
                j < dataArea.getMaxY(); j += 1) {
                    
                    fPars[secondParameterIdx] = rangeAxis.java2DToValue(
                    j, dataArea, RectangleEdge.LEFT);
                    
                    stepper.setParameters(fPars);
                    stepper.setInitialValue(initVars);
                    stepper.initialize();
                    stepper.step();
                    stepper.getCurrentValue(result);
                    
                    for (int h = 1; h < transients; h++) {
                        if (stopped) {
                            state = STATE_STOPPED;
                            return;
                        }
                        stepper.step();
                    }
                    
                    stepper.getCurrentValue(result);
                    
                    
                    Paint color = Color.white; // white == longer period
                    for (int h = 0; h < dim; h++) {
                        if (Math.abs(result[h]) > infinity || Double.isNaN(result[h])) {
                            color = Color.black; // black == infinity
                            break;
                        }
                    }
                    
                    if (color != Color.black) {
                        // get maxPeriod next values
                        for (int h = 0; h <= period; h++) {
                            if (stopped) {
                                state = STATE_STOPPED;
                                return;
                            }
                            
                            stepper.step();
                            stepper.getCurrentValue(result);
                            
                            for (int k = 0; k < dim; k++) {
                                periodArray[h][k] = result[k];
                            }
                        }
                        
                        int h, k = -1;
                        for (h = 1; h <= period; h++) {
                            for (k = 0; k < dim; k++) {
                                if (Math.abs(periodArray[0][k]
                                - periodArray[h][k]) >= epsilon) {
                                    break;
                                }
                            }
                            if (k == dim) {
                                break;
                            }
                        }
                        
                        if (h <= period) { // found period
                            color = colorArray[h - 1];
                        }
                    }
                    g2.setPaint(color);
                    g2.drawRect((int) i, (int) j, 1, 1);
                    
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }
                }
            }
            state = STATE_FINISHED;
        }
        
    }
    
    public void initialize(
    final double[] parameters, final double[] initialVal,
    final int firstParIdx, final int secondParIdx,
    final double epsilon, final double infinity,
    final int transients, final int period) {
        
        this.fixedParameters = new double[parameters.length];
        System.arraycopy(
        parameters, 0, fixedParameters, 0, fixedParameters.length);
        
        this.initialValue = new double[initialVal.length];
        System.arraycopy(
        initialVal, 0, this.initialValue, 0, initialVal.length);
        
        this.firstParameterIdx = firstParIdx;
        this.secondParameterIdx = secondParIdx;
        
        this.epsilon = epsilon;
        this.infinity = infinity;
        
        this.transients = transients;
        this.period = period;
    }
    
    public void initialize(
    final double[] parameters, final double[] initialVal,
    final int firstParIdx, final int secondParIdx,
    final double epsilon, final double infinity, final double time, final double step, final double[] hyperplaneCoeff,
    final int transients, final int period) {
        
        this.fixedParameters = new double[parameters.length];
        System.arraycopy(
        parameters, 0, fixedParameters, 0, fixedParameters.length);
        
        this.initialValue = new double[initialVal.length];
        System.arraycopy(
        initialVal, 0, this.initialValue, 0, initialVal.length);
        
        this.firstParameterIdx = firstParIdx;
        this.secondParameterIdx = secondParIdx;
        
        this.epsilon = epsilon;
        this.infinity = infinity;
        
        this.time=time;
        this.step=step;
        this.hyperplaneCoeff=hyperplaneCoeff;
        this.transients = transients;
        this.period = period;
    }
    
    public LegendItemCollection getLegendItems() {
        
        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL);
        Shape shape = new Rectangle2D.Double(-3, -3, 6, 6);
        
        LegendItemCollection legendItems = new LegendItemCollection();
        
        for (int i = 0; i < period; i++) {
            legendItems.add(new LegendItem(Integer.toString(i + 1),
            "",
            shape,
            true,
            (DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i]),
            stroke,
            Color.yellow,
            stroke));
        }
        
        legendItems.add(new LegendItem(">" + period,
        "",
        shape,
        true,
        Color.white,
        stroke,
        Color.yellow,
        stroke));
        
        legendItems.add(new LegendItem("infinity",
        "",
        shape,
        true,
        Color.black,
        stroke,
        Color.yellow,
        stroke));
        
        return legendItems;
    }
    
    public boolean positionWrtPoincareSection(double[] point){
        double res=0;
        for (int i=0;i<point.length;i++){
            res=res+point[i]*hyperplaneCoeff[i];
        }
        if (res>hyperplaneCoeff[point.length])
            return true;
        else
            return false;
    }
    
    public double[] pointOnPoincareSection(double[] x0, double[] x1){
        double ax0=0;
        double ax1=0;
        for (int i=0;i<x0.length;i++){
            ax0=ax0+hyperplaneCoeff[i]*x0[i];
            ax1=ax1+hyperplaneCoeff[i]*x1[i];
        }
        double [] result=new double[x0.length];
        double c0=(ax1-hyperplaneCoeff[x0.length])/(ax1-ax0);
        double c1=(hyperplaneCoeff[x0.length]-ax0)/(ax1-ax0);
        for (int i=0;i<x0.length;i++){
            result[i]=c0*x0[i]+c1*x1[i];
        }
        return result;
    }
    
    public double abs(double x){
        if (x<0)
            return -x;
        else
            return x;
    }
    
    public double distance(double[] x, double[] y){
        double d=0;
        for (int i=0;i<x.length;i++){
            double dd=abs(x[i]-y[i]);
            if (dd>d)
                d=dd;
        }
        return d;
    }
    
    
    
}
