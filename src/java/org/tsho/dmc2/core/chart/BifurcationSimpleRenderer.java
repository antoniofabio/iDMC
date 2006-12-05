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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;

public class BifurcationSimpleRenderer implements DmcPlotRenderer {
    
    private DmcRenderablePlot plot;
    private Stepper stepper;
    private Model model;
    // parameters
    
    private double[] fixedParameters;
    private double[] initialValue;
    private int parameterIdx;
    private int variableIdx;
    private int iterations;
    private int transients;
    private double time;
    private double step;
    private double[] hyperplaneCoeff;
    
    
    // options
    
    private boolean fixedInitialPoint;
    
    // flags
    
    private boolean stopped;
    
    // internal state
    
    private int state;
    double index;
    double parLower;
    double parUpper;
    
    public BifurcationSimpleRenderer(
    final DmcRenderablePlot plot,
    final Stepper stepper,
    final Model model) {
        
        this.model=model;
        this.plot = plot;
        this.stepper = stepper;
    }
    
    public void initialize() {
        //throw new ModelException("cucu");
        stopped = false;
    }
    
    public LegendItemCollection getLegendItems() {
        return null;
    }
    
    public void stop() {
        stopped = true;
    }
    
    public void render(
    final Graphics2D g2, final Rectangle2D dataArea,
    PlotRenderingInfo info) {
        
        if (model instanceof ODE){
            boolean pointBeyondPoincareSection;
            int numVar=model.getNVar();
            
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();
            
            Stepper.Point2D result;
            
            g2.setPaint(plot.getPaint());
            
            parLower = domainAxis.getRange().getLowerBound();
            parUpper = domainAxis.getRange().getUpperBound();
            final double parStep;
            
            parStep = Math.abs(parUpper - parLower) / dataArea.getWidth();
            
            stepper.setAxes(variableIdx, variableIdx);
            
            state = STATE_RUNNING;
            
            for (index = parLower; index < parUpper; index += parStep) {
                
                fixedParameters[parameterIdx] = index;
                
                stepper.setParameters(fixedParameters);
                stepper.setInitialValue(initialValue);
                stepper.initialize();
                
                int h=0;
                int transX, transY;
                
                double [] currentPoint=new double[numVar];
                double [] previousPoint=new double[numVar];
                stepper.getCurrentValue(currentPoint);
                stepper.getCurrentValue(previousPoint);
                pointBeyondPoincareSection=positionWrtPoincareSection(currentPoint);
                
                
                for (int j = 0; j < time/step; j++) {
                    stepper.step();
                    stepper.getCurrentValue(currentPoint);
                    
                    if (positionWrtPoincareSection(currentPoint)==pointBeyondPoincareSection){
                        stepper.getCurrentValue(previousPoint);
                        continue;
                    }
                    
                    
                    pointBeyondPoincareSection=!pointBeyondPoincareSection;
                    double[] pointOnSection=pointOnPoincareSection(previousPoint,currentPoint);
                    stepper.setInitialValue(pointOnSection);
                    stepper.initialize();
                    stepper.getCurrentValue(currentPoint);
                    stepper.getCurrentValue(previousPoint);
                    
                    //pointBeyondPoincareSection=positionWrtPoincareSection(currentPoint,coeff);
                    result = stepper.getCurrentPoint2D();
                    
                    transX = (int) domainAxis.valueToJava2D(
                    index, dataArea, RectangleEdge.BOTTOM);
                    
                    transY = (int) rangeAxis.valueToJava2D(
                    result.getX(), dataArea, RectangleEdge.LEFT);
                    
                    h++;
                    if (h>transients){
                        g2.fillRect(transX, transY, 1, 1);
                    }
                    
                }
                
                if (!fixedInitialPoint) {
                    stepper.getCurrentValue(initialValue);
                }
                
                if (stopped) {
                    state = STATE_STOPPED;
                    return;
                }
            }
            
            state = STATE_FINISHED;
        }
        else{
            //the system is discrete
            ValueAxis domainAxis = plot.getDomainAxis();
            ValueAxis rangeAxis = plot.getRangeAxis();
            
            Stepper.Point2D result;
            
            g2.setPaint(plot.getPaint());
            
            parLower = domainAxis.getRange().getLowerBound();
            parUpper = domainAxis.getRange().getUpperBound();
            final double parStep;
            
            parStep = Math.abs(parUpper - parLower) / dataArea.getWidth();
            
            stepper.setAxes(variableIdx, variableIdx);
            
            state = STATE_RUNNING;
            
            for (index = parLower; index < parUpper; index += parStep) {
                
                fixedParameters[parameterIdx] = index;
                
                stepper.setParameters(fixedParameters);
                stepper.setInitialValue(initialValue);
                stepper.initialize();
                
                for (int h = 0; h < transients; h++) {
                    if (stopped) {
                        state = STATE_STOPPED;
                        return;
                    }
                    
                    stepper.step();
                }
                
                int transX, transY;
                for (int j = 0; j < iterations; j++) {
                    
                    stepper.step();
                    
                    result = stepper.getCurrentPoint2D();
                    
                    transX = (int) domainAxis.valueToJava2D(
                    index, dataArea, RectangleEdge.BOTTOM);
                    
                    transY = (int) rangeAxis.valueToJava2D(
                    result.getX(), dataArea, RectangleEdge.LEFT);
                    
                    g2.fillRect(transX, transY, 1, 1);
                }
                
                if (!fixedInitialPoint) {
                    stepper.getCurrentValue(initialValue);
                }
                
                if (stopped) {
                    state = STATE_STOPPED;
                    return;
                }
            }
            state = STATE_FINISHED;
        }
        
    }
    
    public void initialize(
    final double[] parameters,
    final double[] initialVal,
    final int parLabelIdx,
    final int varLabelIdx,
    final int iterations,
    final int transients,
    final boolean fixedInitial) {
        
        this.fixedParameters = new double[parameters.length];
        System.arraycopy(
        parameters, 0, fixedParameters, 0, fixedParameters.length);
        
        this.initialValue = new double[initialVal.length];
        System.arraycopy(
        initialVal, 0, this.initialValue, 0, initialVal.length);
        
        this.parameterIdx= parLabelIdx;
        this.variableIdx = varLabelIdx;
        
        this.iterations = iterations;
        this.transients = transients;
        this.fixedInitialPoint = fixedInitial;
    }
    
    public void initialize(
    final double[] parameters,
    final double[] initialVal,
    final int parLabelIdx,
    final int varLabelIdx,
    final double time,
    final double step,
    final double[] hyperplaneCoeff,
    final int transients,
    final boolean fixedInitial) {
        
        this.fixedParameters = new double[parameters.length];
        System.arraycopy(
        parameters, 0, fixedParameters, 0, fixedParameters.length);
        
        this.initialValue = new double[initialVal.length];
        System.arraycopy(
        initialVal, 0, this.initialValue, 0, initialVal.length);
        
        this.parameterIdx= parLabelIdx;
        this.variableIdx = varLabelIdx;
        
        this.time = time;
        this.step = step;
        this.hyperplaneCoeff=hyperplaneCoeff;
        
        this.transients=transients;
        this.fixedInitialPoint = fixedInitial;
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
    
    public int getState() {
        return state;
    }
    
    public void setState(int i) {
        state = i;
    }
    public double getIndex() {
        return index;
    }
    
    public double getParLower() {
        return parLower;
    }
    
    public double getParUpper() {
        return parUpper;
    }
    
}
