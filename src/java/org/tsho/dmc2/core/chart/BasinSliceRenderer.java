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
import org.tsho.dmc2.ui.basinslice.*;
import org.tsho.dmc2.core.algorithms.*;

public class BasinSliceRenderer implements DmcPlotRenderer {
	/** this is needed for comunicating data to be saved*/
    private BasinSliceComponent basinComponent;
    /** plotting area */
    private DmcRenderablePlot plot;
    /** map to be analysed */
    private SimpleMap map;
    
    // flags    
    private boolean stopped;
    private boolean bigDotsEnabled = false;
    
    // internal state
    private int state;
    private int gridWidth;
    private int gridHeight;
    
    private Graphics2D g2;
    private BufferedImage image;
    private int imageX, imageY, rate;
    private int[] imageData;
    private int[] gridColors;
    private static ColorSettings colorSettings = new ColorSettings(BasinRenderer.class);

    public BasinSliceRenderer(final SimpleMap map, final DmcRenderablePlot plot, BasinSliceComponent bc) {
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
        basinComponent.setDataobject(null);

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
        
        imageData = ((DataBufferInt) raster.getDataBuffer()).getData();
        rate = gridHeight * gridWidth / 100;
        
        //TODO: step through C level basin object
        
        /** TODO: pass basin data to the plot component */
        basinComponent.setDataobject(null);

        drawImage();
        state = STATE_FINISHED;
    }
    
    public void drawImage() {
    	int [] gridData = new int[gridWidth * gridHeight];
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
                /* TODO: step through basins attractors
	        for(int j=0; j<attractorsSamplePoints.size(); j++) {
	        	g2.setColor(new Color(gridColors[Math.min(2+j*2, gridColors.length-1)]));
	        	tmpAttr = (double[]) attractorsSamplePoints.elementAt(j);
	        	for(int i=0; i<attractorIterations; i++) {
	        		tmpXy = grid.pointToXy(tmpAttr);
	        		g2.fillRect(imageX + tmpXy[0] - 1, imageY + gridHeight - tmpXy[1] - 1, 3, 3);
	        		iterate(tmpAttr);
	        	}
	        }
                 */
        }
    }
    
    public void initialize(
    final double[] parameters, final int attractorLimit,
    final int attractorIterations, final int trials, final double infinity) {
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
    
    public void setBigDotsEnabled(boolean flag){
        bigDotsEnabled=flag;
    }

	/**
	 * @return the stopped
	 */
	public boolean isStopped() {
		return stopped;
	}

	/**
	 * @return the rate
	 */
	public int getRate() {
		return rate;
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
		BasinSliceRenderer.colorSettings = colorSettings;
	}    
}
