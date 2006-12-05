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

import javax.swing.JComponent;

import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.AbsorbingAreaRenderer;
import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.absorbingArea.AbsorbingAreaComponent;
import org.tsho.dmc2.ui.absorbingArea.AbsorbingAreaControlForm2;

/**
 *
 * @author tsho
 */
public class AbsorbingAreaManager extends AbstractManager
implements AbstractManager.Crosshair,
AbstractManager.AxesVisibility{
    
    private final DifferentiableMap model;
    private final AbsorbingAreaControlForm2 form;
    
    private AbsorbingAreaRenderer plotRenderer;
    
    private boolean crosshair;
    
    private final int refreshSleepTime = 500;
    
    public AbsorbingAreaManager(final DifferentiableMap model,
    final AbsorbingAreaControlForm2 form,
    final ManagerListener2 frame) {
        
        super(frame);
        
        this.model = model;
        this.form = form;
        
        crosshair = false;
        
        chartPanel.setMouseZoomable(true, false);
        chartPanel.setPopupMenu(null);
        setCrosshair(crosshair);
        
        plotRenderer = new AbsorbingAreaRenderer(model, plot, (AbsorbingAreaComponent) frame);
        plot.setPlotRenderer(plotRenderer);
        
        plot.setDrawGridLines(false);
        chart.setTitle(model.getName());
        
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
            public void componentResized(final ComponentEvent e) {
                AbsorbingAreaManager.this.stopRendering();
            }
        });
    }
    
    public boolean doRendering() {
        try {
            plot.setNoData(false);
            
            String[] vars = model.getVariables().labelsArray();
            
            xAxis.setLabel(vars[0]);
            if (model.getNVar() > 1) {
                yAxis.setLabel(vars[1]);
            }
            else {
                yAxis.setLabel(vars[0]);
            }
            
            plotRenderer.initialize(
            VariableDoubles.toArray(form.getParameterValues()),
            form.getEpsilon(),
            form.getIterations(),
            form.getTransients());
            
            plot.setDataRanges(form.getXRange(), form.getYRange());
        }
        catch (InvalidData e) {
            errorMessage = e.getMessage();
            return false;
        }
        
        if (plot.getPlotRenderer() != plotRenderer) {
            plot.setPlotRenderer(plotRenderer);
        }
        
        launchThread();
        return true;
    }
    
    
    
    private void launchThread() {
        
        plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_NONE);
        
        Thread refreshJob = new RefreshThread();
        refreshJob.start();
        
        Thread plotJob = new PlotThread("DMCDUE - AbsorbingArea plotter", false);
        plotJob.setPriority(Thread.currentThread().getPriority() - 1);
        plotJob.start();
    }
    
    
    class RefreshThread extends Thread {
        
        int state = AbsorbingAreaRenderer.STATE_NONE;
        int ratio;
        
        RefreshThread() {
            super("DMCDUE - AbsorbingArea refresh");
        }
        
        public void run() {
            
            while (true) {
                int newState = plotRenderer.getState();
                
                if (newState != state) {
                    
                    state = newState;
                    
                    switch (newState) {
                        case AbsorbingAreaRenderer.STATE_NONE:
                            ratio = 0;
                            break;
                            
                        case AbsorbingAreaRenderer.STATE_RUNNING:
                            //                            frame.progressString("plotting...");
                            break;
                            
                        case AbsorbingAreaRenderer.STATE_FINISHED:
                            //                            frame.progressString("ok. ");
                            return;
                            
                        default:
                            ratio = 0;
                    }
                }
                
                if ((state & AbsorbingAreaRenderer.STATE_RUNNING) != 0 ) {
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
        //here should come whatever is associated with stop in this case.
        //note that this might depend on the current state
        
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
    
    public AbsorbingAreaRenderer getPlotRenderer(){
        return plotRenderer;
    }
    
    public void handleMouseClicked(int x, int y){
        plotRenderer.setMouseClicked(true);
        plotRenderer.setXClicked(x);
        plotRenderer.setYClicked(y);
        
    }
    
}
