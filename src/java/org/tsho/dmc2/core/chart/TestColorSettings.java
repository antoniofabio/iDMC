/**
 * 
 */
package org.tsho.dmc2.core.chart;

import junit.framework.TestCase;
import java.awt.Color;

/**
 * @author Antonio
 *
 */
public class TestColorSettings extends TestCase {
	ColorSettings cs;
	
	public void testCreate() {
		cs = new ColorSettings(TestColorSettings.class);
		cs.reset();
		//Test defaults:
		assertEquals(ColorSettings.defaultPairs.length, cs.getPairsVector().size());
		assertEquals(ColorSettings.defaultEmpty, cs.getEmpty());
		assertEquals(ColorSettings.defaultInfinity, cs.getInfinity());
	}
	
	public void testSaveRetrieve() {
		cs = new ColorSettings(TestColorSettings.class);
		cs.reset();
		cs.setEmpty(10);
		cs.setInfinity(20);
		cs.getPairsVector().clear(); //clear all pairs
		cs.addPair(new int[] {30, 40});
		cs.addPair(new int[] {50, 60});
		cs.addPair(new int[] {70, 80});
		cs.save();
		ColorSettings cs2 = new ColorSettings(TestColorSettings.class);
		assertEquals(10, cs2.getEmpty());
		assertEquals(20, cs2.getInfinity());
		assertEquals(3, cs2.getPairsVector().size());
		assertEquals(60, ((int[])cs2.getPair(1))[1]);
		assertEquals(70, ((int[])cs2.getPair(2))[0]);
	}
	
	public void testClone() {
		//TODO: write cloning test
	}
	
}
