package org.tsho.dmc2.core.algorithms;

import java.util.Vector;

import org.tsho.dmc2.core.chart.*;

public class SlowBasinsAlgorithm implements BasinsAlgorithm {
	private BasinRenderer br;
	private Grid grid;
	private final int attractorLimit;
	private final int attractorIterations;
	
    private static final int UNTOUCHED = 0;
    private static final int INFINITY = 1;
    /** Records for each attractor found a point in its basin. 
     * This way, also after the image is enlarged,
     * we may generate the attractors and basins anew with the same colors. */
    private Vector attractorsSamplePoints;
    
    private int attractorColor, index, numberOfRandomPoints, rate;
    /**	attractor1 is considered the same as attractor2 if the ratio of points of attractor1 which belong to attractor2,
    to the points which belong to attractor 1 is larger than overlapFactor. This relation is nonsymmetric.
    Identity of points is being measured on the display. */
    private final double overlapFactor=0.1;
    double[] startPoint, currentPoint; // (x, y) 
	
	public SlowBasinsAlgorithm(BasinRenderer br) {
		this.br = br;
		grid = br.getGrid();
		rate = br.getRate();
		attractorLimit = br.getAttractorLimit();
		attractorIterations = br.getAttractorIterations();
		numberOfRandomPoints = br.getNumberOfRandomPoints();
        attractorsSamplePoints = br.getAttractorsSamplePoints();
        index=0;

        int attractorIndex=0;
        for (int i=0; i<numberOfRandomPoints; i++){
            int [] attractorsCoincidences=new int[numberOfRandomPoints];//assuming initially all zeros
            boolean isInfinite=false;
            boolean isNewAttractorFound=true;
            double[] p = generateRandomPoint();
            startPoint = p;
            attractorColor = getAttractorColor(attractorIndex);
            
            //1-st pass
            for (int j=1;j<(attractorLimit+attractorIterations);j++){
                br.iterate(p);
                if (br.isPointInfinite(p)){
                    isInfinite=true;
                    break;
                }
                if (j==attractorLimit+1)
                    startPoint=p;
                if (j>attractorLimit && grid.isPointInsideBounds(p)){
                    int gs = grid.getValue(p);
                    if (gs != UNTOUCHED){
                        if (gs!=INFINITY){
                            attractorColor = grid.getValue(p);
                            attractorsCoincidences[getAttractorIndex(attractorColor)]+=1;
                        }
                    }
                }
            }
            
            if (!isInfinite){
                for (int ii=0;ii<attractorIndex;ii++){
                    if (attractorsCoincidences[ii]>overlapFactor*attractorIterations){
                        isNewAttractorFound=false;
                        break;
                    }
                }
            }
            else{
                isNewAttractorFound=false;
            }
            
            //2-nd pass
            if (br.isStopped()) 
            	return;
            if (!isInfinite){
                if (isNewAttractorFound) {
                    fillTrack(startPoint,attractorIterations,getAttractorColor(attractorIndex));
                    attractorIndex++;
                    double[] temp = (double[]) startPoint.clone();
                    attractorsSamplePoints.addElement(temp);
                }
            }
            br.drawImage();
        } //end for each trial
        System.out.println("" + attractorsSamplePoints.size() + " attractors found");
	}

	public void run() {
        startPoint = new double[2];
        
        while (grid.nextFreeCell()) {
            startPoint = grid.getCurrPoint();
            if (br.isStopped())
                break;
            boolean attractorNotFound=true;
            int attractorIndex=INFINITY;
            index++;
            
            if (rate > 0 && index % rate == 0)
            	br.drawImage();
            
            if (br.isStopped())
                return;
            
            currentPoint = (double[]) startPoint.clone();
            
            int jj=0;
            while (attractorNotFound && jj<=attractorLimit){
                if (grid.isPointInsideBounds(currentPoint)){
                	int val = grid.getValue(currentPoint);
                	if(val!=UNTOUCHED && val!=INFINITY && isEven(val)) { //attractor detected
                		attractorIndex = getAttractorIndex(val);
                        attractorNotFound=false;
                	}
                }
                jj++;
                br.iterate(currentPoint);
            }
            
            if (!attractorNotFound){
                fillTrack(startPoint,jj-1,getBasinColor(attractorIndex));
            } else{
                grid.setValue(startPoint,INFINITY);
            }
        }
	}
	
    private double[] generateRandomPoint(){
    	double[] r = grid.getRanges();
        return new double[] {(r[1]-r[0])*Math.random()+r[0], (r[3]-r[2])*Math.random()+r[2]};
    }
    
    private void fillTrack(double[] start, int iterations, int state) {
        double[] point = (double[]) start.clone();
        
        grid.setValue(point, state);
        
        for (int i = 0; i < iterations; i++) {
            
            br.iterate(point);
            if(!grid.isPointInsideBounds(point))
            	continue;
            
            int gs = grid.getValue(point);
            if (gs==UNTOUCHED)
                grid.setValue(point, state);
        }
    }
    
	private static boolean isEven(int i) {
		return i==((i/2)*2);
	}
	
	private static int getAttractorColor(int attractorId) {
		return attractorId*2+2;
	}
	private static int getBasinColor(int attractorId) {
		return getAttractorColor(attractorId)+1;
	}
	
	/**
	 * Get attractor index from a given color (which can be a basin or an attractor itself)
	 * */
    private static int getAttractorIndex(int color){
        return (color-2)/2;
    }

}
