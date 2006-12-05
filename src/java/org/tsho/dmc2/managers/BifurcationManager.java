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
import org.tsho.dmc2.core.chart.Bifurcation2DRenderer;
import org.tsho.dmc2.core.chart.BifurcationSimpleRenderer;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.bifurcation.BifurcationControlForm2;

public class BifurcationManager extends AbstractManager
implements AbstractManager.GridLines,
AbstractManager.Crosshair,
AbstractManager.Transparency,
AbstractManager.AxesVisibility {
    
    private final Model model;
    private final BifurcationControlForm2 form;
    
    private StandardLegend legend;
    
    private boolean gridlines;
    private boolean crosshair;
    private boolean fixedInitialPoint;
    
    private int odeStepFunction=4;
    
    
    public BifurcationManager(
    final Model model,
    final BifurcationControlForm2 form,
    final ManagerListener2 frame) {
        
        super(frame);
        
        this.model = model;
        this.form = form;
        
        /* defaults */
        setGridlines(true);
        crosshair = false;
        
        plot.setForegroundAlpha(0.2F);
        plot.setAntialias(false);
        
        setCrosshair(crosshair);
        
        legend = new StandardLegend();
        chart.setTitle(model.getName());
        chart.setLegend(legend);
        
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
        
        // stop everything on resizing
        chartPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                BifurcationManager.this.stopRendering();
            }
        });
    }
    
    public boolean doRendering(final boolean redraw) {
        
        switch(form.getType()) {
            case BifurcationControlForm2.TYPE_SINGLE:
                return renderSingle(redraw);
                
            case BifurcationControlForm2.TYPE_DOUBLE:
                return renderDouble(redraw);
                
            default:
                throw new Error("invalid type");
        }
    }
    
    public boolean renderSingle(boolean redraw) {
        VariableDoubles parameters;
        VariableDoubles initial;
        String parLabel, varLabel;
        Range parRange, verRange;
        int transients;
        int iterations=0;
        double time=0;
        double step=0;//to avoid problems with initialization
        double[] hyperplaneCoeff=null;
        
        try {
            initial = form.getInitialValues();
            parameters = form.getParameters();
            parRange = form.getFirstParameterRange();
            verRange = form.getVerticalRange();
            parLabel = form.getFirstParameterLabel();
            varLabel = form.getVerticalAxisLabel();
            
            if (model instanceof ODE){
                time = form.getTime();
                step = form.getStep();
                hyperplaneCoeff= form.getHyperplaneCoeff();
            }
            else{
                iterations = form.getIterations();
            }
            transients = form.getTransients();
        }
        catch (InvalidData e) {
            frame.showInvalidDataDialog(e.getMessage());
            return false;
        }
        
        plot.setNoData(false);
        plot.setDrawGridLines(gridlines);
        
        Stepper stepper = null;
        if (model instanceof SimpleMap) {
            stepper = Lua.newIterator2(model,
            		VariableDoubles.toArray(parameters),
            		VariableDoubles.toArray(initial));
        }
        else if (model instanceof ODE) {
            stepper = Lua.newODEStepper(model,
            		VariableDoubles.toArray(parameters),
            		VariableDoubles.toArray(initial),
            		step, odeStepFunction);
        }
        
        BifurcationSimpleRenderer renderer;
        renderer = new BifurcationSimpleRenderer(plot, stepper,model);
        plot.setPlotRenderer(renderer);
        
        if (model instanceof ODE){
            renderer.initialize(
            VariableDoubles.toArray(parameters),
            VariableDoubles.toArray(initial),
            VariableDoubles.indexOf(parameters, parLabel),
            VariableDoubles.indexOf(initial, varLabel),
            time,step,hyperplaneCoeff,
            transients, fixedInitialPoint);
        }
        else{
            renderer.initialize(
            VariableDoubles.toArray(parameters),
            VariableDoubles.toArray(initial),
            VariableDoubles.indexOf(parameters, parLabel),
            VariableDoubles.indexOf(initial, varLabel),
            iterations,
            transients, fixedInitialPoint);
        }
        
        xAxis.setLabel(parLabel);
        yAxis.setLabel(varLabel);
        
        if (!redraw) {
            plot.setDataRanges(parRange, verRange);
            plot.zoom(0.0); // reset zoom
        }
        
        launchThread();
        return true;
    }
    
    public boolean renderDouble(boolean redraw) {
        VariableDoubles parameters;
        VariableDoubles initial;
        String parLabel1, parLabel2;
        Range parRange1, parRange2;
        int transients, period;
        double epsilon, infinity;
        double time=0;
        double step=0;
        double[] hyperplaneCoeff=null;
        
        try {
            initial = form.getInitialValues();
            parameters = form.getParameters();
            
            parRange1 = form.getFirstParameterRange();
            parRange2 = form.getSecondParameterRange();
            
            parLabel1 = form.getFirstParameterLabel();
            parLabel2 = form.getSecondParameterLabel();
            
            if (model instanceof ODE){
                time = form.getTime();
                step = form.getStep();
                hyperplaneCoeff= form.getHyperplaneCoeff();
            }
            
            transients = form.getTransients();
            period = form.getPeriod();
            //            if (period > DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length) {
            //                throw new InvalidData("Period value too large.");
            //            }
            
            epsilon = form.getEpsilon();
            infinity = form.getInfinity();
        }
        catch (InvalidData e) {
            frame.showInvalidDataDialog(e.getMessage());
            return false;
        }
        
        Stepper stepper = null;
        if (model instanceof SimpleMap) {
            stepper = Lua.newIterator2(model,
            		VariableDoubles.toArray(parameters),
                    VariableDoubles.toArray(initial));
        }
        else if (model instanceof ODE) {
            stepper = Lua.newODEStepper(model,
            		VariableDoubles.toArray(parameters),
                    VariableDoubles.toArray(initial),
                    step, odeStepFunction);
        }
        
        Bifurcation2DRenderer renderer;
        renderer = new Bifurcation2DRenderer(plot, stepper, model);
        plot.setPlotRenderer(renderer);
        
        if (model instanceof ODE){
            renderer.initialize(
            VariableDoubles.toArray(parameters),
            VariableDoubles.toArray(initial),
            VariableDoubles.indexOf(parameters, parLabel1),
            VariableDoubles.indexOf(parameters, parLabel2),
            epsilon,
            infinity,
            time,
            step, 
            hyperplaneCoeff,
            transients,
            period);
        }
        else{
            renderer.initialize(
            VariableDoubles.toArray(parameters),
            VariableDoubles.toArray(initial),
            VariableDoubles.indexOf(parameters, parLabel1),
            VariableDoubles.indexOf(parameters, parLabel2),
            epsilon,
            infinity,
            transients,
            period);
        }
        
        xAxis.setLabel(parLabel1);
        yAxis.setLabel(parLabel2);
        
        if (!redraw) {
            plot.setDataRanges(parRange1, parRange2);
            
            // this is a bit of a hack
            plot.setDrawGridLines(false);
            plot.setNoData(true);
            chartPanel.drawChart();
            plot.zoom(0.0); // reset zoom
        }
        
        plot.setNoData(false);
        launchThread();
        
        return true;
    }
    
    class RefreshThread extends Thread {
        private static final int REFRESH_SLEEP_TIME_NORMAL = 250;
        
        int state = DmcPlotRenderer.STATE_NONE;
        
        RefreshThread() {
            super("DMCDUE - Bifurcation refresh");
        }
        
        public void run() {
            DmcPlotRenderer renderer = plot.getPlotRenderer();
            
            while (true) {
                int newState = renderer.getState();
                
                if (newState != state) {
                    
                    state = newState;
                    
                    switch (newState) {
                        case DmcPlotRenderer.STATE_NONE:
                            break;
                            
                        case DmcPlotRenderer.STATE_FINISHED:
                            return;
                            
                        case DmcPlotRenderer.STATE_STOPPED:
                            return;
                            
                        case DmcPlotRenderer.STATE_RUNNING:
                            frame.progressString("plotting...");
                            break;
                            
                        default:
                    }
                }
                
                chartPanel.repaint();
                
                try {
                    sleep(REFRESH_SLEEP_TIME_NORMAL);
                }
                catch (InterruptedException e2) {
                }
            }
        }
    };
    
    private void launchThread() {
        
        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);
        
        Thread refreshJob = new RefreshThread();
        refreshJob.start();
        
        Thread plotJob = new PlotThread("DMCDUE - Bifurcation plotter", false);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }
    
    public void stopRendering() {
        DmcPlotRenderer renderer = (DmcPlotRenderer) plot.getPlotRenderer();
        
        if (renderer != null) {
            renderer.stop();
        }
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
    
    public void setFixedInitialPoint(boolean flag) {
        fixedInitialPoint = flag;
    }
    
    public void setOdeStepFunction(int i){
        
    }
    
    public void clear() {
        plot.setNoData(true);
        plot.zoom(0.0);
        launchThread();
    }
}
