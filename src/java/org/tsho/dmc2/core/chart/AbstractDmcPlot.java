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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;


public abstract class AbstractDmcPlot 
            extends Plot
            implements ValueAxisPlot {

    /** The x data range. */
    protected Range xDataRange;

    /** The y data range. */
    protected Range yDataRange;

    /** The anchor value. */
    protected double anchorX;

    /** The anchor value. */
    protected double anchorY;

    /** The domain axis (used for the x-values). */
    protected ValueAxis domainAxis;

    /** The range axis (used for the y-values). */
    protected ValueAxis rangeAxis;

    public static final Stroke DEFAULT_GRIDLINE_STROKE 
        = new BasicStroke(0.5f,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0.0f, new float[] {2.0f, 2.0f}, 0.0f);

    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;

    public static final Stroke DEFAULT_STROKE = new BasicStroke();

    public static final Paint DEFAULT_PAINT = Color.red;

    protected Stroke gridStroke, stroke;
    protected Paint gridPaint, paint;

    private boolean alpha;
    private boolean plotAntialias;
    protected boolean drawGridlines;

    protected List coreStatusListeners;

    private boolean noData;

    protected AbstractDmcPlot(ValueAxis domainAxis, ValueAxis rangeAxis) {
        super();
        coreStatusListeners = new ArrayList();
        
        this.anchorX = 0.0;
        this.anchorY = 0.0;

        this.domainAxis = domainAxis;
        if (domainAxis != null) {
            domainAxis.setPlot(this);
            this.anchorX = domainAxis.getRange().getCentralValue();
        }

        this.rangeAxis = rangeAxis;
        if (rangeAxis != null) {
            rangeAxis.setPlot(this);
            this.anchorY = rangeAxis.getRange().getCentralValue();
        }

        this.paint = DEFAULT_PAINT;
        this.stroke = DEFAULT_STROKE;
        this.gridStroke = DEFAULT_GRIDLINE_STROKE;
        this.gridPaint = DEFAULT_GRIDLINE_PAINT;
        
        this.alpha = false;
        this.drawGridlines = true;
        plotAntialias = true;
    }

    /**
     * Returns the domain axis for the plot.  If the domain axis for this plot
     * is null, then the method will return the parent plot's domain axis (if
     * there is a parent plot).
     *
     * @return the domain axis.
     */
    public ValueAxis getDomainAxis() {

        return this.domainAxis;

    }

    /**
     * Returns the range axis for the plot.  If the range axis for this plot is
     * null, then the method will return the parent plot's range axis (if
     * there is a parent plot).
     *
     * @return the range axis.
     */
    public ValueAxis getRangeAxis() {

        return this.rangeAxis;

    }

    /**
     * Draws the fast scatter plot on a Java 2D graphics device (such as the screen or
     * a printer).
     *
     * @param g2  the graphics device.
     * @param plotArea   the area within which the plot (including axis labels) should be drawn.
     * @param info  collects chart drawing information (<code>null</code> permitted).
     */
    public void draw(Graphics2D g2, Rectangle2D plotArea, PlotState parentState, PlotRenderingInfo info) {
//        if (data == null)
//            return;

        // set up info collection...
        if (info != null) {
            info.setPlotArea(plotArea);
        }

        // adjust the drawing area for plot insets (if any)...
        Insets insets = getInsets();
        if (insets != null) {
            plotArea.setRect(plotArea.getX() + insets.left,
                             plotArea.getY() + insets.top,
                             plotArea.getWidth() - insets.left - insets.right,
                             plotArea.getHeight() - insets.top - insets.bottom);
        }

        AxisSpace space = new AxisSpace();
        space = this.domainAxis.reserveSpace(g2, this, plotArea, RectangleEdge.BOTTOM, space);
        space = this.rangeAxis.reserveSpace(g2, this, plotArea, RectangleEdge.LEFT, space);
        Rectangle2D dataArea = space.shrink(plotArea, null);

        if (info != null) {
            info.setDataArea(dataArea);
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);

        /* if automatic bounds... */
        if (!isNoData()) {
            if (this instanceof DmcRenderablePlot) {
                DmcPlotRenderer renderer;
                renderer = ((DmcRenderablePlot) this).getPlotRenderer();
                if (renderer != null) {
                    renderer.initialize();
                    if (renderer.getState() == DmcPlotRenderer.STATE_STOPPED) {
                        return;
                    }
                }
            }
        }

        AxisState domainAxisState = null, rangeAxisState = null;
        if (this.domainAxis != null) {
            double cursor;
            cursor = dataArea.getMaxY();
            domainAxisState = this.domainAxis.draw(
                    g2, cursor, plotArea, dataArea, RectangleEdge.BOTTOM, info);
            // cursor = info.getCursor();
        }
        if (this.rangeAxis != null) {
            double cursor;
            cursor = dataArea.getMinX();
            rangeAxisState = this.rangeAxis.draw(
                    g2, cursor, plotArea, dataArea, RectangleEdge.LEFT, info);
        }

        if (drawGridlines == true && domainAxisState != null && rangeAxisState != null) {
            drawGridlines(g2, dataArea, 
                    domainAxisState.getTicks(), rangeAxisState.getTicks());
        }
        
        Shape originalClip = g2.getClip();
        g2.clip(dataArea);

//        Composite originalComposite = g2.getComposite();
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//                                                   getForegroundAlpha()));
//        g2.setStroke(new BasicStroke(12.0F));

        if (isNoData()) {
            drawNoDataMessage(g2, plotArea);
        }
        else {
            drawPlot(g2, dataArea, info);
        }

        g2.setClip(originalClip);
        drawOutline(g2, dataArea);
    }

    public void drawPlot(Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info) {
        Object originalAntialiasHint =
                g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

        if (plotAntialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        Composite originalComposite = g2.getComposite();
        if (alpha == true) {
            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            getForegroundAlpha()));
        }

        /* if automatic bounds... */
        if (!isNoData()) {
            if (this instanceof DmcRenderablePlot) {
                DmcPlotRenderer renderer;
                renderer = ((DmcRenderablePlot) this).getPlotRenderer();
                if (renderer != null) {
                    renderer.initialize();
                }
            }
        }

        g2.setStroke(stroke);
        g2.setPaint(paint);
        render(g2, dataArea, info);   /** This is where the computations method is called.*/
        g2.setComposite(originalComposite);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            originalAntialiasHint);
    }

    /**
     * Draws a representation of the data within the dataArea region.
     * <P>
     * The <code>info</code> and <code>crosshairInfo</code> arguments may be <code>null</code>.
     *
     * @param g2  the graphics device.
     * @param dataArea  the region in which the data is to be drawn.
     * @param info  an optional object for collection dimension information.
     * @param crosshairInfo  an optional object for collecting crosshair info.
     */
    public abstract void render(
            Graphics2D g2, Rectangle2D dataArea, PlotRenderingInfo info);

//    public abstract void stopRendering();

    protected void drawGridlines(
            Graphics2D g2, Rectangle2D dataArea, 
            List domainTicks, List rangeTicks) {
        
        if ((gridStroke != null) && (gridPaint != null) && domainAxis != null) {
            Iterator iterator = domainTicks.iterator();
            while (iterator.hasNext()) {
                ValueTick tick = (ValueTick) iterator.next();
                drawDomainGridLine(g2, dataArea, tick.getValue());
            }
        }

        if ((gridStroke != null) && (gridPaint != null) && rangeAxis != null) {
            Iterator iterator = rangeTicks.iterator();
            while (iterator.hasNext()) {
                ValueTick tick = (ValueTick) iterator.next();
                drawRangeGridLine(g2, dataArea, tick.getValue());
            }
        }
    }

    private void drawDomainGridLine(Graphics2D g2,
                                    Rectangle2D dataArea,
                                    double value) {

        Range range = domainAxis.getRange();
        if (!range.contains(value)) {
            return;
        }

        Line2D line = null;
        double v = domainAxis.valueToJava2D(value, dataArea, RectangleEdge.BOTTOM);
        line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());

        g2.setPaint(gridPaint);
        g2.setStroke(gridStroke);
        g2.draw(line);

    }


    private void drawRangeGridLine(Graphics2D g2,
                                  Rectangle2D dataArea,
                                  double value) {

        Range range = rangeAxis.getRange();
        if (!range.contains(value)) {
            return;
        }

        Line2D line = null;
        double v = rangeAxis.valueToJava2D(value, dataArea, RectangleEdge.LEFT);
        line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);

        g2.setPaint(gridPaint);
        g2.setStroke(gridStroke);
        g2.draw(line);

    }



    /**
     * Returns the range of data values to be plotted along the axis.
     *
     * @param axis  the axis.
     *
     * @return  the range.
     */
    public Range getDataRange(ValueAxis axis) {

        Range result = null;
        if (axis == this.domainAxis) {
            result = this.xDataRange;
        }
        else if (axis == this.rangeAxis) {
            result = this.yDataRange;
        }
        return result;
    }

    public void setDataRanges(Range xRange, Range yRange) {
        xDataRange = xRange;
        yDataRange = yRange;

        this.anchorX = domainAxis.getRange().getCentralValue();
        this.anchorY = rangeAxis.getRange().getCentralValue();

        domainAxis.configure();
        rangeAxis.configure();
    }

    public void resetDataRanges() {
        xDataRange = null;
        yDataRange = null;
    }


    public void zoom(double percent) {

        if (percent > 0.0) {
            ValueAxis domainAxis = getDomainAxis();
            double range = domainAxis.getRange().getLength();
            double scaledRange = range * percent;
            domainAxis.setRange(this.anchorX - scaledRange / 2.0,
                                this.anchorX + scaledRange / 2.0);

            ValueAxis rangeAxis = getRangeAxis();
            range = rangeAxis.getRange().getLength();
            scaledRange = range * percent;
            rangeAxis.setRange(this.anchorY - scaledRange / 2.0,
                               this.anchorY + scaledRange / 2.0);
        }
        else {
            getRangeAxis().setAutoRange(true);
            getDomainAxis().setAutoRange(true);
        }
    }

    /**
     * Multiplies the range on the horizontal axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     */
    public void zoomHorizontalAxes(double factor) {
        domainAxis.resizeRange(factor);
    }

    /**
     * Zooms in on the horizontal axes.
     *
     * @param lowerPercent  the lower bound.
     * @param upperPercent  the upper bound.
     */
    public void zoomHorizontalAxes(double lowerPercent, double upperPercent) {
        domainAxis.zoomRange(lowerPercent, upperPercent);
    }

    /**
     * Multiplies the range on the vertical axis/axes by the specified factor.
     *
     * @param factor  the zoom factor.
     */
    public void zoomVerticalAxes(double factor) {
        rangeAxis.resizeRange(factor);
    }

    /**
     * Zooms in on the vertical axes.
     *
     * @param lowerPercent  the lower bound.
     * @param upperPercent  the upper bound.
     */
    public void zoomVerticalAxes(double lowerPercent, double upperPercent) {
        rangeAxis.zoomRange(lowerPercent, upperPercent);
    }

    /**
     * @return
     */
    public boolean isDrawGridLines() {
        return drawGridlines;
    }

    /**
     * @param b
     */
    public void setDrawGridLines(boolean b) {
        drawGridlines = b;
    }

    /**
     * @return
     */
    public Paint getGridPaint() {
        return gridPaint;
    }

    /**
     * @return
     */
    public Stroke getGridStroke() {
        return gridStroke;
    }

    /**
     * @param paint
     */
    public void setGridPaint(Paint paint) {
        gridPaint = paint;
    }

    /**
     * @param stroke
     */
    public void setGridStroke(Stroke stroke) {
        gridStroke = stroke;
    }

    /**
     * @return
     */
    public boolean isAlpha() {
        return alpha;
    }

    /**
     * @param b
     */
    public void setAlpha(boolean b) {
        alpha = b;
    }

    /**
     * Returns the paint used to plot data points.
     *
     * @return The paint.
     */
    public Paint getPaint() {
        return this.paint;
    }

    /**
     * Sets the color for the data points.
     *
     * @param paint  the paint.
     */
    public void setPaint(final Paint paint) {
        this.paint = paint;
    }

    /**
     * @return Returns the stroke.
     */
    public Stroke getStroke() {
        return stroke;
    }
    /**
     * @param stroke The stroke to set.
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
    /**
     * @return Returns the antialias.
     */
    public boolean isAntialias() {
        return plotAntialias;
    }
    /**
     * @param antialias The antialias to set.
     */
    public void setAntialias(boolean antialias) {
        this.plotAntialias = antialias;
    }

    /**
     * @return Returns the noData.
     */
    public boolean isNoData() {
        return noData;
    }

    /**
     * @param noData The noData to set.
     */
    public void setNoData(final boolean noData) {
        this.noData = noData;
    }

    public void addCoreStatusListener(final CoreStatusListener l) {
        coreStatusListeners.add(l);
    }

    public void removeCoreStatusListener(final CoreStatusListener l) {
        coreStatusListeners.remove(l);
    }

    public void notifyCoreStatusListeners(final CoreStatusEvent e) {
        Iterator iterator = coreStatusListeners.iterator();
        while (iterator.hasNext()) {
            CoreStatusListener listener = (CoreStatusListener) iterator.next();
            listener.sendCoreStatus(e);
        }
    }
}
