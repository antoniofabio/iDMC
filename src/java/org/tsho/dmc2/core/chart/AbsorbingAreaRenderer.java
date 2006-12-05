/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * CopiedDisplayright (C) 2004 Marji Lines and Alfredo Medio.
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.Vector;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.managers.AbstractManager;
import org.tsho.dmc2.ui.absorbingArea.AbsorbingAreaComponent;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;

//import com.sun.org.omg.CORBA.ExceptionDescription;


public class AbsorbingAreaRenderer implements DmcPlotRenderer {
    
    private DmcRenderablePlot plot;
    private DifferentiableMap map;
    
    private int type;
    
    // parameters
    private double[] parameters;
    private int iterations;
    private int transients;
    private double epsilon;
    
    // flags
    
    private boolean stopped;
    
    // internal state
    
    private int state;
    private int[] grid;
    private int[] gridBackup;
    
    private int gridWidth;
    private int gridHeight;
    private double xEpsilon;
    private double yEpsilon;
    
    private ImplicitDeterminant det;
    
    private boolean criticalSetFound=false;
    private boolean chooseSegmentsSet=false;
    private boolean plotAttractorSet=false;
    private boolean hideAttractorSet=false;
    private boolean iterateChosenSegmentsSet=false;
    private boolean justClearedSet=false;
    private boolean notYetRendered=true;
    
    //as a result of zooming or pressing Start button
    private boolean findCriticalSetAgain=false;
    //In present version always false
    private boolean zooming=false;
    
    //the private variables below are needed for choosing segments functionality
    private boolean mouseClicked;
    private int xClicked;
    private int yClicked;
    
    private int segmentsIteratesCount;
    
    private AbsorbingAreaComponent plotComponent;
    
    private BufferedImage image;
    
    //delay needed in the interactive mode in order not to waste machine cycles on running inside loops
    private int delay=100;
    
    //colors
    int attractorColor=15553;
    
    
    
    
    
    public AbsorbingAreaRenderer(final DifferentiableMap map, final DmcRenderablePlot plot, final AbsorbingAreaComponent plotComponent) {
        type=0;//currently the only type present
        this.map = map;
        this.plot = plot;
        this.plotComponent=plotComponent;
        this.segmentsIteratesCount=0;
    }
    
    public void initialize() {
        this.stopped = false;
        //at present version, disable zooming permanently
        AbstractManager manager=plotComponent.getManager();
        ((DmcChartPanel) manager.getChartPanel()).disableZooming();
        ((DmcChartPanel) manager.getChartPanel()).setManager(manager);
    }
    
    public void render(
    final Graphics2D g2, final Rectangle2D dataArea,
    final PlotRenderingInfo info) {
        
        
        state = STATE_RUNNING;
        
        gridWidth = (int) dataArea.getWidth();
        gridHeight = (int) dataArea.getHeight();
        
        //imageX,imageY correspond to point(0,0)
        int imageX = (int) dataArea.getX() + 1;
        int imageY = (int) dataArea.getY() + 1;
        
        DataBufferInt dataBuffer;
        
        image = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        grid = ((DataBufferInt) raster.getDataBuffer()).getData();
        
        ValueAxis domainAxis = plot.getDomainAxis();
        ValueAxis rangeAxis = plot.getRangeAxis();
        
        xEpsilon = Math.abs((domainAxis.getUpperBound() - domainAxis.getLowerBound())/ (double) gridWidth);
        yEpsilon = Math.abs((rangeAxis.getUpperBound() - rangeAxis.getLowerBound())/ (double) gridHeight);
        
        int numPoints = gridHeight * gridWidth;
        int index = 0;
        int rate = numPoints / 100;
        
        if (justClearedSet){
                if (criticalSetFound){
                    this.clearDisplay();
                    g2.drawImage(image, null, imageX, imageY);
                }
                justClearedSet=false;
                return;
        }
        
        
        if (!notYetRendered){
            plotCopiedDisplay();
            g2.drawImage(image, null, imageX, imageY);
        }
        notYetRendered=false;
        if (criticalSetFound && !findCriticalSetAgain){
            if (this.chooseSegmentsSet)
                chooseSegments(g2,image,imageX,imageY);
            if (this.plotAttractorSet)
                plotAttractor(g2,image,imageX,imageY);
            if (this.iterateChosenSegmentsSet)
                iterateChosenSegments(g2,image,imageX,imageY);
            if (this.hideAttractorSet)
                hideAttractor(g2,image,imageX,imageY);
        }
        else{
            this.disableAllActionsExceptStop();
            det=new AbsorbingAreaRenderer.ImplicitDeterminant(gridWidth,gridHeight,epsilon,g2,image,imageX,imageY);
            gridBackup=new int[grid.length];
            copyDisplay();
            criticalSetFound=true;
            findCriticalSetAgain=false;
            g2.drawImage(image, null, imageX, imageY);
            this.enableAllActionsExceptStop();
        }
    }
    
    
    public boolean getNotYetRendered(){
        return notYetRendered;
    }
    
    
    public void copyDisplay(){
        IntBuffer buffer=IntBuffer.allocate(grid.length);
        buffer.put(grid);
        buffer.flip();
        buffer.get(gridBackup);
    }
    
    
    public void plotCopiedDisplay(){
        IntBuffer buffer=IntBuffer.allocate(grid.length);
        buffer.put(gridBackup);
        buffer.flip();
        buffer.get(grid);
    }
    
    public void delayExecution(){
        try{
            Thread.currentThread().sleep(delay);
        }
        catch(Exception e){
        }
    }
    
    public void chooseSegments(Graphics2D g2,BufferedImage image,int imageX,int imageY){
        boolean atLeastOneSegmentEntered=false;
        disableAllActionsExceptStop();
        det.clearSegments();
        this.segmentsIteratesCount=0;
        mouseClicked=false;
        while(!stopped){
            while(!mouseClicked && !stopped){
                delayExecution();
            }
            if (stopped)
                break;
            mouseClicked=false;
            //else a mouse was clicked, and its coordinates (not Point coordinates) are stored in xClicked,yClicked. This click chooses a segment
            Point p=new Point();
            p.set(xClicked-imageX,yClicked-imageY);
            Segment s=det.getSegment(p);
            //color the segment with color -100000, say.
            det.plotSegment(s, -100000);
            g2.drawImage(image, null, imageX, imageY);
            //here the left point of the segment is chosen. If a point is clicked whose projection to the x-axis does not belong to the
            //projection of the segment, the leftmost point of the segment is chosen automatically. If stopped, the segment is stored as it is.
            while(!mouseClicked && !stopped){
                delayExecution();
            }
            if (stopped){
                //store segment
                det.addSegmentToIterate(s);
                break;
            }
            mouseClicked=false;
            int xlc=xClicked-imageX;
            if (xlc<s.xl) xlc=s.xl;
            if (xlc>s.xr) xlc=s.xr;
            det.plotSegment(s,-10000);//color the segment in the original color
            det.plotSegment(s,-100000,xlc,s.xr);
            g2.drawImage(image, null, imageX, imageY);
            //here the left point of the segment is chosen. If a point is clicked whose projection to the x-axis does not belong to the
            //projection of the segment, the rightmost point of the segment is chosen automatically. If stopped, the segment is stored with user chosen leftmost point.
            while(!mouseClicked && !stopped){
                delayExecution();
            }
            if (stopped){
                //store segment
                Segment s1=new Segment(xlc,s.xr,s.branchIndex);
                det.addSegmentToIterate(s1);
                break;
            }
            mouseClicked=false;
            int xrc=xClicked-imageX;
            if (xrc<s.xl) xrc=s.xl;
            if (xrc>s.xr) xrc=s.xr;
            det.plotSegment(s,-10000);//color the segment in the original color
            det.plotSegment(s,-100000,xlc,xrc);
            g2.drawImage(image, null, imageX, imageY);
            if (xlc<=xrc){
                Segment s2=new Segment(xlc,xrc,s.branchIndex);
                det.addSegmentToIterate(s2);
            }
        }
        stopped=false;
        this.enableAllActionsExceptStop();
    }
    
    public void iterateChosenSegments(Graphics2D g2,BufferedImage image,int imageX,int imageY){
        if (det.getSegments().size()!=0){
            plotCopiedDisplay();
            disableAllActionsExceptStop();
            segmentsIteratesCount++;
            iterateCurve(segmentsIteratesCount,g2,image,imageX,imageY);
            plotComponent.getIterateChosenSegmentsAction().setEnabled(true);
            copyDisplay();
            enableAllActionsExceptStop();
        }
    }
    
    public void plotAttractor(Graphics2D g2,BufferedImage image,int imageX,int imageY){
        //provisional
        
        disableAllActionsExceptStop();
        mouseClicked=false;
        while (!stopped){
            while (!stopped && !mouseClicked){
                delayExecution();
            }
            if (stopped){
                break;
            }
            mouseClicked=false;
            //mouse was clicked. Plot attractor starting from this point
            Point p=new Point();
            p.set(xClicked-imageX,yClicked-imageY);
            int index=0;
            for (int i=0;i<iterations;i++){
                if (i>transients){
                    if (getGridState(p)==0)
                        setGridState(p,attractorColor);
                }
                if (stopped) {
                    stopped=false;//only plotting the attractor was stopped
                    break;
                }
                iterate(p);
                index++;
                if (index==1000){
                    index=0;
                    g2.drawImage(image, null, imageX, imageY);
                }
            }
            g2.drawImage(image, null, imageX, imageY);
        }
        copyDisplay();
        stopped=false;
        enableAllActionsExceptStop();
        mouseClicked=false;
        
    }
    
    
    public void hideAttractor(Graphics2D g2,BufferedImage image,int imageX,int imageY){
        this.disableAllActionsExceptStop();
        plotCopiedDisplay();
        Point p=new Point();
        int index=0;
        for (int i=0;i<gridWidth;i++){
            for (int j=0;j<gridHeight;j++){
                p.set(i,j);
                if (getGridState(p)==attractorColor)
                    setGridState(p,0);
            }
            index++;
            if (index==1000){
                index=0;
                g2.drawImage(image, null, imageX, imageY);
            }
            if (stopped)
                break;
        }
        g2.drawImage(image, null, imageX, imageY);
        copyDisplay();
        stopped=false;
        enableAllActionsExceptStop();
    }
    
    public void clearDisplay(){
        this.disableAllActions();
        Point p=new Point();
        for (int i=0;i<gridWidth;i++){
            for (int j=0;j<gridHeight;j++){
                p.set(i,j);
                setGridState(p,0);
            }
            if (stopped)
                break;
        }
        copyDisplay();
        this.enableAllActionsExceptStop();
        stopped=false;
    }
    
    public void setMouseClicked(boolean flag){
        mouseClicked=flag;
    }
    
    public void setFindCriticalSetAgain(boolean flag ){
        findCriticalSetAgain=flag;
    }
    
    public void setXClicked(int value){
        xClicked=value;
    }
    
    public void setYClicked(int value){
        yClicked=value;
    }
    
    
    private void disableAllActions(){
        plotComponent.getPlotCriticalSetAction().setEnabled(false);
        plotComponent.getStopAction().setEnabled(false);
        plotComponent.getClearAction().setEnabled(false);
        plotComponent.getIterateChosenSegmentsAction().setEnabled(false);
        plotComponent.getChooseSegmentsAction().setEnabled(false);
        plotComponent.getHideAttractorAction().setEnabled(false);
        plotComponent.getPlotAttractorAction().setEnabled(false);
    }
    
    private void disableAllActionsExceptStop(){
        plotComponent.getPlotCriticalSetAction().setEnabled(false);
        plotComponent.getStopAction().setEnabled(true);
        plotComponent.getClearAction().setEnabled(false);
        plotComponent.getIterateChosenSegmentsAction().setEnabled(false);
        plotComponent.getChooseSegmentsAction().setEnabled(false);
        plotComponent.getHideAttractorAction().setEnabled(false);
        plotComponent.getPlotAttractorAction().setEnabled(false);
    }
    
    private void enableAllActionsExceptStop(){
        plotComponent.getPlotCriticalSetAction().setEnabled(true);
        plotComponent.getStopAction().setEnabled(false);
        plotComponent.getClearAction().setEnabled(true);
        plotComponent.getIterateChosenSegmentsAction().setEnabled(true);
        plotComponent.getChooseSegmentsAction().setEnabled(true);
        plotComponent.getHideAttractorAction().setEnabled(true);
        plotComponent.getPlotAttractorAction().setEnabled(true);
        stopped=false;
    }
    
    
    private double evaluateDeterminant(Point point){
        double[] jacobian=new double[4];
        map.evaluateJacobian(parameters,point.real(),jacobian);
        return jacobian[0]*jacobian[3]-jacobian[1]*jacobian[2];
    }
    
    /* returns false on error */
    private void iterate(Point point) {
        double[] result = new double[2];
        map.evaluate(parameters, point.real, result);
        point.set(result[0], result[1]);
    }
    
    private void iterate(Point point,int k) {
        for (int i=0;i<k;i++){
            iterate(point);
        }
    }
    
    private void setGridState(Point point, int state) {
        if (point.isInsideBounds())
            grid[point.locX() + point.locY() * gridWidth] = state;
    }
    
    
    private int getGridState(Point point) {
        return grid[point.locX() + point.locY() * gridWidth];
    }
    
    
    private class Point {
        double[] real = new double[2];
        int[] location = new int[2];
        
        Point() {}
        
        void set(Point p) {
            real[0] = p.realX();
            real[1] = p.realY();
            location[0] = p.locX();
            location[1] = p.locY();
        }
        
        void scale(double f){
            real[0]=real[0]*f;
            real[1]=real[1]*f;
            adjust();
        }
        
        void add(Point p){
            real[0]=real[0]+p.real[0];
            real[1]=real[1]+p.real[1];
            adjust();
        }
        
        //fixes the screen coords of the point if the display was changed
        void adjust(){
            location[0] = xLocation(real[0]);
            location[1] = yLocation(real[1]);
        }
        
        void set(double x, double y) {
            real[0] = x;
            real[1] = y;
            location[0] = xLocation(x);
            location[1] = yLocation(y);
        }
        
        void set(int x, int y) {
            location[0] = x;
            location[1] = y;
            real[0] = xValue(x);
            real[1] = yValue(y);
        }
        
        double realX() {
            return real[0];
        }
        
        double realY() {
            return real[1];
        }
        
        double[] real() {
            return real;
        }
        
        int locX() {
            return location[0];
        }
        
        int locY() {
            return location[1];
        }
        
        int[] location() {
            return location;
        }
        
        boolean isInsideBounds() {
            if (location[0] > gridWidth - 1
            || location[0] < 0
            || location[1] > gridHeight - 1
            || location[1] < 0) {
                return false;
            }
            
            return true;
        }
        
        boolean isInfinite() {
            Double a0=new Double(real[0]);
            Double a1=new Double(real[1]);
            if (a0.isInfinite() || a1.isInfinite() || a0.isNaN()|| a1.isNaN())
                return true;
            else
                return false;
        }
        
    }
    
    private int xLocation(double val) {
        return (int) ((val - plot.getDomainAxis().getLowerBound()) / xEpsilon);
    }
    private int yLocation(double val) {
        return (int) (-(val - plot.getRangeAxis().getUpperBound()) / yEpsilon);
    }
    
    //point coordinates w.r.t. state variables
    private double xValue(int x) {
        return ((double) x * xEpsilon) - xEpsilon / 2
        + plot.getDomainAxis().getLowerBound();
    }
    private double yValue(int y) {
        return  plot.getRangeAxis().getUpperBound()
        - ((double) y * yEpsilon) + yEpsilon / 2;
    }
    
    private int pixelDistance(Point p1, Point p2){
        int xd=Math.abs(p1.location[0]-p2.location[0]);
        int yd=Math.abs(p1.location[1]-p2.location[1]);
        if (xd<yd) return yd; else return xd;
    }
    
    public void initialize(
    final double[] parameters, final double epsilon,
    final int iterations, final int transients) {
        
        this.parameters = parameters;
        this.iterations = iterations;
        this.transients=transients;
        this.epsilon = epsilon;
    }
    
    public LegendItemCollection getLegendItems() {
        return null;
    }
    
    public void stop() {
        stopped = true;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int i) {
        state = i;
    }
    
    public void setType(int type){
        this.type=type;
    }
    
    //needed for samples
    public int getType(){
        return type;
    }
    
    private Point parameterizedCurvePoint(double t){
        Point p=det.getPoint(t);
        return p;
    }
    
    private void iterateCurve(int k, Graphics2D g, BufferedImage image, int imageX, int imageY){
        double delta=0.00001;//should be taken from GUI
        double deltaUpperBound=0.001;
        double deltaLowerBound=0.000001;
        int index=0;
        double t=0;
        double t1=0;
        double tnext;
        Point prevPoint=parameterizedCurvePoint(t);
        Point nextPoint;
        
        iterate(prevPoint,k);
        if (prevPoint.isInsideBounds())
            setGridState(prevPoint,-20000*k);
        
        while (t<1){
            double delta1=delta;
            boolean isDeltaFound=false;
            boolean isPreviousDistanceTooSmall=false;
            //boolean ignoreDistanceTooSmall=false;
            if (prevPoint.isInsideBounds()){
                while (!isDeltaFound && delta1<deltaUpperBound && delta1>deltaLowerBound){
                    t1=t+delta1;
                    nextPoint=parameterizedCurvePoint(t1);
                    iterate(nextPoint,k);
                    if (pixelDistance(prevPoint,nextPoint)==0){
                        delta1=delta1*2;
                        isPreviousDistanceTooSmall=true;
                    }else{
                        if (pixelDistance(prevPoint,nextPoint)>1 && isPreviousDistanceTooSmall){
                            delta1=delta1/2;
                            isDeltaFound=true;
                        }else{
                            if (pixelDistance(prevPoint,nextPoint)>1 && !isPreviousDistanceTooSmall){
                                delta1=delta1/2;
                            }else{
                                if (pixelDistance(prevPoint,nextPoint)<=1 ){
                                    isDeltaFound=true;
                                }
                            }
                        }
                    }
                    if (stopped)
                        break;
                }
            }
            
            if (stopped)
                break;
            t=t+delta1;
            
            nextPoint=parameterizedCurvePoint(t);
            iterate(nextPoint,k);
            prevPoint.set(nextPoint);
            
            if (nextPoint.isInsideBounds())
                setGridState(nextPoint,-20000*k);
            g.drawImage(image,null,imageX,imageY);
        }
    }
    
    //private class which is responsible for computing the critical locus (computed as an implicit function)
    private class ImplicitDeterminant{
        
        private Vector sections;
        private Vector jumps;
        private Vector segments;
        private int gridWidth,gridHeight;
        private Graphics2D g2;
        private BufferedImage image;
        private int imageX;
        private int imageY;
        
        ImplicitDeterminant(int gridWidth,int gridHeight,double epsilon,Graphics2D g2,BufferedImage image,int imageX,int imageY){
            this.g2=g2;
            this.image=image;
            this.imageX=imageX;
            this.imageY=imageY;
            sections=new Vector(gridWidth);
            segments=new Vector();
            Point p=new Point();
            this.gridWidth=gridWidth;
            this.gridHeight=gridHeight;
            
            for (int i=0;i<gridWidth;i++){
                if (stopped)
                    break;
                p.set(i,0);
                Section s=new Section(p.realX(),epsilon);
                this.plotSection(s, -10000);
                sections.addElement(s);
            }
            
            
            int previousCount=((Section) sections.elementAt(0)).branches.size();
            int currentCount;
            jumps=new Vector();
            for (int i=1;i<gridWidth;i++){
                if (stopped)
                    break;
                currentCount=((Section) sections.elementAt(i)).branches.size();
                if (currentCount!=previousCount)
                    jumps.addElement(new Integer(i));
                previousCount=currentCount;
            }
        }
        
        void clearSegments(){
            segments.clear();
        }
        
        void chooseAllSegments(){
            clearSegments();
            int xl=0;
            int xr=0;
            for (int i=0;i<jumps.size();i++){
                xr=((Integer) jumps.elementAt(i)).intValue()-1;
                for (int j=0;j<((Section) sections.elementAt(xr)).branches.size();j++){
                    Segment s=new Segment(xl,xr,j);
                    segments.addElement(s);
                }
                xl=xr+1;
            }
            if (xl<gridWidth){
                xr=gridWidth-1;
                for (int j=0;j<((Section) sections.elementAt(xr)).branches.size();j++){
                    Segment s=new Segment(xl,xr,j);
                    segments.addElement(s);
                }
            }
        }
        
        //state is probably the color RGB value
        void plotSection(Section s, int state){
            double x=s.x;
            double y;
            for (int i=0;i<s.branches.size();i++){
                y=((Double) s.branches.elementAt(i)).doubleValue();
                Point p=new Point();
                p.set(x,y);
                setGridState(p, state);
                g2.drawImage(image, null, imageX, imageY);
            }
        }
        
        //0.00001 should be supplied as parameter
        void plot(){
            chooseAllSegments();
            double t=0;
            int index=0;
            while (t+0.00001<1){
                t=t+0.00001;
                Point p=this.getPoint(t);
                setGridState(p,-30000);
            }
            g2.drawImage(image, null, imageX, imageY);
        }
        
        void plotSegment(Segment s, int state){
            int xl=s.xl;
            int xr=s.xr;
            int i=s.branchIndex;
            for (int x=xl;x<=xr;x++){
                Point p=((Section) sections.elementAt(x)).getBranch(i);
                setGridState(p,state);
            }
            g2.drawImage(image, null, imageX, imageY);
        }
        
        void plotSegment(Segment s, int state,int xlc,int xrc){
            int xl=s.xl;
            int xr=s.xr;
            if (xlc<xl || xlc>xr)
                xlc=xl;
            if (xrc<xl || xrc>xr)
                xrc=xr;
            int i=s.branchIndex;
            for (int x=xlc;x<=xrc;x++){
                Point p=((Section) sections.elementAt(x)).getBranch(i);
                setGridState(p,state);
            }
            g2.drawImage(image, null, imageX, imageY);
        }
        
        
        //returns the segment (its branchIndex is set to 0) to which the screen x coordinate belongs
        Segment getSegment(int x){
            int[] s=new int[2];
            int i=0;
            while (i<jumps.size()){
                if (x<((Integer) jumps.elementAt(i)).intValue())
                    break;
                i++;
            }
            if (jumps.size()==0){
                s[0]=0;
                s[1]=gridWidth-1;
            }
            else{
                if (i==0){
                    s[0]=0;
                    s[1]=((Integer) jumps.elementAt(0)).intValue()-1;
                }
                else{
                    if (i==jumps.size()){
                        s[1]=gridWidth-1;
                        s[0]=((Integer) jumps.elementAt(i-1)).intValue();
                    }
                    else{
                        s[0]=((Integer) jumps.elementAt(i-1)).intValue();
                        s[1]=((Integer) jumps.elementAt(i)).intValue()-1;
                    }
                }
            }
            Segment segment=new Segment(s[0],s[1],0);
            return segment;
        }
        
        
        //find segment nearest to the point clicked. Returns null if above the x-coord of p there is no branch of the det=0 locus.
        Segment getSegment(Point p){
            Segment segment =getSegment(p.locX());
            //need to find the branchIndex
            Section sec=(Section) sections.elementAt(p.locX());
            double pd=0,cd;
            Point sp;
            int index=0;
            for (int i=0;i<sec.branches.size();i++){
                sp=sec.getBranch(i);
                cd=Math.abs(sp.realY()-p.realY());
                if (i!=0){
                    if (cd<pd){
                        index=i;
                        pd=cd;
                    }
                }
                else{
                    pd=cd;
                    index=0;
                }
            }
            if (sec.branches.size()==0)
                return null;
            else{
                Segment s=new Segment(segment.xl,segment.xr,index);
                return s;
            }
        }
        
        Vector getSegments(){
            return segments;
        }
        
        //given a segment [x0,x1], the function returns the interpolated value, based on the class variable sections for
        //t between 0 and 1, where t=0 corresponds to x0, t=1 corresponds to x1.
        Point getPoint(Segment segment,double t){
            double x=segment.xl*(1-t)+segment.xr*t;
            int xi=(int) (Math.floor(x));
            //next 4 lines to avoid numerical errors
            if (xi<segment.xl)
                xi=segment.xl;
            if (xi>=segment.xr){
                Point p=((Section) sections.elementAt(segment.xr)).getBranch(segment.branchIndex);
                return p;
            }
            double tau=x-xi;
            Point p1;
            Point p2;
            Section s1=(Section) sections.elementAt(xi);
            Section s2=(Section) sections.elementAt(xi+1);
            p1=s1.getBranch(segment.branchIndex);
            p2=s2.getBranch(segment.branchIndex);
            p1.scale(1-tau);
            p2.scale(tau);
            p1.add(p2);
            return p1;
        }
        
        //returns the point, parameterized by t, lying on the union of segments which are stored in the class variable segments
        Point getPoint(double t){
            if (segments.size()==0) return null;
            double ts=1/((double)segments.size());
            int i=(int)(Math.floor(t*segments.size()));
            //if (i==1){
            //    boolean letmeknow=true;
            //}
            double tau=(t-i*ts)/ts;
            //numerical errors control
            if (i==segments.size())
                return getPoint((Segment) segments.elementAt(i-1),1);
            if (i==-1)
                return getPoint((Segment) segments.elementAt(0),0);
            return getPoint((Segment) segments.elementAt(i),tau);
        }
        
        void addSegmentToIterate(Segment s){
            segments.addElement(s);
        }
        
    }
    
    
    private class Section{
        double x;
        double epsilon;
        Vector branches;
        
        
        Section(double x, double epsilon){
            this.epsilon=epsilon;
            branches =new Vector();
            this.x=x;
            Point p=new Point();
            Point tp= new Point();
            tp.set(0,0);//to get the real y-coordinate
            p.set(x,tp.realY());
            int sign;
            int previousSign;
            double start=0,end=0;
            
            double det=evaluateDeterminant(p);
            if (det>epsilon)
                sign=1;
            else{
                if (det<-epsilon)
                    sign=-1;
                else
                    sign=0;
            }
            boolean insideZeros=false;
            if (sign==0){
                insideZeros=true;
                start=p.realY();
            }
            previousSign=sign;
            
            for (int j=0;j<gridHeight;j++){
                
                tp.set(0,j);
                p.set(x,tp.realY());
                if (stopped)
                    break;
                det=evaluateDeterminant(p);
                if (det>epsilon)
                    sign=1;
                else{
                    if (det<-epsilon)
                        sign=-1;
                    else{
                        sign=0;
                    }
                }
                
                if (sign!=previousSign && previousSign!=0){
                    insideZeros=true;
                    start=p.realY();
                }
                if (sign==0 && previousSign!=0){
                    insideZeros=true;
                    start=p.realY();
                }
                if (sign!=0 && insideZeros){
                    tp.set(p.locX(),p.locY()-1);
                    end=tp.realY();
                    add((start+end)/2);
                    insideZeros=false;
                }
                // -2 (and not -1) because of roundoff errors with Point
                if (sign==0 && p.locY()==gridHeight-2 && insideZeros){
                    end=p.realY();
                    add((start+end)/2);
                }
                previousSign=sign;
            }
        }
        
        //adds a branch coordinate corresponding to x
        void add(double s){
            branches.addElement(new Double(s));
        }
        
        Point getBranch(int i){
            if (i < branches.size()){
                double a=((Double) branches.elementAt(i)).doubleValue();
                Point p=new Point();
                p.set(x,a);
                return p;
            }
            else
                return null;
        }
    }
    
    
    private class Segment{
        int xl;
        int xr;
        int branchIndex;
        
        Segment(int xl,int xr,int branchIndex){
            this.xl=xl;
            this.xr=xr;
            this.branchIndex=branchIndex;
        }
    }
    
    
    public void setPlotAttractor(){
        this.justClearedSet=false;
        this.plotAttractorSet=true;
        this.chooseSegmentsSet=false;
        this.iterateChosenSegmentsSet=false;
        this.hideAttractorSet=false;
    }
    
    public void setIterateChosenSegments(){
        this.justClearedSet=false;
        this.iterateChosenSegmentsSet=true;
        this.chooseSegmentsSet=false;
        this.plotAttractorSet=false;
        this.hideAttractorSet=false;
    }
    
    public void setHideAttractor(){
        this.justClearedSet=false;
        this.hideAttractorSet=true;
        this.plotAttractorSet=false;
        this.iterateChosenSegmentsSet=false;
        this.chooseSegmentsSet=false;
    }
    
    public void setChooseSegments(){
        this.justClearedSet=false;
        this.hideAttractorSet=false;
        this.plotAttractorSet=false;
        this.iterateChosenSegmentsSet=false;
        this.chooseSegmentsSet=true;
    }
    
    public void setJustCleared(){
        this.justClearedSet=true;
         this.hideAttractorSet=false;
        this.plotAttractorSet=false;
        this.iterateChosenSegmentsSet=false;
        this.chooseSegmentsSet=false;
    }
    
    public void resetSegmentsIteratesCount(){
        segmentsIteratesCount=0;
    }
    
}




