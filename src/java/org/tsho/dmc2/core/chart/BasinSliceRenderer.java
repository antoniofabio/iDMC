/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2008 Marji Lines and Alfredo Medio.
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
import org.tsho.dmc2.core.dlua.LuaModel;
import org.tsho.dmc2.core.dlua.LuaBasinMulti;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.basinslice.*;
import org.tsho.dmc2.core.algorithms.*;

public class BasinSliceRenderer implements DmcPlotRenderer {
	/** this is needed for comunicating data to be saved*/
    private BasinSliceComponent basinComponent;
    
    private BasinSliceControlForm controlForm;
    
    /** plotting area */
    private DmcRenderablePlot plot;
    
    /** low level basin_multi object*/
    LuaBasinMulti bs;
    
    // flags    
    private boolean stopped;
    private boolean bigDotsEnabled = false;
    
    // internal state
    private int state;
    private int gridWidth;
    private int gridHeight;
    private int xvar, yvar;
    private double xEps, yEps;
    private double[] xRange = new double[2];
    private double[] yRange = new double[2];
    
    private Graphics2D g2;
    private BufferedImage image;
    private int imageX, imageY, rate;
    private int[] imageData;
    private int[] gridColors;
    private int[] gridData;
    private double[] par, var;
    private static ColorSettings colorSettings = new ColorSettings(BasinSliceRenderer.class);

    public BasinSliceRenderer(final SimpleMap map, final DmcRenderablePlot plot, BasinSliceComponent bc) {
        this.basinComponent = bc;
        this.plot = plot;
        
        controlForm = bc.getBasinSliceControlForm();
    }
    
    public void initialize() {
        this.stopped = false;
        try {
            par = controlForm.getParameterValues().toArray(controlForm.getParameterValues());
            if (controlForm.getNVar() > 2)
                var = controlForm.getVariableValues().toArray(controlForm.getVariableValues());
            else {
                var = new double[2];
                var[0] = var[1] = 0.0; //these values are actually ignored by the idmclib
            }
        } catch(Exception e) {
            //FIXME: show pertinent error message box
            System.out.println("error getting parameters and variables settings");
            System.out.println(e.getMessage());
        }
    }

    public void render(
    final Graphics2D g2, final Rectangle2D dataArea,
    final PlotRenderingInfo info) {
        gridWidth = (int) dataArea.getWidth();
        gridHeight = (int) dataArea.getHeight();
        xvar = controlForm.getXVar();
        yvar = controlForm.getYVar();
        
    	gridData = new int[gridWidth * gridHeight];

        state = STATE_RUNNING;

        this.imageX = (int) dataArea.getX() + 1;
        this.imageY = (int) dataArea.getY();
        
        this.image = new BufferedImage(
                        gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
        
        basinComponent.setDataobject(null);
        
        this.g2 = g2;
        WritableRaster raster = image.getRaster();

        try{
        ValueAxis rx = plot.getDomainAxis();
        ValueAxis ry = plot.getRangeAxis();
        
        xRange[0] = rx.getLowerBound();
        xRange[1] = rx.getUpperBound();
        yRange[0] = ry.getLowerBound();
        yRange[1] = ry.getUpperBound();
        xEps = (xRange[1] - xRange[0]) / (double) gridWidth;
        yEps = (yRange[1] - yRange[0]) / (double) gridHeight;
        
        imageData = ((DataBufferInt) raster.getDataBuffer()).getData();
        rate = Math.max(1, gridHeight * gridWidth / 100);
        
        LuaModel m = (LuaModel) basinComponent.getModel();
        m.setRngSeed(xvar);

        bs = new LuaBasinMulti((LuaModel) basinComponent.getModel(), par,
            xRange[0], xRange[1], (int) gridWidth,
            yRange[0], yRange[1], (int) gridHeight,
            controlForm.getEpsilon(), controlForm.getLimit(), controlForm.getIterations(),
            controlForm.getTrials(), controlForm.getXVar(), controlForm.getYVar(),
            var);
        } catch (Exception e) {
            //FIXME: show pertinent err msg box
            System.out.println(e.getMessage());
        }
        
        bs.findAttractors();
        
        int i=0;
        while(!bs.finished() & (!stopped)) {
            if((i % rate)==0) {
                drawImage();
                //TODO: update progress bar
            }
            bs.step();
            i++;
        }
        
        /** TODO: pass basin data to the plot component */
        basinComponent.setDataobject(null);

        drawImage();
        if(stopped) {
            stopped = false;
            state = STATE_STOPPED;
        } else {
            state = STATE_FINISHED;
        }
    }
    
    public void drawImage() {
    	gridColors = colorSettings.getArray();
    	int code;
        
        gridData = bs.getData();
    	for(int i=0; i<getGridData().length; i++) { //color code traslation
            code = getGridData()[i];
            if(code<gridColors.length)
                    imageData[i] = gridColors[getGridData()[i]];
            else
                    imageData[i] = gridColors[gridColors.length-1];
    	}
        g2.drawImage(image, null, imageX, imageY);
        
        //add attractors
        int[] pt;
        Vector attrList = bs.getAttractors();
        for(int i=0; i<attrList.size(); i++) {
            g2.setColor(new Color(gridColors[Math.min(2+i*2, gridColors.length-1)]));
            Vector attr = (Vector) attrList.get(i);
            for(int j=0; j<attr.size(); j++) {
                pt = point2XY((double[]) attr.get(j));
                g2.fillRect(imageX + pt[0] - 1, imageY + gridHeight - pt[1],
                        bigDotsEnabled ? 3 : 1, bigDotsEnabled ? 3 : 1);
            }
        }
    }
    
    private int[] point2XY(double[] p) {
        int[] ans = new int[2];
        ans[0] = (int) Math.floor((p[xvar] - xRange[0])/xEps);
        ans[1] = (int) Math.floor((p[yvar] - yRange[0])/yEps);
        return ans;
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

    public int[] getGridData() {
        return gridData;
    }
}