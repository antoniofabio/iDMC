/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004-2006 Marji Lines and Alfredo Medio.
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
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Vector;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.tsho.dmc2.core.algorithms.Grid;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.basin.*;
import org.tsho.dmc2.core.algorithms.*;

public class BasinRenderer implements DmcPlotRenderer {
	/** this is needed for comunicating data to be saved*/
    private BasinComponent basinComponent;
    /** plotting area */
    private DmcRenderablePlot plot;
    /** map to be analysed */
    private SimpleMap map;
    
    public static final int FAST_ALGORITHM = 0;
    public static final int SLOW_ALGORITHM = 1;
    public static final int NATIVE_ALGORITHM = 2;
    private int type;

    /** parameters */
    private double[] parameters;
    /** now stands for the number of iterations needed to get an attractor. */
    private int attractorLimit;
    /** now stands for the number of iterations done for each point either to draw the attractor or to check whether converges to an attractor. */
    private int attractorIterations;
    /** Absolute value for which a coordinate value can be considered 'infinity' */    
    private double infinity;
    /** number of trials to preliminarly find attractors */
    private int numberOfRandomPoints;

    // flags    
    private boolean stopped;
    private boolean bigDotsEnabled = false;
    
    // internal state
    private int state;
    private Grid grid;
    private int gridWidth;
    private int gridHeight;
    
    /** Records for each attractor found a point in its basin. 
     * This way, also after the image is enlarged,
     * we may generate the attractors and basins anew with the same colors. */
    private Vector attractorsSamplePoints;
    private Graphics2D g2;
    private BufferedImage image;
    private int imageX, imageY, rate;
    private int[] imageData;
    private int[] gridColors;
    private static ColorSettings colorSettings = new ColorSettings(BasinRenderer.class);

    public BasinRenderer(final SimpleMap map, final DmcRenderablePlot plot, BasinComponent bc) {
        this.basinComponent = bc;
        this.map = map;
        this.plot = plot;
    }
    
    public void initialize() {
        this.stopped = false;
    }

    public void render(
    final Graphics2D g2, final Rectangle2D dataArea,
    final PlotRenderingInfo info) {
        attractorsSamplePoints = new Vector();
        
        state = STATE_RUNNING;

        gridWidth = (int) dataArea.getWidth();
        gridHeight = (int) dataArea.getHeight();

        this.imageX = (int) dataArea.getX() + 1;
        this.imageY = (int) dataArea.getY();
        
        this.image = new BufferedImage(
                        gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
    	this.g2 = g2;
        WritableRaster raster = image.getRaster();

        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();
        
        double maxCoordinate=Math.abs(domainAxis.getUpperBound());
        if (Math.abs(domainAxis.getLowerBound())>maxCoordinate )
            maxCoordinate=Math.abs(domainAxis.getLowerBound());
        if (Math.abs(rangeAxis.getLowerBound())>maxCoordinate)
            maxCoordinate=Math.abs(rangeAxis.getLowerBound());
        if (Math.abs(rangeAxis.getUpperBound())>maxCoordinate)
            maxCoordinate=Math.abs(rangeAxis.getUpperBound());
        if (infinity<maxCoordinate)
            infinity=maxCoordinate+1;
        
        grid = new Grid(
        		new double[] {domainAxis.getLowerBound(), 
        				domainAxis.getUpperBound(), rangeAxis.getLowerBound(),
        				rangeAxis.getUpperBound()}, 
        				gridHeight, gridWidth);
        imageData = ((DataBufferInt) raster.getDataBuffer()).getData();
        rate = gridHeight * gridWidth / 100;
        attractorsSamplePoints = new Vector();
        
        BasinsAlgorithm bA=null;
        if(type==FAST_ALGORITHM)
        	bA = new FastBasinsAlgorithm(this);
        else if(type==SLOW_ALGORITHM)
        	bA = new SlowBasinsAlgorithm(this);
        else if(type==NATIVE_ALGORITHM)
        	bA = new NativeBasinsAlgorithm(this);
      	bA.run();
        
        /** Now that grid is computed, pass it to the BasinComponent for possible storing */
        int [] tmp = grid.getData();
        grid.setData((int[]) tmp.clone());//from now on, grid data is disconnected from image data
        basinComponent.setDataobject(grid);

        drawImage();
        state = STATE_FINISHED;
    }
    
    public void drawImage() {
    	int [] gridData = grid.getData();
    	gridColors = colorSettings.getArray();
    	int code;
    	for(int i=0; i<gridData.length; i++) { //color code traslation
    		code = gridData[i];
    		if(code<gridColors.length)
    			imageData[i] = gridColors[gridData[i]];
    		else
    			imageData[i] = gridColors[gridColors.length-1];
    	}
        g2.drawImage(image, null, imageX, imageY);
        if(bigDotsEnabled) {
        	double[] tmpAttr;
        	int [] tmpXy;
	        for(int j=0; j<attractorsSamplePoints.size(); j++) {
	        	g2.setColor(new Color(gridColors[Math.min(2+j*2, gridColors.length-1)]));
	        	tmpAttr = (double[]) attractorsSamplePoints.elementAt(j);
	        	for(int i=0; i<attractorIterations; i++) {
	        		tmpXy = grid.pointToXy(tmpAttr);
	        		g2.fillRect(imageX + tmpXy[0] - 1, imageY + gridHeight - tmpXy[1] - 1, 3, 3);
	        		iterate(tmpAttr);
	        	}
	        }
        }
    }
    
    public void initialize(
    final double[] parameters, final int attractorLimit,
    final int attractorIterations, final int trials, final double infinity) {
        this.parameters = parameters;
        this.attractorLimit = attractorLimit;
        this.attractorIterations = attractorIterations;
        this.infinity = infinity;
        numberOfRandomPoints=trials;
    }
    
    public boolean iterate(double[] p) {
        map.evaluate(parameters, (double[]) p.clone(), p);
        return true;
    }
    
    public LegendItemCollection getLegendItems() {
        return null;
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
    
    public boolean isPointInfinite(double[] p) {
        Double a0=new Double(p[0]);
        Double a1=new Double(p[1]);
        if (a0.isInfinite() || a1.isInfinite() || a0.isNaN()|| a1.isNaN() || Math.abs(p[0])>infinity || Math.abs(p[1])>infinity)
            return true;
        else
            return false;
    }

    public void setType(int type){
        this.type=type;
    }
    
    public void setBigDotsEnabled(boolean flag){
        bigDotsEnabled=flag;
    }

    /**
     * @return the grid
     */
    public Grid getGrid() {
            return grid;
    }

    /**
     * @return the infinity
     */
    public double getInfinity() {
            return infinity;
    }

    /**
     * @return the attractorLimit
     */
    public int getAttractorLimit() {
            return attractorLimit;
    }

    /**
     * @return the attractorIterations
     */
    public int getAttractorIterations() {
            return attractorIterations;
    }

    /**
     * @return the stopped
     */
    public boolean isStopped() {
            return stopped;
    }

    /**
     * @return the numberOfRandomPoints
     */
    public int getNumberOfRandomPoints() {
            return numberOfRandomPoints;
    }

    /**
     * @return the rate
     */
    public int getRate() {
            return rate;
    }

    /**
     * @return the attractorsSamplePoints
     */
    public Vector getAttractorsSamplePoints() {
            return attractorsSamplePoints;
    }

    /**
     * @return the colorSettings
     */
    public ColorSettings getColorSettings() {
            return colorSettings;
    }

    /**
     * @param colorSettings the colorSettings to set
     */
    public void setColorSettings(ColorSettings colorSettings) {
            BasinRenderer.colorSettings = colorSettings;
    }

    public SimpleMap getMap() {
        return map;
    }
}
