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

import org.jfree.chart.StandardLegend;
import org.jfree.data.Range;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.ODEStepper;
import org.tsho.dmc2.core.Stepper;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.chart.TrajectoryMultiRenderer;
import org.tsho.dmc2.core.chart.TrajectoryRenderer;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.trajectory.*;
import org.tsho.dmc2.core.util.*;

/**
 *
 * @author tsho
 */
public class TrajectoryManager extends AbstractManager 
                            implements AbstractManager.GridLines,
                                       AbstractManager.Crosshair,
                                       AbstractManager.Transparency,
                                       AbstractManager.ConnectDots,
                                       AbstractManager.BigDots,
                                       AbstractManager.AxesVisibility {

    private Model model;
    private TrajectoryComponent component;
    private TrajectoryControlForm2 form;

    private boolean gridlines;
    private boolean crosshair;
    private boolean bigDots;
    private boolean connectDots;
    
    private int odeStepFunction;

    private static final int REFRESH_SLEEP_TIME_NORMAL = 500;
    private static final int REFRESH_SLEEP_TIME_FAST = 25;
    private int refreshSleepTime = REFRESH_SLEEP_TIME_NORMAL;

    public TrajectoryManager(final TrajectoryComponent component,
            final TrajectoryControlForm2 form) {

        super(component);
        this.model = component.getModel();
        this.component = component;
        this.form = form;

        /* defaults */
        gridlines = true;
        crosshair = false;
        bigDots = false;

        chart.setLegend(new StandardLegend());

        chart.setTitle(model.getName());
        setCrosshair(crosshair);

        plot.setForegroundAlpha(0.1F);

        if (model instanceof SimpleMap) {
            plot.setAntialias(false);
            connectDots = false;
        }
        else if (model instanceof ODE) {
            plot.setAntialias(true);
            connectDots = true;
        }

        plot.addCoreStatusListener( new CoreStatusListener() {
            public void sendCoreStatus(final CoreStatusEvent event) {
                if (event.getType() == CoreStatusEvent.REDRAW) {
                    ((TrajectoryRenderer) plot.getPlotRenderer()).setContinua(false);
                    launchThread(false);
                }
                else if (event.getType() == CoreStatusEvent.REPAINT) {
                    chartPanel.repaint();
                }
            }
        });

        // stop everything on resizing
        // TODO fix
        chartPanel.addComponentListener(new ComponentAdapter() {
            public final void componentResized(final ComponentEvent e) {
                TrajectoryManager.this.stopRendering();
            }
        });
    }

    public final boolean doRendering(final boolean redraw) {

        TrajectoryRenderer renderer = null;

        VariableDoubles initialVal;
        VariableDoubles parameters;
        VariableDoubles deltaVar = null;
        VariableDoubles deltaPar = null;
        int variationCount = 0;
        double stepSize = 0;
        int transients;
        int iterations;
        int rangeIterations = 0;
        Range xRange = null, yRange = null;
        String xLabel;
        String yLabel;

        // collect user values

        try {
            parameters  = form.getParameterValues();
            initialVal = form.getInitialValues();

            if (form.isVariation()) {
                deltaVar = form.getDeltaVarValues();
                deltaPar = form.getDeltaParameters();
                variationCount = form.getVariationCount();
            }

            if (model instanceof ODE) {
                stepSize = form.getStepSize();
            }

            transients = form.getTransients();
            iterations = form.getIterations();

            if (form.isAutoRanges()) {
                rangeIterations = form.getRangeIterations();
            }
            else {
                if (!form.isTime()) {
                    xRange = form.getXRange();
                }
                yRange = form.getYRange();
            }

            xLabel = form.getLabelOnX();
            yLabel = form.getLabelOnY();
        }
        catch (InvalidData e) {
            errorMessage = e.getMessage();
            // frame.errorNotify(e.getMessage()); // this causes the sm to re-enter!
            return false;
        }

        // create renderer

        if (!form.isVariation()) {
            Stepper stepper = null;
            if (model instanceof SimpleMap) {
                stepper = Lua.newIterator(model, 
                		VariableDoubles.toArray(parameters), 
                		VariableDoubles.toArray(initialVal));
            }
            else if (model instanceof ODE) {
                stepper = Lua.newODEStepper(model,
                		VariableDoubles.toArray(parameters),
                		VariableDoubles.toArray(initialVal),
                		stepSize,
                		odeStepFunction);
            }

            renderer = new TrajectoryRenderer(plot, stepper, component);
            plot.setPlotRenderer(renderer);

            stepper.setAxes(
                    VariableDoubles.indexOf(initialVal, xLabel),
                    VariableDoubles.indexOf(initialVal, yLabel));
        }
        else {
            Stepper[] steppersList;
            steppersList = createStepperList(
                    model, variationCount,
                    initialVal, parameters,
                    deltaVar, deltaPar,
                    xLabel, yLabel);

            renderer = new TrajectoryMultiRenderer(plot, steppersList);
            plot.setPlotRenderer(renderer);
        }

        // set type-depending stuff
        if (!form.isTime()) {
            renderer.setTimePlot(false);
            xAxis.setLabel(xLabel);
            yAxis.setLabel(yLabel);
        }
        else {
            renderer.setTimePlot(true);
            xAxis.setLabel("time");
            yAxis.setLabel(yLabel);
            xLabel = yLabel;
        }

        // set ranges if not redrawing only

        if (!redraw) {
            if (form.isAutoRanges()) {
                renderer.setRangeIterations(rangeIterations);
                xAxis.setLowerMargin(0.05);
                xAxis.setUpperMargin(0.05);
                yAxis.setLowerMargin(0.05);
                yAxis.setUpperMargin(0.05);
                renderer.setComputeRanges(true);
            }
            else {
                xAxis.setLowerMargin(0);
                xAxis.setUpperMargin(0);
                yAxis.setLowerMargin(0);
                yAxis.setUpperMargin(0);
                if (form.isTime()) {
                    Range timeRange = new Range(transients, transients+iterations);
                    plot.setDataRanges(timeRange, yRange);
                }
                else {
                    plot.setDataRanges(xRange, yRange);
                }
                renderer.setComputeRanges(false);
            }
            plot.zoom(0.0); // reset zoom
        }
        else {
            renderer.setComputeRanges(false);
        }

        renderer.setTransients(transients);
        renderer.setIterations(iterations);
        renderer.setDelay(getFrame().getDelayValue());
        renderer.setBigDots(bigDots);
        renderer.setConnectWithLines(connectDots);
        renderer.setContinua(false);

        plot.setDrawGridLines(gridlines);
        plot.setNoData(false);

        launchThread(false);

        return true;
    }

    private static VariableDoubles updateValues(
            final int count, final VariableDoubles vals,
            final VariableDoubles deltas) {

        VariableDoubles.Iterator I, J;
        VariableDoubles newInitVals = new VariableDoubles(vals.labelsArray());
        I = vals.iterator();
        J = deltas.iterator();
        while (I.hasNext()) {
            double val, delta;
            val = I.nextValue();
            delta = J.nextValue();

            newInitVals.put(I.label(), val + (delta * count));
        }
        return newInitVals;
    }

    private static Stepper[] createStepperList(
            final Model model, final int count,
            final VariableDoubles initialVal, final VariableDoubles parameters,
            final VariableDoubles deltaVar, final VariableDoubles deltaPar,
            final String xLabel, String yLabel) {

        Stepper[] list = new Stepper[count];
        for (int i = 0; i < count; i++) {
            Stepper stepper;
            if (model instanceof SimpleMap) {
                stepper = Lua.newIterator(model,
                		VariableDoubles.toArray(
                                updateValues(i, parameters, deltaPar)),
                        VariableDoubles.toArray(
                                updateValues(i, initialVal, deltaVar))
                );
            }
            else if (model instanceof ODE) {
                stepper = Lua.newODEStepper(model,
                		VariableDoubles.toArray(
                                updateValues(i, parameters, deltaPar)),
                        VariableDoubles.toArray(
                                updateValues(i, initialVal, deltaVar)),
                                0.01,
                                4);
            }
            else {
                throw new Error();
            }

            stepper.setAxes(
                    VariableDoubles.indexOf(initialVal, xLabel),
                    VariableDoubles.indexOf(initialVal, yLabel));
            stepper.initialize();

            list[i] = stepper;
        }
        return list;
    }

    public final boolean continueRendering() {
        TrajectoryRenderer renderer = (TrajectoryRenderer) plot.getPlotRenderer();
        try {
            renderer.setIterations(form.getIterations());
        }
        catch (InvalidData e) {
            errorMessage = e.getMessage();
            return false;
        }

        renderer.setContinua(true);

        launchThread(true);

        return true;
    }

    class RefreshThread extends Thread {

        int state = DmcPlotRenderer.STATE_NONE;
        int ratio;

        RefreshThread() {
            super("DMCDUE - Trajectory refresh");
        }

        public void run() {
            TrajectoryRenderer renderer = (TrajectoryRenderer) plot.getPlotRenderer();
            while (true) {
                int newState = renderer.getState();

                if (newState != state) {

                    state = newState;

                    switch (newState) {
                        case DmcPlotRenderer.STATE_NONE:
                            ratio = 0;
                            break;

                        case DmcPlotRenderer.STATE_RANGES:
                            ratio = renderer.getRangeIterations();
                            getFrame().progressString("calculating ranges...");
                            break;

                        case DmcPlotRenderer.STATE_TRANSIENTS:
                            ratio = renderer.getTransients();
                            getFrame().progressString("calculating transients...");
                            break;

                        case DmcPlotRenderer.STATE_POINTS:
                            ratio = renderer.getIterations();
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

                if (((state & DmcPlotRenderer.STATE_TRANSIENTS)
                    | (state & DmcPlotRenderer.STATE_POINTS)
                    | (state & DmcPlotRenderer.STATE_RANGES)) != 0 ) {

                    report();
                    chartPanel.repaint();
                }
                
                try {
                    sleep(refreshSleepTime);
                }
                catch (InterruptedException e2) {
                }
            }
        }

        private void report() {
            TrajectoryRenderer renderer = (TrajectoryRenderer) plot.getPlotRenderer();
            getFrame().progressCount(renderer.getIndex());
            if (ratio != 0) {
                getFrame().progressPercent((renderer.getIndex() * 100 / ratio));
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

    public void stopRendering() {
        DmcPlotRenderer renderer = (DmcPlotRenderer) plot.getPlotRenderer();
        
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
        ((TrajectoryRenderer) plot.getPlotRenderer()).setConnectWithLines(b);
    }

    public boolean isBigDots() {
        return bigDots;
    }

    public void setBigDots(final boolean b) {
        bigDots = b;

        TrajectoryRenderer renderer = (TrajectoryRenderer) plot.getPlotRenderer();        
        if (renderer != null) {
            ((TrajectoryRenderer) plot.getPlotRenderer()).setBigDots(b);
        }
    }

    public void setDelay(final int delay) {
        TrajectoryRenderer renderer = (TrajectoryRenderer) plot.getPlotRenderer();
        
        if (renderer != null) {
            renderer.setDelay(delay);        
        }

        if (delay > 0) {
            refreshSleepTime = REFRESH_SLEEP_TIME_FAST;
        }
        else  {
            refreshSleepTime = REFRESH_SLEEP_TIME_NORMAL;
        }
    }

    public void setOdeStepFunction(int i) {
        odeStepFunction = i;
    }

}
