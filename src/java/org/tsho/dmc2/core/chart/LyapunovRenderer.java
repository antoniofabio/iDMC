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


import java.util.HashSet;
import java.util.Iterator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.ImageObserver;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ValueAxis;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.dlua.LuaLyapExp;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.ui.lyapunov.*;
//?14.7.2004
import org.tsho.dmc2.core.util.*;
import org.tsho.dmc2.core.util.LyapunovColors;
//?

public class LyapunovRenderer implements DmcPlotRenderer, ImageObserver {
    
    private DmcRenderablePlot plot;
    private LyapunovComponent lyapunovComponent;
    
    // parameters
    private Model model;
    private double lower;
    private double upper;
    private double epsilon;
    
    private VariableDoubles parameters;
    private VariableDoubles initialPoint;
    private String firstParLabel;
    private String secondParLabel;
    private int iterations;
    
    private double stepSize;
    private double timePeriod;
    
    // options
    
    private boolean connectWithLines;
    private boolean bigDots;
    private LyapunovColors lyapunovColors;
    
    // flags
    
    private boolean stopped;
    //?
    private int pass;//needed for redrawing chart after legend changed in the type=TYPE_AREA case
    private HashSet signsSet;
    private BufferedImage image;
    
    //?
    
    // internal state
    
    private int state;
    private Paint[] paintSequence;
    
    private final byte TYPE_VSTIME = 1;
    private final byte TYPE_VSPAR = 2;
    private final byte TYPE_AREA = 3;
    private byte type;
    
    public LyapunovRenderer(
    final DmcRenderablePlot plot,
    final Model model,
    LyapunovComponent lyapunovComponent) {
    	        
        this.plot = plot;
        this.model = model;
        this.lyapunovComponent = lyapunovComponent;
        //lyapunovComponent.setDataobject(DataObject dataobject)
        this.pass=0;
        
        this.parameters = model.getParameters();
        this.initialPoint = model.getVariables();
        
        paintSequence = new Paint[model.getNVar()];
        for (int i = 0; i < model.getNVar(); i++) {
            paintSequence[i] = plot.getDrawingSupplier().getNextPaint();
        }
        lyapunovColors=new LyapunovColors(model.getNVar());
        
        signsSet=new HashSet();
    }
    
    public void initialize() {
        stopped = false;
    }
    
    public void render(
    Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) {
        
        boolean status;
        
        state = STATE_RUNNING;
        
        if (type == TYPE_VSTIME)
            status = renderVsTime(g2, dataArea);
        else if (type == TYPE_VSPAR)
            status = renderVsParameter(g2, dataArea);
        else if (type == TYPE_AREA)
            status = renderArea(g2, dataArea);
        else {
            throw new InternalError("Invalid plot type");
        }
        
        state = STATE_FINISHED;
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
        
        int prevX[] = new int[model.getNVar()];
        int prevY[] = new int[model.getNVar()];
        
        //? 14.7.2004
        //LyapunovExpCalc lyapunovExpCalc;
        LuaLyapExp integrator=new LuaLyapExp(model,parameters,initialPoint);
    	String colNames[] = new String[model.getNVar()+1];
    	colNames[0] = "time";
    	for(int j=1; j<model.getNVar()+1; j++)
    		colNames[j] = "" + (new Integer(j));
    	Dataset dataset = new Dataset(colNames);
    	lyapunovComponent.setDataobject(dataset);
    	double tmpRow[];
        if (model instanceof ODE){
            double step=stepSize;  // stepSize and timeStep probably mean the same thing, one for discrete another for ODE
            //checking trajectory
            
            for (int i = 0; i < (int)(upper/stepSize); i ++) {
                double[] result=new double[model.getNVar()];
                int transX, transY;
                
                double[] temp=integrator.evaluateODEStep(step);
                double tt= integrator.getTime();
                for (int h=0;h<temp.length;h++){
                    result[h]=temp[h]/tt;
                }
                tmpRow = new double[model.getNVar()+1];
                tmpRow[0] = i;
                for(int a1 =1; a1 < tmpRow.length; a1++)
                	tmpRow[a1] = result[a1-1];
                try{
                	dataset.addRow(tmpRow);
                } catch(DatasetException de) {
                	System.out.println("" + de);
                }
                
                for (int j = 0; j < result.length; j++) {
                	if(Double.isNaN(result[j]))
                		break;
                    transX = (int) plot.getDomainAxis().valueToJava2D(
                    i*stepSize, dataArea, RectangleEdge.BOTTOM);
                    transY = (int) plot.getRangeAxis().valueToJava2D(
                    result[j], dataArea, RectangleEdge.LEFT);
                    
                    g2.setPaint(paintSequence[j]);
                    
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
            }
            
        }
        else{//? 14.7.2004 section of code above
            for (int i = (int)lower; i < (int)upper; i += timeStep) {
                double[] result=new double[model.getNVar()];
                int transX, transY;
                
                result = Lua.evaluateLyapunovExponents(model, parameters, initialPoint, i);
                tmpRow = new double[model.getNVar()+1];
                tmpRow[0] = i;
                for(int a1 =1; a1 < tmpRow.length; a1++)
                	tmpRow[a1] = result[a1-1];
                try{
                	dataset.addRow(tmpRow);
                } catch(DatasetException de) {
                	System.out.println("" + de);
                }
                
                for (int j = 0; j < result.length; j++) {
                	if(Double.isNaN(result[j]))
                		break;
                	
                    transX = (int) plot.getDomainAxis().valueToJava2D(
                    i, dataArea, RectangleEdge.BOTTOM);
                    transY = (int) plot.getRangeAxis().valueToJava2D(
                    result[j], dataArea, RectangleEdge.LEFT);
                    
                    g2.setPaint(paintSequence[j]);
                    
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
            }
        }               //? } belongs to else 14.7.2004
        
        return true;
    }
    

    
    
    public boolean renderVsParameter(
    Graphics2D g2,
    Rectangle2D dataArea) {
        
        final int imageWidth = (int)dataArea.getWidth();
        
        g2.setPaint(Color.red);
        
        final double parStep;
        
        
        int prevX[] = new int[model.getNVar()];
        int prevY[] = new int[model.getNVar()];
        
        //? 28.7.2004
        
        ValueAxis domainAxis=plot.getDomainAxis();
        lower=domainAxis.getRange().getLowerBound();
        upper=domainAxis.getRange().getUpperBound();
        parStep = Math.abs(upper - lower) / imageWidth;
        
    	String colNames[] = new String[model.getNVar()+1];
    	colNames[0] = firstParLabel;
    	for(int j=1; j<(model.getNVar()+1); j++)
    		colNames[j] = "" + (new Integer(j));
    	Dataset dataset = new Dataset(colNames);
    	lyapunovComponent.setDataobject(dataset);
    	double tmpRow[];        
        
        if (model instanceof ODE){
            double step=stepSize;  // stepSize and timeStep probably mean the same thing, one for discrete another for ODE
            
            for (double i = lower; i < upper; i += parStep) {
                double[] result=new double[model.getNVar()];//initializing not needed but compiler too cautious.
                int transX, transY;
                
                parameters.put(firstParLabel, i);

                result = Lua.evaluateLyapunovExponentsODE(
                    model, parameters, initialPoint, timePeriod,stepSize);
                
                tmpRow = new double[model.getNVar()+1];
                tmpRow[0] = i;
                for(int a1 =1; a1 < tmpRow.length; a1++)
                	tmpRow[a1] = result[a1-1];
                try{
                	dataset.addRow(tmpRow);
                } catch(DatasetException de) {
                	System.out.println("" + de);
                }
                                
                for (int j = 0; j < result.length; j++) {
                	if(Double.isNaN(result[j]))
                		break;
                    
                    transX = (int) plot.getDomainAxis().valueToJava2D(
                    i, dataArea, RectangleEdge.BOTTOM);
                    
                    transY = (int) plot.getRangeAxis().valueToJava2D(
                    result[j], dataArea, RectangleEdge.LEFT);
                    
                    g2.setPaint(paintSequence[j]);
                    
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
            }
            
        }//? 28.7.2004
        else{
            
            for (double i = lower; i < upper; i += parStep) {
                double[] result;
                int transX, transY;
                
                parameters.put(firstParLabel, i);
                
                result = Lua.evaluateLyapunovExponents(
                model, parameters, initialPoint, iterations);
                tmpRow = new double[model.getNVar()+1];
                tmpRow[0] = i;
                for(int a1 =1; a1 < tmpRow.length; a1++)
                	tmpRow[a1] = result[a1-1];
                try{
                	dataset.addRow(tmpRow);
                } catch(DatasetException de) {
                	System.out.println("" + de);
                }                
                
                for (int j = 0; j < result.length; j++) {
                	if(Double.isNaN(result[j]))
                		break;
                    
                    transX = (int) plot.getDomainAxis().valueToJava2D(
                    i, dataArea, RectangleEdge.BOTTOM);
                    
                    transY = (int) plot.getRangeAxis().valueToJava2D(
                    result[j], dataArea, RectangleEdge.LEFT);
                    
                    g2.setPaint(paintSequence[j]);
                    
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
            }
        }
        return true;
    }
    
    
    
    public boolean renderArea(
    Graphics2D g2, Rectangle2D dataArea) {
        
        g2.setPaint(plot.paint);
        
        
        if (pass==1){
            if (image!=null){
                double x= dataArea.getX();
                double y= dataArea.getY();
                //there is a problem when using Graphics2D with affine transform 
                //and BufferedImage; using subclass of Image returned below.
                //rescaling needed because adding legend causes dataArea to change.
                Image scaledImage= image.getScaledInstance((int)dataArea.getWidth()-1,
                                                                   (int) dataArea.getHeight()-1, Image.SCALE_DEFAULT);
                g2.drawImage(scaledImage, 
                (int)x+1, 
                (int)y+1,
                (int)dataArea.getWidth()-1, 
                (int)dataArea.getHeight()-1, 
                this);
                //g2.translate(-1,-1);
                //g2.drawRect((int) x, (int) y, (int) dataArea.getWidth(),(int) dataArea.getHeight());
                //g2.translate(1,1);
            }
            return true;
        }
        
        final double parHStep, parVStep;
        double parHLower = plot.getDomainAxis().getRange().getLowerBound();
        double parHUpper = plot.getDomainAxis().getRange().getUpperBound();
        double parVLower = plot.getRangeAxis().getRange().getLowerBound();
        double parVUpper = plot.getRangeAxis().getRange().getUpperBound();
        
        parHStep = Math.abs(parHUpper - parHLower) / dataArea.getWidth();
        parVStep = Math.abs(parVUpper - parVLower) / dataArea.getHeight();
        
        image = new BufferedImage(
        (int)dataArea.getWidth(),
        (int)dataArea.getHeight(),
        BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        DataBufferInt dataBuffer = (DataBufferInt)raster.getDataBuffer();
        int[] data = dataBuffer.getData();
        
        final double parHStart = parHLower + parHStep / 2;
        final double  parVStart = parVUpper - parVStep / 2;
        
        if (model instanceof ODE){
            double step=stepSize;  // stepSize and timeStep probably mean the same thing, one for discrete another for ODE
            double[] result=new double[model.getNVar()];
            
            for (int i = 0; i < (int)dataArea.getWidth(); i++) {
                for (int j = 0; j < (int)dataArea.getHeight(); j++) {
                    
                    parameters.put(firstParLabel, parHStart + i * parHStep);
                    parameters.put(secondParLabel, parVStart - j * parVStep);
                    
                    int color;
                    result = Lua.evaluateLyapunovExponentsODE(
                    model, parameters, initialPoint, timePeriod,stepSize);
                    
                    if (result == null) {
                        System.out.println("i: " + i + " j: " + j);
                        System.out.println("par1: " + parHStart + i * parHStep);
                        System.out.println("par2: " + parVStart + j * parVStep);
                        g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                        return false;
                    }
                    
                    
                    int zer=0;
                    int pos=0;
                    int neg=0;
                    int nan=0;
                    for (int ii=0;ii<result.length;ii++){
                        if (Math.abs(result[ii])==(1.0/0.0))
                            nan++;
                        else if (Math.abs(result[ii])<=epsilon)
                            zer=zer+1;
                        else if (result[ii]>epsilon)
                            pos=pos+1;
                        else if (result[ii]< (-epsilon))
                            neg=neg+1;
                        else
                            nan++;
                    }
                    
                    color=(lyapunovColors.getColor(zer,pos,neg,nan)).getRGB();
                    ExpsSigns es=new ExpsSigns(zer,pos,neg,nan);
                    if (signsSet.contains(es)){
                        
                    }
                    else{
                        signsSet.add(es);
                        
                    }
                    
                    
                    data[i + j * (int)dataArea.getWidth()] = color;
                    
                    if (stopped == true) {
                        return false;
                    }
                    if (j == (int)dataArea.getHeight() - 1) {
                        g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                    }
                }
            }
        }
        else{
            
            
            for (int i = 0; i < (int)dataArea.getWidth(); i++) {
                for (int j = 0; j < (int)dataArea.getHeight(); j++) {
                    
                    parameters.put(firstParLabel, parHStart + i * parHStep);
                    parameters.put(secondParLabel, parVStart - j * parVStep);
                    
                    double[] result;
                    int color;
                    
                    result = Lua.evaluateLyapunovExponents(
                    model, parameters, initialPoint, iterations);
                    
                    if (result == null) {
                        System.out.println("i: " + i + " j: " + j);
                        System.out.println("par1: " + parHStart + i * parHStep);
                        System.out.println("par2: " + parVStart + j * parVStep);
                        g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                        return false;
                    }
      
                    int zer=0;
                    int pos=0;
                    int neg=0;
                    int nan=0;
                    for (int ii=0;ii<result.length;ii++){
                        if (Math.abs(result[ii])==(1.0/0.0))
                            nan++;
                        else if (Math.abs(result[ii])<=epsilon)
                            zer=zer+1;
                        else if (result[ii]>epsilon)
                            pos=pos+1;
                        else if (result[ii]< (-epsilon))
                            neg=neg+1;
                        else
                            nan++;
                    }
                    
                    color=(lyapunovColors.getColor(zer,pos,neg,nan)).getRGB();
                    ExpsSigns es=new ExpsSigns(zer,pos,neg,nan);
                    if (signsSet.contains(es)){
                        
                    }
                    else{
                        signsSet.add(es);
                    }
                    
                    
                    data[i + j * (int)dataArea.getWidth()] = color;
                    
                    if (stopped == true) {
                        return false;
                    }
                    if (j == (int)dataArea.getHeight() - 1) {
                        g2.drawImage(image, null, (int)dataArea.getX() + 1, (int)dataArea.getY() + 1);
                    }
                }
            }
        }
        return true;
    }
    
    
    
    public void stop() {
        stopped = true;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public void initializeVsTime(
    VariableDoubles parameters,
    VariableDoubles initialValues,
    Range timeRange) {
        
        this.parameters.putAll(parameters);
        this.initialPoint.putAll(initialValues);
        this.lower = (int)timeRange.getLowerBound();
        this.upper = (int)timeRange.getUpperBound();
        
        this.type = TYPE_VSTIME;
    }
    
    //?
    public void initializeVsTime(
    VariableDoubles parameters,
    VariableDoubles initialValues,
    Range timeRange, double stepSize) {
        
        this.parameters.putAll(parameters);
        this.initialPoint.putAll(initialValues);
        this.lower = (int)timeRange.getLowerBound();
        this.upper = (int)timeRange.getUpperBound();
        this.stepSize=stepSize;
        this.type = TYPE_VSTIME;
    }
    //?
    
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
        
        this.type = TYPE_VSPAR;
    }
    
    //?
    public void initializeVsParameter(
    VariableDoubles parameters,
    VariableDoubles initialValues,
    String parLabel,
    Range parameterRange,
    double timePeriod, double stepSize) {
        
        this.parameters.putAll(parameters);
        this.initialPoint.putAll(initialValues);
        this.firstParLabel = parLabel;
        this.lower = parameterRange.getLowerBound();
        this.upper = parameterRange.getUpperBound();
        this.timePeriod=timePeriod;
        this.stepSize=stepSize;
        
        this.type = TYPE_VSPAR;
    }
    //?
    
    
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
        
        plot.getDomainAxis().setLabel(firstParLabel);
        plot.getRangeAxis().setLabel(secondParLabel);
        
        this.type = TYPE_AREA;
    }
    
    //?
    public void initializeParArea(
    VariableDoubles initialValues,
    VariableDoubles par, // fixed ones
    String firstParLabel,
    String secondParLabel,
    double epsilon,
    double timePeriod, double stepSize) {
        
        this.parameters.putAll(par);
        this.initialPoint.putAll(initialValues);
        this.firstParLabel = firstParLabel;
        this.secondParLabel = secondParLabel;
        this.epsilon = epsilon;
        this.timePeriod = timePeriod;
        this.stepSize=stepSize;
        
        plot.getDomainAxis().setLabel(firstParLabel);
        plot.getRangeAxis().setLabel(secondParLabel);
        
        this.type = TYPE_AREA;
    }
    //?
    
    
    public LegendItemCollection getLegendItems() {
        if (type != TYPE_AREA)
            return null;
        LegendItemCollection legendItems = new LegendItemCollection();
        
        Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
        BasicStroke.JOIN_BEVEL);
        Shape shape = new Rectangle2D.Double(-3, -3, 6, 6);
        
           
        Iterator i= signsSet.iterator();
        while(i.hasNext()){
            ExpsSigns es=(ExpsSigns) i.next();
            Color color=lyapunovColors.getColor(es.zer,es.pos,es.neg,es.nan);
            legendItems.add(new LegendItem(es.toString(),
            "",
            shape,
            true,
            color,
            stroke,
            Color.yellow,
            stroke
            ));
        }
        if (pass==0) {
            signsSet.clear();
        }
        return legendItems;
    }
    
    
    public boolean isBigDots() {
        return bigDots;
    }
    
    public boolean isConnectWithLines() {
        return connectWithLines;
    }
    
    public void setBigDots(boolean b) {
        bigDots = b;
    }
    
    public void setConnectWithLines(boolean b) {
        connectWithLines = b;
    }
    
    public void setPass(int pass){
        this.pass=pass;
    }
    
    public int getPass(){
        return pass;
    }
    
    //(x,y) stays fixed, but dataArea width and height get smaller.
    //this is an auxiliary method for renderArea
    //eventually not used as gives slightly incorrect output.
    private AffineTransform createTransform(double x, double y, BufferedImage image,Rectangle2D dataArea){
        int wi=image.getWidth();wi--;
        int hi=image.getHeight();hi--;
        double wd=dataArea.getWidth();wd--;
        double hd=dataArea.getHeight();hd--;
        double a=wd/wi;
        double b=hd/hi;
        double m00=a;double m01=0;double m02=-x*a+x;
        double m10=0;double m11=b;double m12=-y*b+y;
        AffineTransform at=new AffineTransform(m00,m10,m01,m11,m02,m12);
        return at;
    }
    
    public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
        return true;
    }    
    
    
    class ExpsSigns{
        int neg;
        int zer;
        int pos;
        int nan;
        
        ExpsSigns(int z,int p,int n,int nan){
            neg=n;zer=z;pos=p;nan=nan;
        }
        
        public int hashCode(){
            String s="n"+neg+"p"+pos+"z"+zer+"nan"+nan;
            return s.hashCode();
        }
        
        public String toString(){
            String s;
            s = "" + zer + "zero, "
                    + neg + "negative, "
                    + pos + "positive, "
                    + nan + "diverging";
            return s;
        }
        
        public boolean equals(Object o){
            try{
                ExpsSigns es=(ExpsSigns) o;
                if (es.neg==neg && es.pos==pos && es.zer==zer && es.nan==nan)
                    return true;
                else
                    return false;
            }
            catch(Exception e){
                return false;
            }
        }
    }

}
