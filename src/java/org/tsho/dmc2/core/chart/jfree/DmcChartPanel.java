/* ======================================
 * JFreeChart : a free Java chart library
 * ======================================
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * ---------------
 * ChartPanel.java
 * ---------------
 * (C) Copyright 2000-2003, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Andrzej Porebski;
 *                   S???ren Caspersen;
 *                   Jonathan Nash;
 *                   Hans-Jurgen Greiner;
 *                   Andreas Schneider;
 *                   Daniel van Enckevort;
 *                   David M O'Donnell;
 *                   Arnaud Lelievre;
 *
 * $Id: ChartPanel.java,v 1.14 2003/09/09 10:15:13 mungady Exp $
 *
 * adapted to dmcDue by Daniele Pizzoni <auouo@tin.it>
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>.
 *
 *
 */

/* adapted for dmcDue by by Daniele Pizzoni */

package org.tsho.dmc2.core.chart.jfree;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanelConstants;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueAxisPlot;

import org.jfree.chart.ui.ChartPropertyEditPanel;
import org.jfree.ui.ExtensionFileFilter;
import org.jfree.ui.RefineryUtilities;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.chart.AbstractDmcPlot;
import org.tsho.dmc2.managers.AbstractManager;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.core.util.*;

import com.keypoint.PngEncoder;

/**
 * A Swing GUI component for displaying a {@link JFreeChart}.
 * <P>
 * The panel registers with the chart to receive notification of changes to any component of the
 * chart.  The chart is redrawn automatically whenever this notification is received.
 *
 * @author David Gilbert
 */
public class DmcChartPanel extends JPanel implements ChartPanelConstants,
ActionListener,
MouseListener,
MouseMotionListener,
Printable {
    
    /** The chart that is displayed in the panel. */
    private JFreeChart chart;
    
    /** Storage for registered (chart) mouse listeners. */
    private List chartMouseListeners;
    
    /** A buffer for the rendered chart. */
    private Image chartBuffer;
    
    /**Dataset object associated with the chart*/
    private DataObject dataobject;
    
    /** The height of the chart buffer. */
    //    private int chartBufferHeight;
    
    /** The width of the chart buffer. */
    //    private int chartBufferWidth;
    
    private Rectangle2D bufferExtents;
    
    /** The popup menu for the frame. */
    private JPopupMenu popup;
    
    /** The drawing info collected the last time the chart was drawn. */
    protected ChartRenderingInfo info;
    
    /** The zoom rectangle (selected by the user with the mouse). */
    private Rectangle2D zoomRectangle = null;
    
    /** The zoom rectangle starting point (selected by the user with a mouse
     *  click)
     */
    private Point2D zoomPoint = null;
    
    /** Controls if the zoom rectangle is drawn as an outline or filled. */
    private boolean fillZoomRectangle = false;
    
    /** This flag controls whether or not horizontal zooming is enabled. */
    private boolean horizontalZoom = false;
    
    /** This flag controls whether or not vertical zooming is enabled. */
    private boolean verticalZoom = false;
    
    /** This flag controls controls whether or not horizontal tracing is enabled. */
    private boolean horizontalAxisTrace = false;
    
    /** This flag controls whether or not vertical tracing is enabled. */
    private boolean verticalAxisTrace = false;
    
    /** This flag controls whether or not zooming is enabled.*/
    private boolean zoomingEnabled = true;
    
    /** The flag which controls whether the drawing of the map is complete or not (enables crosshair)
     * If the corresponding manager does not set it to false,
     * plotting and crosshair are concurrent; if crosshair is to
     * be disabled during plotting, manager has to set it to false
     * as plotting begins, and set it to true when plotting is finished.
     */
    private boolean crosshairNotBlocked=true;
    private boolean disableCrosshairTillLeavesDisplay=false;
    
    /** Menu item for zooming in on a chart (both axes). */
    private JMenuItem zoomInBothMenuItem;
    
    /** Menu item for zooming in on a chart (horizontal axis). */
    private JMenuItem zoomInHorizontalMenuItem;
    
    /** Menu item for zooming in on a chart (vertical axis). */
    private JMenuItem zoomInVerticalMenuItem;
    
    /** Menu item for zooming out on a chart. */
    private JMenuItem zoomOutBothMenuItem;
    
    /** Menu item for zooming out on a chart (horizontal axis). */
    private JMenuItem zoomOutHorizontalMenuItem;
    
    /** Menu item for zooming out on a chart (vertical axis). */
    private JMenuItem zoomOutVerticalMenuItem;
    
    /** Menu item for resetting the zoom (both axes). */
    private JMenuItem autoRangeBothMenuItem;
    
    /** Menu item for resetting the zoom (horizontal axis only). */
    private JMenuItem autoRangeHorizontalMenuItem;
    
    /** Menu item for resetting the zoom (vertical axis only). */
    private JMenuItem autoRangeVerticalMenuItem;
    
    /** A vertical trace line. */
    private Line2D verticalTraceLine;
    
    /** A horizontal trace line. */
    private Line2D horizontalTraceLine;
    
    /** A flag that controls whether or not file extensions are enforced. */
    private boolean enforceFileExtensions;
    
    /** The resourceBundle for the localization. */
    static protected ResourceBundle localizationResources =
    ResourceBundle.getBundle("org.jfree.chart.LocalizationBundle");
    //? 5.8.2004 - for being able to output mouse coords to status bar
    private AbstractPlotComponent frame;
    //?
    private AbstractManager manager;
    
    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart  the chart.
     */
    public DmcChartPanel(JFreeChart chart) {
        this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT,
        true,  // properties
        true,  // save
        true,  // print
        true,  // zoom
        true   // tooltips
        );
    }
    
    
    /**
     * Constructs a JFreeChart panel.
     *
     * @param chart  the chart.
     * @param width  the preferred width of the panel.
     * @param height  the preferred height of the panel.
     * @param useBuffer  a flag that indicates whether to use the off-screen
     *                   buffer to improve performance (at the expense of memory).
     * @param properties  a flag indicating whether or not the chart property
     *                    editor should be available via the popup menu.
     * @param save  a flag indicating whether or not save options should be
     *              available via the popup menu.
     * @param print  a flag indicating whether or not the print option
     *               should be available via the popup menu.
     * @param zoom  a flag indicating whether or not zoom options should be added to the
     *              popup menu.
     * @param tooltips  a flag indicating whether or not tooltips should be enabled for the chart.
     */
    public DmcChartPanel(JFreeChart chart,
    int width,
    int height,
    boolean properties,
    boolean save,
    boolean print,
    boolean zoom,
    boolean tooltips) {
        
        this.chart = chart;
        this.chartMouseListeners = new java.util.ArrayList();
        this.info = new ChartRenderingInfo();
        
        // setPreferredSize(new Dimension(width, height));
        //        this.chart.addChangeListener(this);
        
        // set up popup menu...
        this.popup = null;
        if (properties || save || print || zoom) {
            popup = createPopupMenu(properties, save, print, zoom);
        }
        
        //        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        //        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setDisplayToolTips(tooltips);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        this.enforceFileExtensions = true;
    }
    
    /**
     * Returns the chart contained in the panel.
     *
     * @return The chart.
     */
    public JFreeChart getChart() {
        return chart;
    }
    
    /**
     * Sets the chart that is displayed in the panel.
     *
     * @param chart  The chart.
     */
    public void setChart(JFreeChart chart) {
        
        // stop listening for changes to the existing chart...
        //        if (this.chart != null) {
        //            this.chart.removeChangeListener(this);
        //            this.chart.removeProgressListener(this);
        //        }
        
        // add the new chart...
        this.chart = chart;
        //        this.chart.addChangeListener(this);
        //        this.chart.addProgressListener(this);
        //        if (this.useBuffer) {
        //            this.refreshBuffer = true;
        //        }
        Plot plot = chart.getPlot();
        ValueAxis horizontalAxis = getHorizontalValueAxis(plot);
        this.horizontalZoom = this.horizontalZoom && (horizontalAxis != null);
        ValueAxis verticalAxis = getVerticalValueAxis(plot);
        this.verticalZoom = this.verticalZoom && (verticalAxis != null);
        repaint();
        
    }
    
    
    /**
     * Returns the popup menu.
     *
     * @return the popup menu.
     */
    public JPopupMenu getPopupMenu() {
        return this.popup;
    }
    
    /**
     * Sets the popup menu for the panel.
     *
     * @param popup  the new popup menu.
     */
    public void setPopupMenu(JPopupMenu popup) {
        this.popup = popup;
    }
    
    /**
     * Returns the chart rendering info from the most recent chart redraw.
     *
     * @return the chart rendering info.
     */
    public ChartRenderingInfo getChartRenderingInfo() {
        return this.info;
    }
    
    /**
     * A flag that controls mouse-based zooming.
     *
     * @param flag  <code>true</code> enables zooming and rectangle fill on zoom.
     */
    public void setMouseZoomable(boolean flag) {
        setMouseZoomable(flag, true);
    }
    
    /**
     * Controls mouse zooming and how the zoom rectangle is displayed
     *
     * @param flag  <code>true</code> if zooming enabled
     * @param fillRectangle  <code>true</code> if zoom rectangle is filled,
     *                       false if rectangle is shown as outline only.
     */
    public void setMouseZoomable(boolean flag, boolean fillRectangle) {
        setHorizontalZoom(flag);
        setVerticalZoom(flag);
        setFillZoomRectangle(fillRectangle);
    }
    
    /**
     * A flag that controls mouse-based zooming on the horizontal axis.
     *
     * @param flag  <code>true</code> enables zooming on HorizontalValuePlots.
     */
    public void setHorizontalZoom(boolean flag) {
        Plot plot = this.chart.getPlot();
        ValueAxis axis = getHorizontalValueAxis(plot);
        this.horizontalZoom = flag && (axis != null);
    }
    
    /**
     * A flag that controls how the zoom rectangle is drawn.
     *
     * @param flag  <code>true</code> instructs to fill the rectangle on
     *              zoom, otherwise it will be outlined.
     */
    public void setFillZoomRectangle(boolean flag) {
        this.fillZoomRectangle = flag;
    }
    
    /**
     * A flag that controls mouse-based zooming on the vertical axis.
     *
     * @param flag  <code>true</code> enables zooming on VerticalValuePlots.
     */
    public void setVerticalZoom(boolean flag) {
        Plot plot = this.chart.getPlot();
        ValueAxis axis = getVerticalValueAxis(plot);
        this.verticalZoom = flag && (axis != null);
    }
    
    /**
     * A flag that controls trace lines on the horizontal axis.
     *
     * @param flag  <code>true</code> enables trace lines for the mouse
     *      pointer on the horizontal axis.
     */
    public void setHorizontalAxisTrace(boolean flag) {
        this.horizontalAxisTrace = flag;
    }
    
    /**
     * A flag that controls trace lines on the vertical axis.
     *
     * @param flag  <code>true</code> enables trace lines for the mouse
     *              pointer on the vertical axis.
     */
    public void setVerticalAxisTrace(boolean flag) {
        this.verticalAxisTrace = flag;
    }
    
    /**
     * Returns <code>true</code> if file extensions should be enforced, and <code>false</code>
     * otherwise.
     *
     * @return The flag.
     */
    public boolean isEnforceFileExtensions() {
        return this.enforceFileExtensions;
    }
    
    /**
     * Sets a flag that controls whether or not file extensions are enforced.
     *
     * @param enforce  the new flag value.
     */
    public void setEnforceFileExtensions(boolean enforce) {
        this.enforceFileExtensions = enforce;
    }
    
    /**
     * Switches chart tooltip generation on or off.
     *
     * @param flag  the flag.
     */
    public void setDisplayToolTips(boolean flag) {
        
        if (flag) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        else {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
        
    }
    
    /**
     * Returns a string for the tooltip.
     *
     * @param e  the mouse event.
     *
     * @return a tool tip or <code>null</code> if no tooltip is available.
     */
    public String getToolTipText(MouseEvent e) {
        
        String result = null;
        
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                Insets insets = getInsets();
                ChartEntity entity = entities.getEntity((int) (e.getX() - insets.left),
                (int) (e.getY() - insets.top));
                if (entity != null) {
                    result = entity.getToolTipText();
                }
            }
        }
        
        return result;
        
    }
    
    /**
     * Translates a Java2D point on the chart to a screen location.
     *
     * @param java2DPoint  the Java2D point.
     *
     * @return the screen location.
     */
    public Point translateJava2DToScreen(Point2D java2DPoint) {
        Insets insets = getInsets();
        int x = (int) (java2DPoint.getX() + insets.left);
        int y = (int) (java2DPoint.getY() + insets.top);
        return new Point(x, y);
    }
    
    /**
     * Translates a screen location to a Java2D point.
     *
     * @param screenPoint  the screen location.
     *
     * @return the Java2D coordinates.
     */
    public Point2D translateScreenToJava2D(Point screenPoint) {
        Insets insets = getInsets();
        double x = (screenPoint.getX() - insets.left);
        double y = (screenPoint.getY() - insets.top);
        return new Point2D.Double(x, y);
    }
    
    /**
     * Returns the chart entity at a given point.
     * <P>
     * This method will return null if there is (a) no entity at the given point, or
     * (b) no entity collection has been generated.
     *
     * @param viewX  the x-coordinate.
     * @param viewY  the y-coordinate.
     *
     * @return the chart entity (possibly null).
     */
    public ChartEntity getEntityForPoint(int viewX, int viewY) {
        
        ChartEntity result = null;
        if (this.info != null) {
            Insets insets = getInsets();
            double x = (viewX - insets.left);
            double y = (viewY - insets.top);
            EntityCollection entities = this.info.getEntityCollection();
            result = entities != null ? entities.getEntity(x, y) : null;
        }
        return result;
        
    }
    
    /**
     * Paints the component by drawing the chart to fill the entire component,
     * but allowing for the insets (which will be non-zero if a border has been
     * set for this component).  To increase performance (at the expense of
     * memory), an off-screen buffer image can be used.
     *
     * @param g  the graphics device for drawing on.
     */
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        Insets insets = getInsets();
        
        if (bufferExtents == null || !bufferExtents.equals(getChartArea())) {
            
            createBuffer();
            ((AbstractDmcPlot) chart.getPlot()).setNoData(true);
            
            Graphics2D bufferG2 = (Graphics2D) chartBuffer.getGraphics();
            chart.draw(bufferG2, bufferExtents, info);
        }
        
        g2.drawImage(chartBuffer, insets.left, insets.right, this);
        
        this.verticalTraceLine = null;
        this.horizontalTraceLine = null;
    }
    
    private Rectangle2D getChartArea() {
        Dimension size = getSize();
        Insets insets = getInsets();
        
        double width = size.getWidth() - insets.left - insets.right;
        double height = size.getHeight() - insets.top - insets.bottom;
        
        return new Rectangle2D.Double(0, 0, width, height);
    }
    
    // TODO fix a minimum size for the buffer
    private void createBuffer() {
        
        bufferExtents = getChartArea();
        chartBuffer = createImage((int) bufferExtents.getWidth(),
        (int) bufferExtents.getHeight());
    }
    
    public Image getBufferImage() {
        return chartBuffer;
    }
    
    public void setBufferImage(Image image) {
        bufferExtents = getChartArea();
        
        Insets insets = getInsets();
        chartBuffer.getGraphics().drawImage(image, insets.left, insets.right, this);
    }
    
    /**
     *  Draws the chart on the buffer
     */
    public void drawChart() {
        
        // disable zoom
        boolean hZoomSave = horizontalZoom;
        boolean vZoomSave = verticalZoom;
        horizontalZoom = false;
        verticalZoom = false;
        
        createBuffer();
        
        Graphics2D g2 = (Graphics2D) chartBuffer.getGraphics();
        
        chart.draw(g2, bufferExtents, this.info);
        repaint();
        
        horizontalZoom = hZoomSave;
        verticalZoom = vZoomSave;
    }
    
    /**
     * Redraws the plot only. Permits continuing of a scatter plot without
     * deleting the previous contents
     */
    public void drawPlot() {
        
        if (!(chart.getPlot() instanceof AbstractDmcPlot)) {
            throw new Error("not an AbstractDmcScatterPlot");
        }
        
        // disable zoom
        boolean hZoomSave = horizontalZoom;
        boolean vZoomSave = verticalZoom;
        horizontalZoom = false;
        verticalZoom = false;
        
        AbstractDmcPlot dmcPlot;
        dmcPlot = (AbstractDmcPlot) chart.getPlot();
        
        Rectangle2D plotArea = new Rectangle2D.Double();
        plotArea = getChartRenderingInfo().getPlotInfo().getDataArea();
        
        Graphics2D bufferG2 = (Graphics2D) chartBuffer.getGraphics();
        
        bufferG2.setClip(plotArea);
        dmcPlot.drawPlot(
        bufferG2, plotArea, getChartRenderingInfo().getPlotInfo());
        
        repaint();
        
        horizontalZoom = hZoomSave;
        verticalZoom = vZoomSave;
    }
    
    
    /**
     * Receives notification of changes to the chart, and redraws the chart.
     *
     * @param event  details of the chart change event.
     */
    //    public void chartChanged(ChartChangeEvent event) {
    //
    //        // I want no events
    //        throw new RuntimeException("DmcChartPanel got a ChartChangeEvent");
    ////        this.refreshBuffer = true;
    ////        repaint();
    //
    //    }
    
    /**
     * Receives notification of a chart progress event.
     *
     * @param event  the event.
     */
    //    public void chartProgress(ChartProgressEvent event) {
    //        // I want no events
    //        throw new RuntimeException("DmcChartPanel got a ChartProgressEvent");
    //    }
    
    /**
     * Handles action events generated by the popup menu.
     *
     * @param event  the event.
     */
    public void actionPerformed(ActionEvent event) {
        
        String command = event.getActionCommand();
        
        if (command.equals(PROPERTIES_ACTION_COMMAND)) {
            attemptEditChartProperties();
        }
        else if (command.equals(SAVE_ACTION_COMMAND)) {
            try {
                doSaveAs();
            }
            catch (IOException e) {
                System.err.println("ChartPanel.doSaveAs: i/o exception = " + e.getMessage());
            }
        }
        else if (command.equals(PRINT_ACTION_COMMAND)) {
            createChartPrintJob();
        }
        else if (command.equals(ZOOM_IN_BOTH_ACTION_COMMAND)) {
            zoomInBoth(this.zoomPoint.getX(), this.zoomPoint.getY());
        }
        else if (command.equals(ZOOM_IN_HORIZONTAL_ACTION_COMMAND)) {
            zoomInHorizontal(this.zoomPoint.getX());
        }
        else if (command.equals(ZOOM_IN_VERTICAL_ACTION_COMMAND)) {
            zoomInVertical(this.zoomPoint.getY());
        }
        else if (command.equals(ZOOM_OUT_BOTH_ACTION_COMMAND)) {
            zoomOutBoth(this.zoomPoint.getX(), this.zoomPoint.getY());
        }
        else if (command.equals(ZOOM_OUT_HORIZONTAL_ACTION_COMMAND)) {
            zoomOutHorizontal(this.zoomPoint.getX());
        }
        else if (command.equals(ZOOM_OUT_VERTICAL_ACTION_COMMAND)) {
            zoomOutVertical(this.zoomPoint.getY());
        }
        else if (command.equals(AUTO_RANGE_BOTH_ACTION_COMMAND)) {
            autoRangeBoth();
        }
        else if (command.equals(AUTO_RANGE_HORIZONTAL_ACTION_COMMAND)) {
            autoRangeHorizontal();
        }
        else if (command.equals(AUTO_RANGE_VERTICAL_ACTION_COMMAND)) {
            autoRangeVertical();
        }
        
    }
    
    /**
     * Handles a 'mouse entered' event.
     * <P>
     * This method does nothing, but is required for implementation of the MouseListener
     * interface.
     *
     * @param e  the mouse event.
     */
    public void mouseEntered(MouseEvent e) {
        // do nothing
    }
    
    /**
     * Handles a 'mouse exited' event.
     * <P>
     * This method does nothing, but is required for implementation of the MouseListener
     * interface.
     *
     * @param e  the mouse event.
     */
    public void mouseExited(MouseEvent e) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        Rectangle2D dataArea = getScaledDataArea();
        
        g2.setXORMode(java.awt.Color.orange);
        if (verticalTraceLine != null) {
            g2.draw(verticalTraceLine);
            verticalTraceLine = null;
        }
        if (horizontalTraceLine != null) {
            g2.draw(horizontalTraceLine);
            horizontalTraceLine = null;
        }
        
    }
    
    /**
     * Handles a 'mouse pressed' event.
     * <P>
     * This event is the popup trigger on Unix/Linux.  For Windows, the popup
     * trigger is the 'mouse released' event.
     *
     * @param e  The mouse event.
     */
    public void mousePressed(MouseEvent e) {
        
        if (zoomRectangle == null) {
            
            this.zoomPoint = RefineryUtilities.getPointInRectangle(e.getX(), e.getY(),
            getScaledDataArea());
            
            // check for popup trigger...
            if (e.isPopupTrigger()) {
                if (popup != null) {
                    displayPopupMenu(e.getX(), e.getY());
                }
            }
        }
        
    }
    
    /**
     * Handles a 'mouse released' event.
     * <P>
     * On Windows, we need to check if this is a popup trigger, but only if we
     * haven't already been tracking a zoom rectangle.
     *
     * @param e  Information about the event.
     */
    public void mouseReleased(MouseEvent e) {
        
        
        if (zoomRectangle != null) {
            
            //            if (Math.abs(e.getX() - zoomPoint.getX()) >= MINIMUM_DRAG_ZOOM_SIZE) {
            if (Math.abs(e.getX() - zoomPoint.getX()) >= 7) {
                if (e.getX() < zoomPoint.getX() || e.getY() < zoomPoint.getY()) {
                    autoRangeBoth();
                }
                else {
                    double x, y, w, h;
                    Rectangle2D scaledDataArea = getScaledDataArea();
                    //for a mouseReleased event, (horizontalZoom || verticalZoom)
                    //will be true, so we can just test for either being false;
                    //otherwise both are true
                    if (!verticalZoom) {
                        x = zoomPoint.getX();
                        y = scaledDataArea.getMinY();
                        w = Math.min(zoomRectangle.getWidth(),
                        scaledDataArea.getMaxX() - zoomPoint.getX());
                        h = scaledDataArea.getHeight();
                    }
                    else if (!horizontalZoom) {
                        x = scaledDataArea.getMinX();
                        y = zoomPoint.getY();
                        w = scaledDataArea.getWidth();
                        h = Math.min(zoomRectangle.getHeight(),
                        scaledDataArea.getMaxY() - zoomPoint.getY());
                    }
                    else {
                        x = zoomPoint.getX();
                        y = zoomPoint.getY();
                        w = Math.min(zoomRectangle.getWidth(),
                        scaledDataArea.getMaxX() - zoomPoint.getX());
                        h = Math.min(zoomRectangle.getHeight(),
                        scaledDataArea.getMaxY() - zoomPoint.getY());
                    }
                    Rectangle2D zoomArea = new Rectangle2D.Double(x, y, w, h);
                    zoom(zoomArea);
                    
                }
                this.zoomPoint = null;
                this.zoomRectangle = null;
            }
            else {
                Graphics2D g2 = (Graphics2D) getGraphics();
                g2.setXORMode(java.awt.Color.gray);
                if (fillZoomRectangle) {
                    g2.fill(zoomRectangle);
                }
                else {
                    g2.draw(zoomRectangle);
                }
                g2.dispose();
                this.zoomRectangle = null;
            }
            
            // notify a redraw event
            CoreStatusEvent ev = new CoreStatusEvent(this);
            ev.setType(CoreStatusEvent.REDRAW);
            ((AbstractDmcPlot) chart.getPlot()).notifyCoreStatusListeners(ev);
        }
        
        else if (e.isPopupTrigger()) {
            if (popup != null) {
                displayPopupMenu(e.getX(), e.getY());
            }
        }
    }
    
    /**
     * Receives notification of mouse clicks on the panel. These are
     * translated and passed on to any registered chart mouse click listeners.
     *
     * @param event  Information about the mouse event.
     */
    public void mouseClicked(MouseEvent event) {
        
        Insets insets = getInsets();
        int x = event.getX() - insets.left;
        int y = event.getY() - insets.top;
        
        //"Custom" mouse click handling code. One may use it for interactive work with the plots
        //(or follow the original framework). 
        //Manager should be set with setManager(), and handleMouseClicked(), which
        //is an empty function of AbstractManager, should be overriden in the AbstractManager subclass.
        if (manager!=null)
            manager.handleMouseClicked(x,y);
        
        // old 'handle click' code...
        chart.handleClick(x, y, this.info);
        
        // new entity code...
        if (this.chartMouseListeners.isEmpty()) {
            return;
        }
        
        ChartEntity entity = null;
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
        ChartMouseEvent chartEvent = new ChartMouseEvent(getChart(), event, entity);
        
        Iterator iterator = chartMouseListeners.iterator();
        while (iterator.hasNext()) {
            ChartMouseListener listener = (ChartMouseListener) iterator.next();
            listener.chartMouseClicked(chartEvent);
        }
    }
    
    /**
     * Implementation of the MouseMotionListener's method
     *
     * @param e  the event.
     */
    public void mouseMoved(MouseEvent e) {
        
        
        //crosshair
        if (this.horizontalAxisTrace && this.verticalAxisTrace && this.crosshairNotBlocked) {
            Rectangle2D rect=this.getScaledDataArea();
            double ux=userX(e);
            double uy=userY(e);
      
            if (!(rect.contains(e.getX(),e.getY()) && disableCrosshairTillLeavesDisplay )){
                if (disableCrosshairTillLeavesDisplay){
                    disableCrosshairTillLeavesDisplay=false;
                    drawHorizontalAxisTrace(e.getX());
                    drawVerticalAxisTrace(e.getY());
                }
                else{
                    drawHorizontalAxisTrace(e.getX());
                    drawVerticalAxisTrace(e.getY());
                    if (rect.contains(e.getX(),e.getY())){
                        float uxf=(float) ux;
                        float uyf=(float) uy;
                        this.writeToStatusBar("( "+uxf+" , "+uyf+" )");
                        
                    }
                    else{
                        this.writeToStatusBar("");
                    }
                }
            }
            
        }
        
        if (this.chartMouseListeners.isEmpty()) {
            return;
        }
        
        Insets insets = getInsets();
        int x = e.getX() - insets.left;
        int y = e.getY() - insets.top;
        
        ChartEntity entity = null;
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
        
        ChartMouseEvent event = new ChartMouseEvent(getChart(), e, entity);
        
        Iterator iterator = chartMouseListeners.iterator();
        while (iterator.hasNext()) {
            ChartMouseListener listener = (ChartMouseListener) iterator.next();
            listener.chartMouseMoved(event);
        }
    }
    
    /**
     * Handles a 'mouse dragged' event.
     *
     * @param e  the mouse event.
     */
    public void mouseDragged(MouseEvent e) {
        
        if (this.zoomingEnabled){
            // if the popup menu has already been triggered, then ignore dragging...
            if (popup != null && popup.isShowing()) {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) getGraphics();
            
            // use XOR to erase the previous zoom rectangle (if any)...
            g2.setXORMode(java.awt.Color.gray);
            if (zoomRectangle != null) {
                if (fillZoomRectangle) {
                    g2.fill(zoomRectangle);
                }
                else {
                    g2.draw(zoomRectangle);
                }
            }
            
            Rectangle2D scaledDataArea = getScaledDataArea();
            if (this.horizontalZoom && this.verticalZoom) {
                // selected rectangle shouldn't extend outside the data area...
                double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
                double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
                zoomRectangle = new Rectangle2D.Double(zoomPoint.getX(), zoomPoint.getY(),
                xmax - zoomPoint.getX(),
                ymax - zoomPoint.getY());
            }
            else if (this.horizontalZoom) {
                double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
                zoomRectangle = new Rectangle2D.Double(zoomPoint.getX(), scaledDataArea.getMinY(),
                xmax - zoomPoint.getX(),
                scaledDataArea.getHeight());
            }
            else if (this.verticalZoom) {
                double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
                zoomRectangle = new Rectangle2D.Double(scaledDataArea.getMinX(), zoomPoint.getY(),
                scaledDataArea.getWidth(),
                ymax - zoomPoint.getY());
            }
            
            if (zoomRectangle != null) {
                // use XOR to draw the new zoom rectangle...
                if (fillZoomRectangle) {
                    g2.fill(zoomRectangle);
                }
                else {
                    g2.draw(zoomRectangle);
                }
            }
            g2.dispose();
        }   
    }
    
    /**
     * Zooms in on an anchor point (measured in Java2D coordinates).
     *
     * @param x  The x value.
     * @param y  The y value.
     */
    public void zoomInBoth(double x, double y) {
        
        zoomInHorizontal(x);
        zoomInVertical(y);
        
    }
    
    /**
     * Returns a reference to the 'horizontal' value axis, if there is one.
     *
     * @param plot  the plot.
     *
     * @return The axis.
     */
    
    // TODO remove if not useful
    private ValueAxis getHorizontalValueAxis(Plot plot) {
        
        if (plot == null) {
            return null;
        }
        
        ValueAxis axis = null;
        
        if (plot instanceof CategoryPlot) {
            CategoryPlot cp = (CategoryPlot) plot;
            if (cp.getOrientation() == PlotOrientation.HORIZONTAL) {
                axis = cp.getRangeAxis();
            }
        }
        
        //        if (plot instanceof DmcTrajectoryScatterPlot) {
        //            DmcTrajectoryScatterPlot dtsp = (DmcTrajectoryScatterPlot) plot;
        //                axis = dtsp.getDomainAxis();
        //        }
        
        if (plot instanceof AbstractDmcPlot) {
            AbstractDmcPlot adsp = (AbstractDmcPlot) plot;
            axis = adsp.getDomainAxis();
        }
        
        return axis;
    }
    
    /**
     * Returns a reference to the 'vertical' value axis, if there is one.
     *
     * @param plot  the plot.
     *
     * @return The axis.
     */
    private ValueAxis getVerticalValueAxis(Plot plot) {
        
        if (plot == null) {
            return null;
        }
        
        ValueAxis axis = null;
        
        if (plot instanceof CategoryPlot) {
            CategoryPlot cp = (CategoryPlot) plot;
            if (cp.getOrientation() == PlotOrientation.VERTICAL) {
                axis = cp.getRangeAxis();
            }
        }
        
        //        if (plot instanceof DmcTrajectoryScatterPlot) {
        //            DmcTrajectoryScatterPlot dtsp = (DmcTrajectoryScatterPlot) plot;
        //                axis = dtsp.getRangeAxis();
        //        }
        
        if (plot instanceof AbstractDmcPlot) {
            AbstractDmcPlot xyp = (AbstractDmcPlot) plot;
            axis = xyp.getRangeAxis();
        }
        
        return axis;
        
    }
    
    /**
     * Decreases the range on the horizontal axis, centered about a Java2D
     * x coordinate.
     * <P>
     * The range on the x axis is halved.
     *
     * @param x  The x coordinate in Java2D space.
     */
    public void zoomInHorizontal(double x) {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomHorizontalAxes(0.5);
        }
    }
    
    /**
     * Decreases the range on the vertical axis, centered about a Java2D
     * y coordinate.
     * <P>
     * The range on the y axis is halved.
     *
     * @param y  The y coordinate in Java2D space.
     */
    public void zoomInVertical(double y) {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomVerticalAxes(0.5);
        }
    }
    
    /**
     * Zooms out on an anchor point (measured in Java2D coordinates).
     *
     * @param x  The x value.
     * @param y  The y value.
     */
    public void zoomOutBoth(double x, double y) {
        
        zoomOutHorizontal(x);
        zoomOutVertical(y);
        
    }
    
    /**
     * Increases the range on the horizontal axis, centered about a Java2D
     * x coordinate.
     * <P>
     * The range on the x axis is doubled.
     *
     * @param x  The x coordinate in Java2D space.
     */
    public void zoomOutHorizontal(double x) {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomHorizontalAxes(2.0);
        }
    }
    
    /**
     * Increases the range on the vertical axis, centered about a Java2D y coordinate.
     * <P>
     * The range on the y axis is doubled.
     *
     * @param y  the y coordinate in Java2D space.
     */
    public void zoomOutVertical(double y) {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomVerticalAxes(2.0);
        }
    }
    
    /**
     * Zooms in on a selected region.
     *
     * @param selection  the selected region.
     */
    public void zoom(Rectangle2D selection) {
        
        double hLower = 0.0;
        double hUpper = 0.0;
        double vLower = 0.0;
        double vUpper = 0.0;
        
        if ((selection.getHeight() > 0) && (selection.getWidth() > 0)) {
            
            Rectangle2D scaledDataArea = getScaledDataArea();
            hLower = (selection.getMinX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
            hUpper = (selection.getMaxX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
            vLower = (scaledDataArea.getMaxY() - selection.getMaxY()) / scaledDataArea.getHeight();
            vUpper = (scaledDataArea.getMaxY() - selection.getMinY()) / scaledDataArea.getHeight();
            
            Plot p = chart.getPlot();
            if (p instanceof ValueAxisPlot) {
                ValueAxisPlot plot = (ValueAxisPlot) p;
                plot.zoomHorizontalAxes(hLower, hUpper);
                plot.zoomVerticalAxes(vLower, vUpper);
            }
            
            
        }
        
    }
    
    /**
     * Restores the auto-range calculation on both axes.
     */
    public void autoRangeBoth() {
        autoRangeHorizontal();
        autoRangeVertical();
    }
    
    /**
     * Restores the auto-range calculation on the horizontal axis.
     */
    public void autoRangeHorizontal() {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomHorizontalAxes(0.0);
        }
    }
    
    /**
     * Restores the auto-range calculation on the vertical axis.
     */
    public void autoRangeVertical() {
        Plot p = chart.getPlot();
        if (p instanceof ValueAxisPlot) {
            ValueAxisPlot plot = (ValueAxisPlot) p;
            plot.zoomVerticalAxes(0.0);
        }
    }
    
    /**
     * Returns the data area for the chart (the area inside the axes) with the
     * current scaling applied.
     *
     * @return the scaled data area.
     */
    public Rectangle2D getScaledDataArea() {
        Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
        Insets insets = getInsets();
        //        double x = dataArea.getX() * scaleX + insets.left;
        //        double y = dataArea.getY() * scaleY + insets.top;
        //        double w = dataArea.getWidth() * scaleX;
        //        double h = dataArea.getHeight() * scaleY;
        //        return new Rectangle2D.Double(x, y, w, h);
        
        double x = dataArea.getX() + insets.left;
        double y = dataArea.getY() + insets.top;
        double w = dataArea.getWidth();
        double h = dataArea.getHeight();
        return new Rectangle2D.Double(x, y, w, h);
    }
    
    /**
     * Draws a vertical line used to trace the mouse position to the horizontal axis.
     *
     * @param x  the x-coordinate of the trace line.
     */
    private void drawHorizontalAxisTrace(int x) {
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        Rectangle2D dataArea = getScaledDataArea();
        
        g2.setXORMode(java.awt.Color.orange);
        if (((int) dataArea.getMinX() < x) && (x < (int) dataArea.getMaxX())) {
            
            if (verticalTraceLine != null) {
                g2.draw(verticalTraceLine);
                verticalTraceLine.setLine(x, (int) dataArea.getMinY(),
                x, (int) dataArea.getMaxY());
            }
            else {
                verticalTraceLine = new Line2D.Float(x, (int) dataArea.getMinY(),
                x, (int) dataArea.getMaxY());
            }
            g2.draw(verticalTraceLine);
        }
        else {
            if (horizontalTraceLine != null) {
                g2.draw(horizontalTraceLine);
                horizontalTraceLine = null;
            }
            if (verticalTraceLine != null) {
                g2.draw(verticalTraceLine);
                verticalTraceLine = null;
            }
            
        }
    }
    
    /**
     * Draws a horizontal line used to trace the mouse position to the vertical axis.
     *
     * @param y  the y-coordinate of the trace line.
     */
    private void drawVerticalAxisTrace(int y) {
        
        Graphics2D g2 = (Graphics2D) getGraphics();
        Rectangle2D dataArea = getScaledDataArea();
        
        g2.setXORMode(java.awt.Color.orange);
        if (((int) dataArea.getMinY() < y) && (y < (int) dataArea.getMaxY())) {
            
            if (horizontalTraceLine != null) {
                g2.draw(horizontalTraceLine);
                horizontalTraceLine.setLine((int) dataArea.getMinX(), y,
                (int) dataArea.getMaxX(), y);
            }
            else {
                horizontalTraceLine = new Line2D.Float((int) dataArea.getMinX(), y,
                (int) dataArea.getMaxX(), y);
            }
            g2.draw(horizontalTraceLine);
        }
        else {
            if (verticalTraceLine != null) {
                g2.draw(verticalTraceLine);
                verticalTraceLine = null;
            }
            if (horizontalTraceLine != null) {
                g2.draw(horizontalTraceLine);
                horizontalTraceLine = null;
            }
            
        }
    }
    
    /**
     * Displays a dialog that allows the user to edit the properties for the
     * current chart.
     */
    private void attemptEditChartProperties() {
        
        ChartPropertyEditPanel panel = new ChartPropertyEditPanel(chart);
        int result =
        JOptionPane.showConfirmDialog(this, panel,
        localizationResources.getString("Chart_Properties"),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            panel.updateChartProperties(chart);
        }
    }
    
    /**
     * Opens a file chooser and gives the user an opportunity to save the chart
     * in PNG format.
     *
     * @throws IOException if there is an I/O error.
     */
    public void doSaveAs() throws IOException {
        
        JFileChooser fileChooser = new JFileChooser();
        ExtensionFileFilter filter =
        new ExtensionFileFilter(localizationResources.getString("PNG_Image_Files"), ".png");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.addChoosableFileFilter(new ExtensionFileFilter("All files", ""));
        
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            if (isEnforceFileExtensions()) {
                if (!filename.endsWith(".png")) {
                    filename = filename + ".png";
                }
            }
            
            OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));
            PngEncoder encoder = new PngEncoder(chartBuffer, true, 0, 9);
            out.write(encoder.pngEncode());
            out.close();
        }
    }
   
    /**
     * Creates a print job for the chart.
     */
    public void createChartPrintJob() {
        
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        PageFormat pf2 = job.pageDialog(pf);
        if (pf2 != pf) {
            job.setPrintable(this, pf2);
            if (job.printDialog()) {
                try {
                    job.print();
                }
                catch (PrinterException e) {
                    JOptionPane.showMessageDialog(this, e);
                }
            }
        }
        
    }
    
    /**
     * Prints the chart on a single page.
     *
     * @param g  the graphics context.
     * @param pf  the page format to use.
     * @param pageIndex  the index of the page. If not <code>0</code>, nothing gets print.
     *
     * @return the result of printing.
     */
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        
        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        chart.draw(g2, new Rectangle2D.Double(x, y, w, h), null);
        return PAGE_EXISTS;
        
    }
    
    /**
     * Adds a listener to the list of objects listening for chart mouse events.
     *
     * @param listener  the listener.
     */
    public void addChartMouseListener(ChartMouseListener listener) {
        this.chartMouseListeners.add(listener);
    }
    
    /**
     * Removes a listener from the list of objects listening for chart mouse events.
     *
     * @param listener  the listener.
     */
    public void removeChartMouseListener(ChartMouseListener listener) {
        this.chartMouseListeners.remove(listener);
    }
    
    /**
     * Creates a popup menu for the panel.
     *
     * @param properties  include a menu item for the chart property editor.
     * @param save  include a menu item for saving the chart.
     * @param print  include a menu item for printing the chart.
     * @param zoom  include menu items for zooming.
     *
     * @return The popup menu.
     */
    protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print,
    boolean zoom) {
        
        JPopupMenu result = new JPopupMenu("Chart:");
        boolean separator = false;
        
        if (properties) {
            JMenuItem propertiesItem =
            new JMenuItem(localizationResources.getString("Properties..."));
            propertiesItem.setActionCommand(PROPERTIES_ACTION_COMMAND);
            propertiesItem.addActionListener(this);
            result.add(propertiesItem);
            separator = true;
        }
        
        if (save) {
            if (separator) {
                result.addSeparator();
                separator = false;
            }
            JMenuItem saveItem = new JMenuItem(localizationResources.getString("Save_as..."));
            saveItem.setActionCommand(SAVE_ACTION_COMMAND);
            saveItem.addActionListener(this);
            result.add(saveItem);
            separator = true;
        }
        
        if (print) {
            if (separator) {
                result.addSeparator();
                separator = false;
            }
            JMenuItem printItem = new JMenuItem(localizationResources.getString("Print..."));
            printItem.setActionCommand(PRINT_ACTION_COMMAND);
            printItem.addActionListener(this);
            result.add(printItem);
            separator = true;
        }
        
        if (zoom) {
            if (separator) {
                result.addSeparator();
                separator = false;
            }
            
            JMenu zoomInMenu = new JMenu(localizationResources.getString("Zoom_In"));
            
            zoomInBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
            zoomInBothMenuItem.setActionCommand(ZOOM_IN_BOTH_ACTION_COMMAND);
            zoomInBothMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInBothMenuItem);
            
            zoomInMenu.addSeparator();
            
            zoomInHorizontalMenuItem =
            new JMenuItem(localizationResources.getString("Horizontal_Axis"));
            zoomInHorizontalMenuItem.setActionCommand(ZOOM_IN_HORIZONTAL_ACTION_COMMAND);
            zoomInHorizontalMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInHorizontalMenuItem);
            
            zoomInVerticalMenuItem =
            new JMenuItem(localizationResources.getString("Vertical_Axis"));
            zoomInVerticalMenuItem.setActionCommand(ZOOM_IN_VERTICAL_ACTION_COMMAND);
            zoomInVerticalMenuItem.addActionListener(this);
            zoomInMenu.add(zoomInVerticalMenuItem);
            
            result.add(zoomInMenu);
            
            JMenu zoomOutMenu = new JMenu(localizationResources.getString("Zoom_Out"));
            
            zoomOutBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
            zoomOutBothMenuItem.setActionCommand(ZOOM_OUT_BOTH_ACTION_COMMAND);
            zoomOutBothMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutBothMenuItem);
            
            zoomOutMenu.addSeparator();
            
            zoomOutHorizontalMenuItem =
            new JMenuItem(localizationResources.getString("Horizontal_Axis"));
            zoomOutHorizontalMenuItem.setActionCommand(ZOOM_OUT_HORIZONTAL_ACTION_COMMAND);
            zoomOutHorizontalMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutHorizontalMenuItem);
            
            zoomOutVerticalMenuItem =
            new JMenuItem(localizationResources.getString("Vertical_Axis"));
            zoomOutVerticalMenuItem.setActionCommand(ZOOM_OUT_VERTICAL_ACTION_COMMAND);
            zoomOutVerticalMenuItem.addActionListener(this);
            zoomOutMenu.add(zoomOutVerticalMenuItem);
            
            result.add(zoomOutMenu);
            
            JMenu autoRangeMenu = new JMenu(localizationResources.getString("Auto_Range"));
            
            autoRangeBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
            autoRangeBothMenuItem.setActionCommand(AUTO_RANGE_BOTH_ACTION_COMMAND);
            autoRangeBothMenuItem.addActionListener(this);
            autoRangeMenu.add(autoRangeBothMenuItem);
            
            autoRangeMenu.addSeparator();
            autoRangeHorizontalMenuItem =
            new JMenuItem(localizationResources.getString("Horizontal_Axis"));
            autoRangeHorizontalMenuItem.setActionCommand(AUTO_RANGE_HORIZONTAL_ACTION_COMMAND);
            autoRangeHorizontalMenuItem.addActionListener(this);
            autoRangeMenu.add(autoRangeHorizontalMenuItem);
            
            autoRangeVerticalMenuItem =
            new JMenuItem(localizationResources.getString("Vertical_Axis"));
            autoRangeVerticalMenuItem.setActionCommand(AUTO_RANGE_VERTICAL_ACTION_COMMAND);
            autoRangeVerticalMenuItem.addActionListener(this);
            autoRangeMenu.add(autoRangeVerticalMenuItem);
            
            result.addSeparator();
            result.add(autoRangeMenu);
            
        }
        
        return result;
        
    }
    
    /**
     * The idea is to modify the zooming options depending on the type of chart being displayed by
     * the panel.  This code is incomplete.
     *
     * @param x  horizontal position of the popup.
     * @param y  vertical position of the popup.
     */
    protected void displayPopupMenu(int x, int y) {
        
        if (popup != null) {
            
            // go through each zoom menu item and decide whether or not to enable it...
            Plot plot = this.chart.getPlot();
            ValueAxis horizontalAxis = getHorizontalValueAxis(plot);
            boolean isHorizontal = (horizontalAxis != null);
            ValueAxis verticalAxis = getVerticalValueAxis(plot);
            boolean isVertical = (verticalAxis != null);
            
            if (this.zoomInHorizontalMenuItem != null) {
                this.zoomInHorizontalMenuItem.setEnabled(isHorizontal);
            }
            if (this.zoomOutHorizontalMenuItem != null) {
                this.zoomOutHorizontalMenuItem.setEnabled(isHorizontal);
            }
            if (this.autoRangeHorizontalMenuItem != null) {
                this.autoRangeHorizontalMenuItem.setEnabled(isHorizontal);
            }
            
            if (this.zoomInVerticalMenuItem != null) {
                this.zoomInVerticalMenuItem.setEnabled(isVertical);
            }
            if (this.zoomOutVerticalMenuItem != null) {
                this.zoomOutVerticalMenuItem.setEnabled(isVertical);
            }
            
            if (this.autoRangeVerticalMenuItem != null) {
                this.autoRangeVerticalMenuItem.setEnabled(isVertical);
            }
            
            if (this.zoomInBothMenuItem != null) {
                this.zoomInBothMenuItem.setEnabled(isHorizontal & isVertical);
            }
            if (this.zoomOutBothMenuItem != null) {
                this.zoomOutBothMenuItem.setEnabled(isHorizontal & isVertical);
            }
            if (this.autoRangeBothMenuItem != null) {
                this.autoRangeBothMenuItem.setEnabled(isHorizontal & isVertical);
            }
            
            popup.show(this, x, y);
        }
        
    }
    
    protected void finalize() {
        System.out.println("finalizing: " + getClass());
    }
    
    //? 5.8.2004
    public void setCrosshairNotBlocked(boolean val){
        crosshairNotBlocked=val;
    }
    
    public boolean getCrosshairNotBlocked(){
        return crosshairNotBlocked;
    }
    
    public void setDisableCrosshairTillLeavesDisplay(boolean b){
        disableCrosshairTillLeavesDisplay=b;
    }
    
    
    public void setStatusBarFrame(AbstractPlotComponent frame){
        this.frame=frame;
    }
    
    public void writeToStatusBar(String s){
        frame.writeToStatusBar(s);
    }
    
    public void setManager(AbstractManager manager){
        this.manager=manager;
    }
    
    public void enableZooming(){
        zoomingEnabled = true;
    }
    
    public void disableZooming(){
        zoomingEnabled = false;
    }
    
    private double userX(MouseEvent event){
        Rectangle2D rectangle=getScaledDataArea();
        double minX=rectangle.getMinX();
        double maxX=rectangle.getMaxX();
        
        Plot plot=chart.getPlot();
        
        int xc=event.getX();
        
        ValueAxis xa=this.getHorizontalValueAxis(plot);
        
        double xmin=xa.getLowerBound();
        double xmax=xa.getUpperBound();
        
        
        double u=(xc-minX)/(maxX-minX);
        return (u*(xmax-xmin)+xmin);
    }
    
    private double userY(MouseEvent event){
        Rectangle2D rectangle=getScaledDataArea();
        double minY=rectangle.getMinY();
        double maxY=rectangle.getMaxY();
        Plot plot=chart.getPlot();
        int yc=event.getY();
        
        ValueAxis ya=this.getVerticalValueAxis(plot);
        double ymin=ya.getLowerBound();
        double ymax=ya.getUpperBound();
        double v=(yc-minY)/(maxY-minY);
        v=1-v;
        return (v*(ymax-ymin)+ymin);
    }
    //?
    
}
