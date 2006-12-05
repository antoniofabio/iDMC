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
import org.jfree.chart.plot.ValueAxisPlot;


public class DmcRenderablePlot
        extends AbstractDmcPlot
        implements ValueAxisPlot {

    private DmcPlotRenderer plotRenderer;

    public DmcRenderablePlot(
            final ValueAxis domainAxis, final ValueAxis rangeAxis) {
        super(domainAxis, rangeAxis);
    }

    public void render(
            final Graphics2D g2, final Rectangle2D dataArea,
            final PlotRenderingInfo info) {

        plotRenderer.render(g2, dataArea, info);
    }

    public String getPlotType() {
        // TODO Auto-generated method stub
        return null;
    }

    public LegendItemCollection getLegendItems() {
        if (plotRenderer != null) {
            return plotRenderer.getLegendItems();
        }
        return null;
    }

    public DmcPlotRenderer getPlotRenderer() {
        return plotRenderer;
    }

    public void setPlotRenderer(DmcPlotRenderer renderer) {
        plotRenderer = renderer;
    }
}
