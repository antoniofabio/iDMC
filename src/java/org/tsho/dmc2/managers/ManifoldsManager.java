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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.Range;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.chart.DmcRenderablePlot;
import org.tsho.dmc2.core.chart.ManifoldsRenderer;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.manifolds.ManifoldsControlForm2;

public class ManifoldsManager extends AbstractManager
                                implements AbstractManager.GridLines,
                                           AbstractManager.Crosshair,
                                           AbstractManager.AxesVisibility {

    private final DifferentiableMap model;
    private final ManifoldsControlForm2 form;

    private ManifoldsRenderer plotRenderer;

    private boolean gridlines;
    private boolean crosshair;
    private byte plotType;

    public ManifoldsManager(
            final DifferentiableMap model,
            final ManifoldsControlForm2 form,
            final ManagerListener2 frame) {

        super(frame);

        this.model = model;
        this.form = form;

        /* defaults */
        setGridlines(true);
        crosshair = false;

        plotRenderer = new ManifoldsRenderer(model, plot);
        plot.setPlotRenderer(plotRenderer);

        plot.setForegroundAlpha(0.2F);
        plot.setAntialias(false);

        setCrosshair(crosshair);
        chart.setTitle(model.getName());

        plot.addCoreStatusListener(new CoreStatusListener() {
            public void sendCoreStatus(final CoreStatusEvent event) {
                if (event.getType() == CoreStatusEvent.REDRAW) {
                    launchThread();
                }
                else if (event.getType() == CoreStatusEvent.REPAINT) {
                    chartPanel.repaint();
                }
            }
        });

        // stop everything on resizing
        chartPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                ManifoldsManager.this.stopRendering();
            }
        });
    }

    public ManifoldsManager(
            final DifferentiableMap model,
            final ManifoldsControlForm2 form,
            final ManagerListener2 frame,
            final DmcChartPanel otherPanel) {

        super(frame);

        this.model = model;
        this.form = form;

        /* defaults */
        setGridlines(true);
        crosshair = false;

        xAxis = (NumberAxis) ((DmcRenderablePlot) otherPanel.getChart().getPlot()).getDomainAxis();
        yAxis = (NumberAxis) ((DmcRenderablePlot) otherPanel.getChart().getPlot()).getRangeAxis();

        plot = new DmcRenderablePlot(xAxis, yAxis);
        plotRenderer = new ManifoldsRenderer(model, plot);
        plot.setPlotRenderer(plotRenderer);

        plot.addCoreStatusListener(new CoreStatusListener() {
            public void sendCoreStatus(final CoreStatusEvent event) {
                if (event.getType() == CoreStatusEvent.REDRAW) {
                    launchThread();
                }
                else if (event.getType() == CoreStatusEvent.REPAINT) {
                    chartPanel.repaint();
                }
            }
        });

        chart = new JFreeChart(model.getName(), plot);

        chartPanel = new DmcChartPanel(chart);
        chartPanel.setMouseZoomable(true, false);

        // stop everything on resizing
        chartPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent e) {
                ManifoldsManager.this.stopRendering();
            }
        });

        setCrosshair(crosshair);
    }


    public boolean doRendering(final boolean redraw) {

        return render(redraw);

    }

    public boolean render(boolean redraw) {
        VariableDoubles parameters;
        VariableDoubles initial;
        double epsilon;

        Range horRange, verRange;
        Range lockoutHRange, lockoutVRange;
        int firstIteration, lastIteration;

        try {
            initial = form.getNodeValues();
            parameters = form.getParameterValues();
            epsilon = form.getEpsilon();
            horRange = form.getHorizontalRange();
            verRange = form.getVerticalRange();
            lockoutHRange = form.getLockoutHRange();
            lockoutVRange = form.getLockoutVRange();
            firstIteration = form.getIterationsFirst();
            lastIteration = form.getIterationsLast();
        }
        catch (InvalidData e) {
            getFrame().showInvalidDataDialog(e.getMessage());
            return false;
        }

        plot.setNoData(false);
        plot.setDrawGridLines(gridlines);

        if (plot.getPlotRenderer() != plotRenderer) {
            plot.setPlotRenderer(plotRenderer);
        }

        plotRenderer.initialize(
            plotType,
            initial, parameters, epsilon, lockoutHRange,
            lockoutVRange, firstIteration, lastIteration);

        //xAxis.setLabel(parLabel);
        //yAxis.setLabel(varLabel);

        if (!redraw) {
            plot.setDataRanges(horRange, verRange);
        }

        launchThread();
        return true;
    }

    class RefreshThread extends Thread {
        int state = ManifoldsRenderer.STATE_NONE;

        RefreshThread() {
            super("DMCDUE - ManifoldsRefresh");
        }

        public void run() {

            while (true) {
                int newState = plotRenderer.getState();

                if (newState != state) {

                    state = newState;

                    switch (newState) {
                        case ManifoldsRenderer.STATE_NONE:
//                            ratio = 0;
                            break;
        
                        case ManifoldsRenderer.STATE_RUNNING:
//                            ratio = plot.getIterations();
//                            frame.progressString("plotting...");
                            break;
                    
                        case ManifoldsRenderer.STATE_FINISHED:
//                            frame.progressString("ok. ");
//                            report();
                            return;

                        case ManifoldsRenderer.STATE_STOPPED:
//                            frame.progressString("stopped, ok.");
//                            report();
                            return;
                    
                        default:
//                            ratio = 0;
                    }
                }

                if ((state & ManifoldsRenderer.STATE_RUNNING) != 0 ) {
//                    report();
                    chartPanel.repaint();
                }
                
                try {
                    sleep(100);
                }
                catch (InterruptedException e2) {}
            }
        }

//        private void report() {
//            frame.progressCount(plot.getIndex());
//            if (ratio != 0) {
//                frame.progressPercent((plot.getIndex() * 100 / ratio));
//            }
//        }
    };

    private void launchThread() {

        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);

        Thread refreshJob = new RefreshThread();
        refreshJob.start();

        Thread plotJob = new PlotThread("DMCDUE - Manifoldes plotter", false);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }

    public void stopRendering() {
        plotRenderer.stop();
    }

    public void setAlpha(boolean flag) {
        plot.setAlpha(flag);
    }

    public void setAlphaValue(float f) {
        plot.setForegroundAlpha(f);
    }

    public float getAlphaValue() {
        return plot.getForegroundAlpha();
    }

    public void setCrosshair(boolean flag) {
        crosshair = flag;
        chartPanel.setHorizontalAxisTrace(flag);
        chartPanel.setVerticalAxisTrace(flag);
    }

    public boolean isCrosshair() {
        return crosshair;
    }

    public boolean isGridlines() {
        return gridlines;
    }

    public void setGridlines(boolean b) {
        gridlines = b;
    }

    public void clear() {
        plot.setNoData(true);
        plot.zoom(0.0);
        launchThread();
    }

    public byte getPlotType() {
        return plotType;
    }

    public void setPlotType(byte b) {
        switch(b) {
            case (ManifoldsRenderer.TYPE_UNSTABLE | ManifoldsRenderer.TYPE_STABLE):
                plotType |= b;
            break;

            case ManifoldsRenderer.TYPE_UNSTABLE:
                plotType &= ~ManifoldsRenderer.TYPE_STABLE;
                plotType |= ManifoldsRenderer.TYPE_UNSTABLE;
            break;
            
            case ManifoldsRenderer.TYPE_STABLE:
                plotType &= ~ManifoldsRenderer.TYPE_UNSTABLE;
                plotType |= ManifoldsRenderer.TYPE_STABLE;
            break;

            case (ManifoldsRenderer.TYPE_RIGHT | ManifoldsRenderer.TYPE_LEFT):
                plotType |= b;
            break;
            
            case ManifoldsRenderer.TYPE_RIGHT:
                plotType &= ~ManifoldsRenderer.TYPE_LEFT;
                plotType |= ManifoldsRenderer.TYPE_RIGHT;
            break;
            
            case ManifoldsRenderer.TYPE_LEFT:
                plotType &= ~ManifoldsRenderer.TYPE_RIGHT;
                plotType |= ManifoldsRenderer.TYPE_LEFT;
            break;
        }
    }
}
