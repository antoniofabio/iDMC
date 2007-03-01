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

import java.awt.Paint;
import java.io.IOException;

import javax.swing.JComponent;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;

import org.tsho.dmc2.core.chart.DmcPlotRenderer;
import org.tsho.dmc2.core.chart.DmcRenderablePlot;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.ui.AbstractPlotComponent;

/**
 * This abstract class defines the methods needed by AbtractPlotFrame.
 *
 * Note that there is _no_ synchronization at all between different threads
 * so care is needed (...).
 */
public abstract class AbstractManager {

    // interfaces

    public interface GridLines {
        void setGridlines(boolean flag);
        boolean isGridlines();
    }

    public interface Crosshair {
        void setCrosshair(boolean flag);
        boolean isCrosshair();
    }

    public interface BigDots {
        void setBigDots(boolean flag);
        boolean isBigDots();
    }

    public interface ConnectDots {
        void setConnectDots(boolean flag);
        boolean isConnectDots();
    }

    public interface Transparency {
        void setAlpha(boolean flag);
        void setAlphaValue(float f);
        float getAlphaValue();
    }

    public interface AxesVisibility {
        void setXAxisVisible(boolean flag);
        void setYAxisVisible(boolean flag);
        boolean isXAxisVisible();
        boolean isYAxisVisible();
    }

    // common data

    protected JFreeChart chart;
    protected DmcRenderablePlot plot;
    protected DmcChartPanel chartPanel;
    private ManagerListener2 frame;

    protected String errorMessage;
    protected NumberAxis xAxis;
    protected NumberAxis yAxis;

    // Constructor
    protected AbstractManager(ManagerListener2 frame) {

        this.frame = frame;

        xAxis = new NumberAxis();
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setAutoRangeStickyZero(false);
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);

        yAxis = new NumberAxis();
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setAutoRangeStickyZero(false);
        yAxis.setLowerMargin(0);
        yAxis.setUpperMargin(0);

        plot = new DmcRenderablePlot(xAxis, yAxis);
        plot.setNoDataMessage("No data.");
        
        chart = new JFreeChart(plot);
        chart.setNotify(false);

        chartPanel = new DmcChartPanel(chart);
        //chartPanel.setDataset(frame.getDataset());
        chartPanel.setMouseZoomable(true, false);
        chartPanel.setPopupMenu(null);
        
        chartPanel.setStatusBarFrame((AbstractPlotComponent) frame);
    }

    // common methods

    public abstract void stopRendering();
    public abstract void clear();
    
    final public JComponent getChartPanel() {
        return chartPanel;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    protected void finalize() {
        System.out.println("finalizing: " + getClass());
    }

    // axes visibility

    public void setXAxisVisible(boolean flag) {
        xAxis.setVisible(flag);
    }

    public void setYAxisVisible(boolean flag) {
        yAxis.setVisible(flag);
    }

    public boolean isXAxisVisible() {
        return xAxis.isVisible();
    }

    public boolean isYAxisVisible() {
        return yAxis.isVisible();
    }
    
    // Colors

    public Paint getChartBackgroundColor() {
        return chart.getBackgroundPaint();
    }

    public void setChartBackgroundColor(Paint color) {
        chart.setBackgroundPaint(color);
    }

    public Paint getPlotBackgroundColor() {
        return plot.getBackgroundPaint();
    }

    public void setPlotBackgroundColor(Paint color) {
        plot.setBackgroundPaint(color);
    }
    
    //handles mouse clicks in case zooming is disabled
    //should be overriden in inherited classes when necessary
    public void handleMouseClicked(int x,int y){
        
    }
    
    // save image
    
    public void saveImageAs() throws IOException {
        chartPanel.doSaveAs();
    }
    
    // plot thread
    
    protected class PlotThread extends Thread {
        boolean plotOnly;

        PlotThread(String name, boolean p) {
            super(name);
            this.plotOnly = p;
        }

        public final void run() {
            
           
            
             try {
                getFrame().jobNotify(true);

                if (plotOnly) {
                    chartPanel.drawPlot();
                }
                else {
                    chartPanel.drawChart();
                }
            }
            catch (ModelException e) {
                if (e.getMessage() != null) {
                    getFrame().errorNotify(e.getMessage());
                }
                else {
                    getFrame().errorNotify("Model error.");
                }
            }
            catch (OutOfMemoryError e) {
                e.printStackTrace();
                getFrame().errorNotify("Out of Memory Error");
            }
            catch (RuntimeException re) {
                getFrame().errorNotify(re.getMessage());
            } catch (Throwable e) {
                e.printStackTrace();
                getFrame().errorNotify("Undefined error.");
            }
            finally {
                plot.getPlotRenderer().setState(DmcPlotRenderer.STATE_FINISHED);
                getFrame().jobNotify(false);
            }
             
        }
    };

    public ManagerListener2 getFrame() {
        return frame;
    }

}
