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
package org.tsho.dmc2.managers;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import org.jfree.data.Range;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.CyclesRenderer;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.cycles.CyclesControlForm2;

/**
 *
 * @author tsho
 */
public class CyclesManager extends AbstractManager
                          implements AbstractManager.Crosshair,
                                     AbstractManager.BigDots,
                                     AbstractManager.GridLines,
                                     AbstractManager.Transparency,
                                     AbstractManager.AxesVisibility {

    private final SimpleMap model;
    private final CyclesControlForm2 form;

    private CyclesRenderer plotRenderer;

    private boolean crosshair;
    private boolean bigDots;
    private boolean gridLines;

    private final int refreshSleepTime = 500;

    public CyclesManager(final SimpleMap model,
                        final CyclesControlForm2 form,
                        final ManagerListener2 frame) {

        super(frame);

        this.model = (SimpleMap) model;
        this.form = form;

        crosshair = false;
        gridLines = true;

        plotRenderer = new CyclesRenderer(model, plot);
        plot.setPlotRenderer(plotRenderer);

        plot.setDrawGridLines(gridLines);
        plot.setForegroundAlpha(0.1F);
        chart.setTitle(model.getName());

        setCrosshair(crosshair);

        // stop everything on resizing
        chartPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                CyclesManager.this.stopRendering();
            }
        });

        plot.addCoreStatusListener( new CoreStatusListener() {
            public void sendCoreStatus(final CoreStatusEvent event) {
                if (event.getType() == CoreStatusEvent.REDRAW) {
                    launchThread();
                }
                else if (event.getType() == CoreStatusEvent.REPAINT) {
                    chartPanel.repaint();
                }
            }
        });
    }

    public boolean doRendering() {

        VariableDoubles initial;
        VariableDoubles parameters;
        Range xRange;
        Range yRange;
        String xLabel;
        String yLabel;
        double epsilon;
        int period;
        int maxTries;

        try {
            initial = model.getVariables();
            parameters = form.getParameterValues();

            xRange = form.getXRange();
            yRange = form.getYRange();
            xLabel = form.getLabelOnX();
            yLabel = form.getLabelOnY();
            epsilon = form.getEpsilon();
            period = form.getPeriod();
            maxTries = form.getMaxTries();
        }
        catch (InvalidData e) {
            errorMessage = e.getMessage();
            return false;
        }

        plot.setNoData(false);
        plot.zoom(0.0);

        plotRenderer.initialize(
                parameters, initial, xLabel, yLabel,
                epsilon, period, maxTries);

        plotRenderer.setBigDots(bigDots);

        plot.setDataRanges(xRange, yRange);

        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        launchThread();
        return true;
    }

    private void launchThread() {

        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);

        Thread refreshJob = new RefreshThread();
        refreshJob.start();

        Thread plotJob = new PlotThread("DMCDUE - Cycles plotter", false);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }

    class RefreshThread extends Thread {

        int state = CyclesRenderer.STATE_NONE;
        int ratio;

        RefreshThread() {
            super("DMCDUE - CyclesRefresh");
        }

        public void run() {

            while (true) {
                int newState = plotRenderer.getState();

                if (newState != state) {

                    state = newState;

                    switch (newState) {
                        case CyclesRenderer.STATE_NONE:
                            ratio = 0;
                            break;
            
                        case CyclesRenderer.STATE_RUNNING:
//                            frame.progressString("plotting...");
                            break;
                    
                        case CyclesRenderer.STATE_FINISHED:
                        case CyclesRenderer.STATE_STOPPED:
//                            frame.progressString("ok. ");
                            return;
                   
                        default:
                            ratio = 0;
                    }
                }

                if ((state & CyclesRenderer.STATE_RUNNING) != 0 ) {
                    chartPanel.repaint();
                }

                try {
                    sleep(refreshSleepTime);
                }
                catch (InterruptedException e2) {}
            }
        }
    }

    public void stopRendering() {
        plotRenderer.stop();
    }

    public void setGridlines(final boolean flag) {
        plot.setDrawGridLines(flag);
    }

    public void setCrosshair(final boolean flag) {
        crosshair = flag;
        chartPanel.setHorizontalAxisTrace(flag);
        chartPanel.setVerticalAxisTrace(flag);
    }

    public boolean isCrosshair() {
        return crosshair;
    }

    public void clear() {
        plot.setNoData(true);
        plot.zoom(0.0);
        launchThread();
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public void setBigDots(boolean b) {
        bigDots = b;
    }

    public boolean isGridlines() {
        return gridLines;
    }

    public void setAlpha(final boolean flag) {
        plot.setAlpha(flag);
    }

    public void setAlphaValue(final float f) {
        plot.setForegroundAlpha(f);
    }

    public float getAlphaValue() {
        return plot.getForegroundAlpha();
    }
}
