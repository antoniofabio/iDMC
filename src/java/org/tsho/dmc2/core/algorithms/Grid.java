/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2006 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>
 * Extended by Antonio, Fabio Di Narzo <antonio.dinarzo@studio.unibo.it>
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
package org.tsho.dmc2.core.algorithms;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.*;

import org.tsho.dmc2.core.util.DataObject;

/**
 * Encapsulates a full basins grid with its methods.
 * Is capable of getting/setting grid cell integer values basing on real (x,y) coordinates
 * Has a nextFreeCell iterator method based on the Grid.EMPTY integer costant.
 * The utility method isPointInfinite(p) checks if point coordinate values exceed some fixed 
 * 	threshold
 * 
 * COORDINATES SYSTEM
 * Real axes are oriented as classycal cartesianan ones: origin down left, x axis growing right,
 * y axis growing top
 * 
 * Discrete corresponding grid coordinates have the same orientation, with all indexes 
 * starting at 0.
 * 
 * Internally, the full grid is indexed with a single integer, with the following schema:  
 * 	---------
 *  |1|2|3|4|
 *  ---------
 *  |5|6|7|8|
 *  ---------
 *  This is also the way in which cell pointers grows when calling 'nextFreeCell'
 *  
 *  See GridTest class for usage examples and for what to expect from this class.
 *  
 * */
public class Grid implements DataObject {
	public static final int EMPTY=0;
	int[] data;
	int nr, nc;
	double[] ranges = new double[4];//x1,x2,y1,y2
	double xEpsilon, yEpsilon;
	int currId;
	int numcells; 
	
	public Grid(double[] ranges, int nr, int nc) {
		init(ranges, nr,nc, EMPTY);
	}
	
	public Grid(double[] ranges, int nr, int nc, int value) {
		init(ranges, nr,nc,value);
	}
	
	private void init(double[] ranges, int nr, int nc, int value) {
		this.ranges = (double[]) ranges.clone();
		numcells=nr*nc;
		data = new int[numcells];
		this.nr = nr;
		this.nc = nc;
		xEpsilon = (ranges[1] - ranges[0]) / (double) nc;
		yEpsilon = (ranges[3] - ranges[2]) / (double) nr;
		currId = 0;
		data = new int[numcells];
		for(int i=0; i<(numcells); i++)
			data[i] = value;     		
	}
	
    public boolean isPointInsideBounds(double[] p) {
    	int[] ans = pointToXy(p);
        if (ans[1] > nr - 1
            || ans[1] < 0
            || ans[0] > nc - 1
            || ans[0] < 0) {
        		return false;
        }
        return true;
    }
	

    /**
     * Set internal data buffer
     * */
    public void setData(int[] newData) {
    	if(newData.length != numcells)
    		throw new RuntimeException("Invalid data buffer for storing basins");
    	data = newData;
    }
    
    /**
     * Get internal data buffer
     * */
	public int[] getData() {
		return data;
	}
	
	public int getNr(){
		return nr;
	}
	
	public int getNc(){
		return nc;
	}

	/**
	 * Save grid contents to file 'f'
	 * */
	public void save(File f) throws IOException {
		if(data==null)
			throw new RuntimeException("No stored data to save");
		DataOutputStream out = new DataOutputStream(
				new GZIPOutputStream(
				        new FileOutputStream(f)));				
		for(int i=0; i<4; i++)
			out.writeDouble(ranges[i]);
		out.writeInt(nr);
		out.writeInt(nc);
		for(int i=0; i<data.length; i++)
			out.writeInt(data[i]);
		out.close();
	}
	
	/**
	 * Get grid ranges in the form (x1,x2,y1,y2)
	 * */
	public double[] getRanges() {
		return (double[]) ranges.clone();
	}
	
	/**
	 * Returns true if there are more free cells, and sets internal counters
	 * 	accordingly 
	 * */
	public boolean nextFreeCell() {
		while(currId<numcells) {
			if(data[currId]==EMPTY)
				return true;
			currId++;
		}
		return false;
	}

	/**
	 * Gets the value corresponding to the given point location
	 * */
	public int getValue(double[] p){
		return data[pointToId(p)];
	}
	
	/**
	 * Set cell value
	 * */
	public void setValue(double[] p, int val){
		data[pointToId(p)] = val;
	}

	/**
	 * Set cell value
	 * */	
	public void setValue(int x, int y, int val){
		data[xyToId(x,y)] = val;
	}
	
	/**
	 * Fill a track with a value
	 * */
	public void setValue(double[][] p, int val) {
		for(int i=0; i<p.length; i++)
			setValue(p[i], val);
	}
	
	/**
	 * Set *current* cell value
	 * */
	public void setValue(int val) {
		data[currId] = val;
	}
	
	/**
	 * Get *current* cell value
	 * */
	public int getValue() {
		return data[currId];
	}
	
	
	/**
	 * Returns current cell id
	 * */
	public int getCurrId(){
		return currId;
	}

	/**
	 * Returns current point coordinates
	 * */    	
	public double[] getCurrPoint(){
		return idToPoint(currId);
	}
	
	/**
	 * Converts the internal single (id) representation of a point to the 
	 * 	(real,real) representation
	 * */    	
	private double[] idToPoint(int id) {
		int[] xy = idToXy(id);
		return xyToPoint(xy[0],xy[1]);
	}
	
	public double[] xyToPoint(int x, int y) {
		return new double[] {ranges[0]+(x+0.5)*xEpsilon, ranges[2]+(y+0.5)*yEpsilon};
	}
	
	public int[] pointToXy(double[] p) {
		int[] ans = new int[2];
		ans[0] = (int) Math.floor((p[0] - ranges[0])/xEpsilon);
		ans[1] = (int) Math.floor((p[1] - ranges[2])/yEpsilon);
		return ans;
	}
	
	/**
	 * Converts the (real,real) representation of a point to the single internal (id)
	 * 	representation
	 * */
	private int pointToId(double[] p) {
		int[] xy = pointToXy(p); 
		return xyToId(xy[0],xy[1]);
	}
	
	/**
	 * Converts the (x,y) representation of a cell to the single index (id) representation
	 * */
	private int xyToId(int x, int y) {
		return x+(nr-y-1)*nc;
	}

	/**
	 * Converts the single index (id) representation of a cell to the (x,y) representation
	 * */    	
	private int[] idToXy(int id){
		int[] ans = new int[2];
		ans[1] = nr - id/nc - 1; //row id
		ans[0] = id - (nr-ans[1]-1)*nc; //column id
		return(ans);
	}
	
}
