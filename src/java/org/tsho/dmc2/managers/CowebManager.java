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
import org.tsho.dmc2.core.ODEStepper;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.CowebRenderer;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.coweb.CowebControlForm2;

/**
 *
 * @author tsho
 */
public class CowebManager extends AbstractManager 
                            implements AbstractManager.GridLines,
                                       AbstractManager.Crosshair,
                                       AbstractManager.Transparency,
                                       AbstractManager.ConnectDots,
                                       AbstractManager.BigDots,
                                       AbstractManager.AxesVisibility {

    private Model model;
    private CowebControlForm2 form;

    private boolean gridlines;
    private boolean crosshair;
    private boolean bigDots;
    private boolean connectDots;

    private static final int REFRESH_SLEEP_TIME_NORMAL = 500;
    private static final int REFRESH_SLEEP_TIME_FAST = 25;
    private int refreshSleepTime = REFRESH_SLEEP_TIME_NORMAL;

    public CowebManager(final Model model,
            final CowebControlForm2 form,
            final ManagerListener2 frame) {

        super(frame);

        this.model = model;
        this.form = form;

        /* defaults */
        connectDots = true;
        gridlines = true;
        crosshair = false;
        bigDots = false;

        if (!(model instanceof SimpleMap) || model.getNVar() != 1) {
            throw new Error("only unidimentional maps allowed");
        }

        plot.setForegroundAlpha(0.1F);
        
        plot.setAntialias(true);
        setCrosshair(crosshair);
        chart.setTitle(model.getName());
 
        plot.addCoreStatusListener( new CoreStatusListener() {
            public void sendCoreStatus(final CoreStatusEvent event) {
                if (event.getType() == CoreStatusEvent.REDRAW) {
//                    ((CowebRenderer) plot.getPlotRenderer()).setContinua(false);
                    launchThread(false);
                }
                else if (event.getType() == CoreStatusEvent.REPAINT) {
                    chartPanel.repaint();
                }
            }
        });

//        chart = new JFreeChart(model.getName(), plot);
//
//        chartPanel = new DmcChartPanel(chart);
//        chartPanel.setMouseZoomable(true, false);
//        chartPanel.setPopupMenu(null);

        // stop everything on resizing
        // TODO fix
        chartPanel.addComponentListener(new ComponentAdapter() {
            public final void componentResized(final ComponentEvent e) {
                CowebManager.this.stopRendering();
            }
        });
    }

    public final boolean doRendering(final boolean redraw) {

        CowebRenderer renderer = null;

        int type;
        int power;
        VariableDoubles initialVal = null;
        VariableDoubles parameters;
        double stepSize = 0;
        int transients = 0;
        Range xRange = null, yRange = null;

        // collect user values

        type = form.getPlotType();

        try {
            parameters  = form.getParameterValues();

            if (type == CowebControlForm2.TYPE_COWEB) {
                initialVal = form.getInitialValues();
                transients = form.getTransients();
            }

            power = form.getPower(); 

            xRange = form.getXRange();
            yRange = form.getYRange();

        }
        catch (InvalidData e) {
            errorMessage = e.getMessage();
            return false;
        }

        // create renderer

        Stepper stepper = null;
        if (model instanceof SimpleMap) {
            stepper = Lua.newIterator(model,
            		VariableDoubles.toArray(parameters),
            		new double[model.getNVar()]);
        }
        else if (model instanceof ODE) {
            stepper = Lua.newODEStepper(model,
            		VariableDoubles.toArray(parameters),
            		new double[model.getNVar()],
            		stepSize, 4);
        }

        renderer = new CowebRenderer(plot, stepper);
        plot.setPlotRenderer(renderer);

        stepper.setAxes(0, 0);
        stepper.initialize();

        // set type-depending stuff

        if (type == CowebControlForm2.TYPE_SHIFTED) {
            renderer.setAnimate(false);
            refreshSleepTime = REFRESH_SLEEP_TIME_NORMAL;            
        }
        else if (type == CowebControlForm2.TYPE_COWEB) {
            renderer.setAnimate(true);
            refreshSleepTime = REFRESH_SLEEP_TIME_FAST;
        }
        else {
            throw new Error("Wrong plot type");
        }

        // set ranges if not redrawing only

        if (!redraw) {
            xAxis.setLowerMargin(0);
            xAxis.setUpperMargin(0);
            yAxis.setLowerMargin(0);
            yAxis.setUpperMargin(0);
            plot.setDataRanges(xRange, yRange);
            plot.zoom(0.0); // reset zoom
        }

        if (type == CowebControlForm2.TYPE_COWEB) {
            renderer.setInitialValue(VariableDoubles.toArray(initialVal));
            renderer.setTransients(transients);
        }
        else {
            renderer.setInitialValue(null);
            renderer.setTransients(0);
        }

        // labels

        String xLabel, yLabel;
        xLabel = model.getVarNames()[0];
        yLabel = "f^" + power + "(" + xLabel + ")";
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);
        
        renderer.setPower(power);
        renderer.setDelay(getFrame().getDelayValue());
        renderer.setBigDots(bigDots);
        renderer.setConnectWithLines(connectDots);

        plot.setDrawGridLines(gridlines);
        plot.setNoData(false);

        launchThread(false);

        return true;
    }

//    public final boolean continueRendering() {
//        TrajectoryRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
//        try {
//            renderer.setIterations(form.getIterations());
//        }
//        catch (InvalidData e) {
//            errorMessage = e.getMessage();
//            return false;
//        }
//
//        renderer.setContinua(true);
//
//        launchThread(true);
//
//        return true;
//    }

    class RefreshThread extends Thread {

        int state = DmcPlotRenderer.STATE_NONE;
        int ratio;

        RefreshThread() {
            super("DMCDUE - ScatterRefresh");
        }

        public void run() {
            CowebRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
            while (true) {
                int newState = renderer.getState();

                if (newState != state) {

                    state = newState;

                    switch (newState) {
                        case DmcPlotRenderer.STATE_NONE:
                            ratio = 0;
                            break;

                        case DmcPlotRenderer.STATE_ERROR:
                            ratio = 0;
                            getFrame().progressString("error...");
                            return;
        
                        case DmcPlotRenderer.STATE_TRANSIENTS:
                            ratio = renderer.getTransients();
                            getFrame().progressString("calculating transients...");
                            break;

                        case DmcPlotRenderer.STATE_POINTS:
//                            ratio = renderer.getIterations();
                            getFrame().progressString("plotting...");
                            break;
                    
                        case DmcPlotRenderer.STATE_FINISHED:
                            getFrame().progressString("ok. ");
                            report();
                            return;

                        case DmcPlotRenderer.STATE_STOPPED:
                            getFrame().progressString("stopped, ok.");
                            report();

                            return;

                        default:
                            ratio = 0;
                    }
                }

                if (((state & DmcPlotRenderer.STATE_RUNNING)
                    | (state & DmcPlotRenderer.STATE_POINTS)) != 0 ) {

                    report();
                    chartPanel.repaint();
                }
                
                try {
                    sleep(refreshSleepTime);
                }
                catch (InterruptedException e2) {}
            }
        }

        private void report() {
            CowebRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
//            frame.progressCount(renderer.getIndex());
            if (ratio != 0) {
//                frame.progressPercent((renderer.getIndex() * 100 / ratio));
            }
        }
    };

    private void launchThread(final boolean plotOnly) {

        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);

        Thread refreshJob = new RefreshThread();
        refreshJob.start();

        Thread plotJob = new PlotThread("DMCDUE - Trajectory plotter", plotOnly);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }


//    private void launch1Thread(final boolean plotOnly) {
//
//        Thread job = new Thread("ScatterManager thread") {
//            CowebRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
//            public final void run() {
//
//                RefreshThread refresh = new RefreshThread("ScatterRefresh");
//
//                renderer.setState(DmcPlotRenderer.STATE_NONE);
//                refresh.start();
//
//                try {
//                    frame.jobNotify(true);
//
//                    if (plotOnly) {
//                        chartPanel.drawPlot();
//                    }
//                    else {
//                        chartPanel.drawChart();
//                    }
//                }
//                catch (ModelException e) {
//                    renderer.setState(DmcPlotRenderer.STATE_ERROR);
//                    e.printStackTrace();
//                    if (e.getCause() != null) {
//                        e.getCause().printStackTrace();
//                    }
//                    if (e.getMessage() != null) {
//                        frame.showInvalidDataDialog(e.getMessage());
//                    }
//                    else {
//                        frame.showInvalidDataDialog("Model error.");
//                    }
//                    
//                }
//                catch (OutOfMemoryError e) {
//                    renderer.setState(DmcPlotRenderer.STATE_ERROR);
//                    e.printStackTrace();
//                    frame.showInvalidDataDialog("Out of Memory Error");
//                }
//                catch (Throwable e) {
//                    renderer.setState(DmcPlotRenderer.STATE_ERROR);
//                    e.printStackTrace();
//                    if (e.getMessage() != null) {
//                        frame.showInvalidDataDialog("Error - " + e.getMessage());
//                    }
//                    else {
//                        frame.showInvalidDataDialog("Undefined error.");
//                    }
//                }
//                finally {
//                    refresh.interrupt();
//                    frame.jobNotify(false);
//                }
//            }
//        };
//
//		// the GUI (er, the whole system...) gets unresponsive
//		// without this on Windows
//		job.setPriority(Thread.currentThread().getPriority() - 1);
//        job.start();
//    }



    public void stopRendering() {
        CowebRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
        
        if (renderer != null) {
            renderer.stop();
        }
    }

    public void clear() {
        plot.setNoData(true);
        plot.zoom(0.0);
        launchThread(false);
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

    public void setCrosshair(final boolean flag) {
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

    public void setGridlines(final boolean b) {
        gridlines = b;
    }

    public boolean isConnectDots() {
        return connectDots;
    }

    public void setConnectDots(final boolean b) {
        connectDots = b;
        ((CowebRenderer) plot.getPlotRenderer()).setConnectWithLines(b);
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public void setBigDots(final boolean b) {
        bigDots = b;
        ((CowebRenderer) plot.getPlotRenderer()).setBigDots(b);
    }

    public void setDelay(final int delay) {
        CowebRenderer renderer = (CowebRenderer) plot.getPlotRenderer();
        
        if (renderer != null) {
            ((CowebRenderer) plot.getPlotRenderer()).setDelay(delay);        
        }
        
        if (delay > 0) {
            refreshSleepTime = REFRESH_SLEEP_TIME_FAST;
        }
        else  {
            refreshSleepTime = REFRESH_SLEEP_TIME_NORMAL;
        }
    }

    public void setVariation(final boolean flag) {

    }
}
