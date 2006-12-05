package org.tsho.dmc2.core.algorithms;

import org.tsho.dmc2.core.util.TestDataset;

import junit.framework.TestCase;

public class TestGrid extends TestCase {
	double[] ranges= {1,11,1,11};
	double infinity = 12;
	int nr=10;
	int nc=10;
	
	double xEps = (ranges[1]-ranges[0])/(double) nc;
	double yEps = (ranges[3]-ranges[2])/(double) nr;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestDataset.class);
	}
	
	public void testCreate() {
		Grid g = new Grid(ranges, nr, nc);
		assertEquals("Incorrect number of columns reported", g.getNc(), nc);
		assertEquals("Incorrect number of rows reported", g.getNr(), nr);
		assertTrue("Point expected to be inside bounds",
				g.isPointInsideBounds(new double[] {1.5,1.5}));
		assertTrue("Point expected to be outside bounds",
				!g.isPointInsideBounds(new double[] {0.5,0.5}));
	}
	
	public void testGetSet() {
		Grid g = new Grid(ranges, nr, nc);
		double [] p1 = new double[] {1.5, 1.5};
		double [] p2 = new double[] {0.5, 0.5};
		g.setValue(p1, 1);
		assertEquals("Invalid value returned", 1, g.getValue(p1));
		assertEquals("Invalid value returned", 1, g.getValue(new double[] {1.9, 1.9}));
		assertEquals("Invalid value returned", 1, g.getValue(new double[] {1.1, 1.1}));
		assertEquals("Invalid value returned", Grid.EMPTY, g.getValue(new double[] {2.5, 2.5}));
		assertTrue("Bad check on grid bounds", g.isPointInsideBounds(p1));
		assertTrue("Bad check on grid bounds", !g.isPointInsideBounds(p2));		
	}
	
	/**
	 * Extensive tests on grid getting/setting capabilities
	 * */
	public void testGetSet2() {
		double[][][] track = new double[nr][nc][2];
		int[][] val = new int[nr][nc];
		
		Grid g1 = new Grid(ranges, nr, nc);
		/* Fill the whole grid with random values, then check if they were correctly stored
		 * */
		for(int i=0; i<nr; i++)
			for(int j=0; j<nc; j++) {
				track[i][j][0] = (j+0.5)*xEps + ranges[0];				
				track[i][j][1] = (i+0.5)*yEps + ranges[2];
				val[i][j] = ((int)Math.floor(Math.random()*10)) + 1;
				try{
					g1.setValue(track[i][j], val[i][j]);
				} catch(Exception e) {
					System.err.println("x = "+ track[i][j][0]);
					System.err.println("y = "+ track[i][j][1]);
					System.err.println("val = "+ val[i][j]);
					assertTrue("Unexpected exception: "+ e, false);
				}
			}
		for(int i=0; i<nr; i++) 
			for(int j=0; j<nc; j++) {
				assertEquals("Incorrect value returned", val[i][j], g1.getValue(track[i][j]));
		}
	}
	
	public void testCheck() {
		Grid g = new Grid(ranges, nr, nc);
		int i=0;
		while(g.nextFreeCell()) {
			g.setValue(1);
			i++;
		}
		assertEquals("Not all iterations where completed", nr*nc, i);
		assertEquals(1, g.getValue(new double[] {1.5,1.5}));
		assertEquals(1, g.getValue(new double[] {10.5,10.5}));		
	}
	
	public void testCheck2() {
		double[] p = new double[] {infinity+1, infinity+1};
		double[] p2 = new double[] {-infinity, -infinity};
		double[] p3 = new double[] {3.5, 11.5};
		double[] p4 = new double[] {11.5, 3.5};
		Grid g = new Grid(ranges, nr, nc);
		assertTrue("Expected out of bounds point", !g.isPointInsideBounds(p));
		assertTrue("Expected out of bounds point", !g.isPointInsideBounds(p2));
		assertTrue("Expected out of bounds point", !g.isPointInsideBounds(p3));
		assertTrue("Expected out of bounds point", !g.isPointInsideBounds(p4));
	}
	
	public void testInternals() {
		int[] data;
		Grid g = new Grid(ranges, nr, nc);
		data = g.getData();
		g.setValue(new double[] {2.5, 9.5}, 1);
		System.out.println("cell "+ findValue(g, 1) + " was set");
		assertEquals("Incorrect cell positioning",1,data[11]);
		g.setValue(new double[] {2.5, 10.5}, 1);
		System.out.println("cell "+ findValue(g, 1) + " was set");
		assertEquals("Incorrect cell positioning",1,data[1]);
		g.setValue(new double[] {1.5, 10.5}, 1);
		System.out.println("cell "+ findValue(g, 1) + " was set");
		assertEquals("Incorrect cell positioning",1,data[0]);
	}
	
	/**
	 * Test coordinates traslation system
	 * */
	public void testTraslation() {
		Grid g = new Grid(ranges, nr, nc);
		g.setValue(0, 0, 1);
		assertEquals(1, g.getValue(new double[] {1.5,1.5}));
		g.setValue(0, 9, 1);
		assertEquals(1, g.getValue(new double[] {1.5,10.5}));
	}
	
	/**
	 * Test coordinate system traslation
	 * In particular, checks that grid filling is done row by row
	 * */
	public void testCoordinates() {
		Grid g = new Grid(ranges, nr, nc);
		assertEquals("incorrect id reported", 0, g.getCurrId());
		g.setValue(10);
		double[] p = g.getCurrPoint();
		/** first point should be top-left */
		assertTrue("incorrect coordinate value", (1.5==p[0]) & (10.5==p[1]) );
		g.nextFreeCell();
		assertEquals("incorrect id reported", 1, g.getCurrId());
		p = g.getCurrPoint();
		/** second point should be the second point on the last row */
		assertTrue("incorrect coordinate value", (2.5==p[0]) & (10.5==p[1]) );
		for(int i=0; i<10; i++){
			g.setValue(1);			
			g.nextFreeCell();
		}
		p = g.getCurrPoint();
		/** after a full line, next point should be the second point on the last-1 row */
		assertTrue("incorrect coordinate value", (2.5==p[0]) & (9.5==p[1]) );
	}

	/**
	 * Search where is 'value' in grid 'g' and returns internal single index
	 * */
	private int findValue(Grid g, int value) {
		int[] data = g.getData();
		for(int i=0; i<(g.getNr()*g.getNc()); i++)
			if(data[i]==value)
				return i;
		return -1;
	}

}
