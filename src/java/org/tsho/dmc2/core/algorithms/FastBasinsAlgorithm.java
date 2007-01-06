package org.tsho.dmc2.core.algorithms;

import java.util.Vector;

import org.tsho.dmc2.core.chart.BasinRenderer;

public class FastBasinsAlgorithm implements BasinsAlgorithm {
	private BasinRenderer br;
	private Grid grid;
	private final int attractorLimit;
	private final int attractorIterations;
	
    private static final int INFINITY = 1;
    
    private Vector attractorsSamplePoints;
    
    private int rate, index;
    double[] startPoint, currentPoint; // (x, y)
    
    public FastBasinsAlgorithm(BasinRenderer br){
		this.br = br;
		grid = br.getGrid();
		rate = br.getRate();
		attractorLimit = br.getAttractorLimit();
		attractorIterations = br.getAttractorIterations();
        attractorsSamplePoints = br.getAttractorsSamplePoints();
        index=0;
    }
    
	public void run() {
        int color;
        
        int attractorColor = 2;
        int basinColor = attractorColor+1;

        double[] startPoint, currentPoint; // (x, y)

        int algorithmState;
        int attr = -1;

        while (grid.nextFreeCell()) {
        	startPoint = grid.getCurrPoint();
            index++;

            if (rate > 0 && index % rate == 0)
            	br.drawImage();

            if (br.isStopped())
                return;

            currentPoint = (double[]) startPoint.clone();
            grid.setValue(currentPoint, basinColor);
            color = basinColor;
            attr = -1;

            for (int i = 0;;) {
                if (br.isStopped())
                    return;
                
                br.iterate(currentPoint);
                i++;

                if (br.isPointInfinite(currentPoint)) {
                     fillTrack(startPoint, i, INFINITY);
                     break;
                }
                if (!grid.isPointInsideBounds(currentPoint)) {
                     if (i >= attractorLimit) {
                         fillTrack(startPoint, i, INFINITY);
                         break;
                     }
                     else {
                         continue;
                     }
                }

                algorithmState = grid.getValue(currentPoint);

                /* untouched */
                if (algorithmState == 0) {
                    grid.setValue(currentPoint, color);
                    continue;
                }

                if (algorithmState == INFINITY) {
                    fillTrack(startPoint, i - 1, INFINITY);
                    break;
                }

                if (algorithmState == basinColor) {
                    if (attr == -1) {
                        attr = i;
                        continue;
                    }
                    else if (i - attr < attractorLimit) {
                        continue;
                    }
                    else if (i - attr < (attractorLimit+attractorIterations)) {
                        if (color != attractorColor) {
                            color = attractorColor;
                        }

                        grid.setValue(currentPoint, color);
                        continue;
                    }
                    else {
                        continue;
                    }
                    // not reached
                }

                if (algorithmState == attractorColor) {
                    if (attr != -1 && i - attr < (attractorLimit+attractorIterations)) {
                        continue;
                    }
                    else {
                        attractorColor += 2;
                        basinColor = attractorColor+1;
                        attractorsSamplePoints.add(currentPoint);
                        break;
                    }
                }

                attr = -1;


                /* another basin encountered */
                if (!isOdd(algorithmState) && algorithmState != basinColor && algorithmState != INFINITY) {
                    fillTrack(startPoint, i - 1, algorithmState);
                    break;
                }

                /* another attractor */
                if (isOdd(algorithmState) && algorithmState != attractorColor && algorithmState != INFINITY) {
                    fillTrack(startPoint, i - 1, algorithmState+1);
                    break;
                }

                throw new Error("program should not reach this line.");
            }
        }

        br.drawImage();
	}
	
	private static boolean isOdd(int i) {
		return i==((i/2)*2);
	}
        
    private void fillTrack(double[] start, int iterations, int state) {
        double[] point = (double[]) start.clone();
        
        grid.setValue(point, state);
        
        for (int i = 0; i < iterations; i++) {
            
            br.iterate(point);
            if(!grid.isPointInsideBounds(point))
            	continue;
            
            grid.setValue(point, state);
        }
    }

}
