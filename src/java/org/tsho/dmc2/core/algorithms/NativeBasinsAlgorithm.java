/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2007 Marji Lines and Alfredo Medio.
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
package org.tsho.dmc2.core.algorithms;

import java.util.Vector;

import org.tsho.dmc2.core.chart.BasinRenderer;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.jidmclib.*;

public class NativeBasinsAlgorithm implements BasinsAlgorithm {
    private BasinRenderer br;
    private Basin basin;
	
    private static final int INFINITY = 1;
    
    private Vector attractorsSamplePoints;
    
    private int rate, index;
    double[] startPoint, currentPoint; // (x, y)
    
    public NativeBasinsAlgorithm(BasinRenderer br){
        this.br = br;
        Grid grid = br.getGrid();
        double ranges[] = grid.getRanges();
        SimpleMap map = br.getMap();
        SWIGTYPE_p_double parameters = idmc.new_doubleArray(map.getNPar());
        basin = new Basin((Model) map, parameters, 
            ranges[0], ranges[1], grid.nc, 
            ranges[2], ranges[3], grid.nr, 
            br.getAttractorLimit(), br.getAttractorIterations());
        rate = br.getRate();
        attractorsSamplePoints = br.getAttractorsSamplePoints();
        index=0;
    }
    
    public void run() {
        SWIGTYPE_p_int b_data = basin.getData();
        int br_data[] = br.getGrid().getData();
        int len = br_data.length;
        index=0;
        while (!(basin.finished()!=0)) {
            basin.step_n(rate);
            index+=rate;
            /*Fill raster data as wanted by BasinRenderer*/
            for(int i=0; (i<index) && (i<len); i++)
                br_data[i] = idmc.intArray_getitem(b_data, i);
            br.drawImage();
            if (br.isStopped())
                return;
	}
        br.drawImage();
    }

}
