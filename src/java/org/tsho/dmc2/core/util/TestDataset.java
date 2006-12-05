package org.tsho.dmc2.core.util;

import java.io.*;

import junit.framework.TestCase;

public class TestDataset extends TestCase {
	double[][] table = {
			{0.1d, 0.2d, 0.3d},
			{1d, 2d, 3d}
	};	

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestDataset.class);
	}
	
	/**
	 * Testing method 
	 */
	public void testCreate() {
		Dataset d = new Dataset(3);
		assertEquals("incorrect number of columns after creation", d.getNcol(), 3);
		assertEquals("incorrect number of rows after creation", d.getNrows(), 0);
		try{
			d = new Dataset(table);
		} catch(DatasetException e) {
			assertTrue("Unexpected DatasetException: " + e, false);
		}
		assertEquals("incorrect number of columns after creation", d.getNcol(), 3);
		assertEquals("incorrect number of rows after creation", d.getNrows(), 2);		
	}
	public void testUpdate() {
		Dataset d = new Dataset(3);
		
		try {
			d.addRow(table[0]);
			d.addRow(table[1]);
		} catch ( DatasetException e) {
			assertTrue("Can't add regular rows to the dataset", false);
		}

		assertEquals(d.getNcol(), 3);
		assertEquals(d.getNrows(), 2);
	}
	
	public void testSaving() {
		Dataset d = new Dataset(3);
		try{
			d.addRow(table[0]);
			d.addRow(table[1]);
		} catch (DatasetException e) {
			assertTrue("can't add regular rows", false);
		}
		
		File tmpfile = new File("testOutput.csv");
		BufferedReader in;
		String currline;
		try {
			d.save(tmpfile);
			in = new BufferedReader(new FileReader(tmpfile));
			currline = in.readLine();
			int tmpi=0;
			while(in.ready()) {
				currline = in.readLine();
				assertEquals("dataset line doesn't match",currline, Dataset.asStringRecord(d.getRow(tmpi)));
				tmpi++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		tmpfile.delete();
	}
	
	public void testLoad() {
		Dataset d = new Dataset(3);
		File tmpfile = new File("tmpfile.csv");
		try{
			d.addRow(table[0]);
			d.addRow(table[1]);
			d.save(tmpfile);
			Dataset d2 = Dataset.load(tmpfile);
			for(int j = 0; j<d2.getNrows(); j++)
				for(int i=0; i<d2.getRow(j).length; i++)
					assertTrue("Righe ineguali", d2.getRow(j)[i]==table[j][i]);
			tmpfile.delete();
		} catch (DatasetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void testClear() {
		Dataset d = new Dataset(3);
		try{
			d.addRow(table[0]);
			d.addRow(table[1]);
		} catch (DatasetException e) {
			assertTrue("can't add regular rows", false);
		}
		d.clearRows();
		assertEquals("incorrect number of rows reported after clearing dataset", d.getNrows(), 0);
		assertEquals("incorrect number of columns reported after clearing dataset", d.getNcol(), 3);
	}

}
