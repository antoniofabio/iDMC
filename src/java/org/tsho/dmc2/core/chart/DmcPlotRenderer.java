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
import org.jfree.chart.plot.PlotRenderingInfo;

public interface DmcPlotRenderer {
    int STATE_NONE       = 0x0000;
    int STATE_RANGES     = 0x0001;
    int STATE_TRANSIENTS = 0x0002;
    int STATE_POINTS     = 0x0004;
    int STATE_STOPPED    = 0x0008;
    int STATE_FINISHED   = 0x0010;
    int STATE_ERROR      = 0x0020;
    int STATE_RUNNING    = 0x0040;

    void initialize();

    void render(
            Graphics2D g2, Rectangle2D dataArea,
            PlotRenderingInfo info);

    void stop();

    int getState();
    void setState(int state);

    LegendItemCollection getLegendItems();
}
