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
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.chart.LyapunovRenderer;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.lyapunov.LyapunovControlForm2;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.ui.lyapunov.*;

public class LyapunovManager extends AbstractManager
implements AbstractManager.Crosshair,
AbstractManager.GridLines,
AbstractManager.ConnectDots,
AbstractManager.BigDots,
AbstractManager.AxesVisibility {
	
	private LyapunovComponent component;
    
    private Model model;
    //? seems that interferes with frame of the superclass // private ManagerListener frame;
    private LyapunovControlForm2 form;
    
    private LyapunovRenderer renderer;
    
    private byte type;
    
    private boolean gridlines;
    private boolean crosshair;
    private boolean connectWithDots;
    private boolean bigDots;
    
    public LyapunovManager(LyapunovComponent component) {
    	
        super((ManagerListener2) component);
        this.component = component;
        this.model = component.getModel();
        this.form = (LyapunovControlForm2) component.getControlForm();
               
        /* defaults */
        setGridlines(true);
        crosshair = false;
        
        this.type = LyapunovControlForm2.TYPE_VS_TIME;
        
        renderer = new LyapunovRenderer(plot, model, component);
        plot.setPlotRenderer(renderer);
        
        chart.setTitle(model.getName());
        setCrosshair(crosshair);
        
        chart.setLegend(new StandardLegend());
        
        // stop everything on resizing
        chartPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                LyapunovManager.this.stopRendering();
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
        plot.setDrawGridLines(gridlines);
        
        plot.setNoData(false);
        switch(type) {
            case LyapunovControlForm2.TYPE_VS_TIME:
                return renderVsTime();
                
            case LyapunovControlForm2.TYPE_VS_PAR:
                return renderVsParameter();
                
            case LyapunovControlForm2.TYPE_PAR_SPACE:
                return renderArea();
                
            default:
                throw new InternalError("invalid type");
        }
    }
    
    public boolean renderVsTime() {
        VariableDoubles parameters;
        VariableDoubles initial;
        Range timeRange, verRange;
        double stepSize;
        
        
        try {
            parameters = form.getParameters();
            //parameters = form.getParameterValues();
            initial = form.getInitialValues();
            timeRange = form.getTimeRange();
            verRange = form.getVerticalRange();
            
            stepSize=0;//needs to be initialized in any case..
            if (model instanceof ODE)
                stepSize =form.getStepSize();
        }
        catch (InvalidData e) {
            getFrame().showInvalidDataDialog(e.getMessage());
            return false;
        }
        
        if (!(model instanceof ODE)) {
            renderer.initializeVsTime(parameters, initial, timeRange);
        }
        else{
            renderer.initializeVsTime(parameters,initial,timeRange,stepSize);
        }
        renderer.setConnectWithLines(connectWithDots);
        renderer.setBigDots(bigDots);
        
        xAxis.setLabel("time");
        yAxis.setLabel("exponent(s)");
        plot.setDataRanges(timeRange, verRange);
        
        launchThread();
        return true;
    }
    
    public boolean renderVsParameter() {
        VariableDoubles parameters;
        VariableDoubles initial;
        String label;
        Range parRange, verRange;
        int iterations=0;//initialization is needed since compiler overcautious
        double timePeriod=0;
        double stepSize=0;
        
        try {
            parameters = form.getParameters();
            initial = form.getInitialValues();
            parRange = form.getFirstParameterRange();
            verRange = form.getVerticalRange();
            if (model instanceof ODE){
                timePeriod = form.getTimePeriod();
                stepSize=form.getStepSize();
            }
            else{
                iterations = form.getIterations();
            }
        }
        catch (InvalidData e) {
            getFrame().showInvalidDataDialog(e.getMessage());
            return false;
        }
        
        label = form.getLabelOnH();
        
        if (!(model instanceof ODE)){
            renderer.initializeVsParameter(parameters, initial, label, parRange, iterations);
        }
        else{
            renderer.initializeVsParameter(parameters,initial, label,parRange,timePeriod,stepSize);
        }
        renderer.setConnectWithLines(connectWithDots);
        renderer.setBigDots(bigDots);
        
        xAxis.setLabel(label);
        yAxis.setLabel("exponent(s)");
        plot.setDataRanges(parRange, verRange);
        
        launchThread();
        return true;
    }
    
    public boolean renderArea() {
        VariableDoubles parameters = null;
        VariableDoubles initial;
        String firstLabel, secondLabel;
        Range firstParRange, secondParRange;
        double epsilon;
        int iterations=0;
        double timePeriod=0;
        double stepSize=0;
        
        try {
            parameters = form.getParameters();
            initial = form.getInitialValues();
            firstParRange = form.getFirstParameterRange();
            secondParRange = form.getSecondParameterRange();
            epsilon = form.getEpsilon();
            if (model instanceof ODE){
                timePeriod = form.getTimePeriod();
                stepSize=form.getStepSize();
            }
            else{
                iterations = form.getIterations();
            }
        } catch (InvalidData e2) {
            getFrame().showInvalidDataDialog(e2.getMessage());
            return false;
        }
        
        firstLabel = form.getLabelOnH();
        secondLabel = form.getLabelOnV();
        
        if (model instanceof ODE){
            renderer.initializeParArea(initial, parameters, firstLabel, secondLabel, epsilon, timePeriod,stepSize);
        }
        else{
            renderer.initializeParArea(initial, parameters, firstLabel, secondLabel, epsilon, iterations);
        }
        plot.setDataRanges(firstParRange, secondParRange);
        
        launchThread();
        return true;
    }
    
    class RefreshThread extends Thread {
        
        int state = DmcPlotRenderer.STATE_NONE;
        int ratio;
        
        RefreshThread() {
            super("DMCDUE - LyapunovRefresh");
        }
        
        public void run() {
            
            while (true) {
                int newState = renderer.getState();
                
                if (newState != state) {
                    
                    state = newState;
                    
                    switch (newState) {
                        case DmcPlotRenderer.STATE_NONE:
                            ratio = 0;
                            break;
                            
                        case DmcPlotRenderer.STATE_RUNNING:
                            //                            frame.progressString("plotting...");
                            break;
                            
                        case DmcPlotRenderer.STATE_FINISHED:
                        case DmcPlotRenderer.STATE_STOPPED:
                            //                            frame.progressString("ok. ");
                            return;
                            
                        default:
                            ratio = 0;
                    }
                }
                
                if ((state & DmcPlotRenderer.STATE_RUNNING) != 0 ) {
                    chartPanel.repaint();
                }
                try {
                    sleep(250);
                }
                catch (InterruptedException e2) {}
            }
        }
    }
    
    private void launchThread() {
        
        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);
        
        Thread refreshJob = new RefreshThread();
        refreshJob.start();
        
        Thread plotJob = new PlotThread("DMCDUE - Lyapunov plotter", false);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }
    
    public void stopRendering() {
        renderer.stop();
    }
    
    public void clear() {
        plot.setNoData(true);
        plot.zoom(0.0);
        launchThread();
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
    
    public void setBigDots(boolean flag) {
        bigDots = flag;
    }
    
    public boolean isBigDots() {
        return bigDots;
    }
    
    public void setConnectDots(boolean flag) {
        connectWithDots = flag;
    }
    
    public boolean isConnectDots() {
        return connectWithDots;
    }
    
    public void setType(byte b) {
        type = b;
    }
    
    public byte getType() {
        return type;
    }
    
    public LyapunovControlForm2 getForm(){
        return form;
    }
    
    public LyapunovRenderer getRenderer(){
        return renderer;
    }
}
