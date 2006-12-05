/**
 * 
 */
package org.tsho.dmc2.core.util;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates a typical dataset of doubles (i.e., a matrix),
 * with the ability of adding rows dynamically, and storing it to a standard csv file
 * @author antonio
 *
 */
public class Dataset implements DataObject {
	/**
	 * A vector of dataset rows
	 * For current number of rows, use rows.size()
	 * */
	private ArrayList rows;

	/**
	 * variable names
	 * */		
	private String[] varnames;
	
	/**
	 * Creates a Dataset of 'ncol' columns
	 * */
	public Dataset(int ncol) {
		init(ncol);
	}

	/**
	 * Creates a Dataset of variables 'varnames'
	 * */	
	public Dataset(String[] varnames){
		init(varnames);
	}
	
	/**
	 * Creates a Dataset from an already existent matrix of doubles
	 * */
	public Dataset(double[][] data) throws DatasetException {
		init(data[0].length);
		for(int i=0;  i<data.length; i++)
			this.addRow(data[i]);
	}
	
	/**
	 * Initialize internal structures
	 * */
	private void init(String[] varnames) {
		rows = new ArrayList();		
		this.varnames = varnames;
	}
	private void init(int ncol) {
		String tmp[] = new String[ncol];
		for(int i = 0; i<ncol; i++)
			tmp[i] = "V"+i;
		init(tmp);
	}
	
	/**
	 * Adds a row to the dataset. If incorrect number of rows, raises a DatasetException.
	 * */
	public void addRow(double[] newrow) throws DatasetException {
		if(newrow.length != varnames.length)
			throw new DatasetException("Invalid number of columns");
		rows.add(newrow);
	}
	
	/**
	 * Delete all rows from the dataset
	 * */
	public void clearRows() {
		rows.clear();
	}

	/**
	 * Get the id-th row
	 * */
	public double[] getRow(int id) {
		return (double[])rows.get(id);
	}
	
	/**
	 * Get number of dataset columns
	 * */
	public int getNcol() {
		return varnames.length;
	}
	
	/**
	 * Get number of dataset rows 
	 * */
	public int getNrows() {
		return rows.size();
	}
	
	/**
	 * Converts the dataset to an array of strings, each representing a line.
	 * This can be used to save the dataset on an external (csv) file
	 * */
	public String[] asLineStrings() {
		int nr = getNrows();
		String result[] = new String[nr];
		for(int i = 0; i<nr; i++)
			result[i] = asStringRecord(getRow(i));
		return result;
	}
	
	/**
	 * Convert double array to comma-separated string record
	 * */
	public static String asStringRecord(double[] drecord) {
		int nc = drecord.length;
		String res = "";
		for(int i = 1; i<nc; i++)
			res += (drecord[i-1] + ",");
		res += drecord[nc-1];
		return res;
	} 
	
	/**
	 * Save the dataset to the file 'f' using the string representation as given by
	 * 'this.asLineStrings()'
	 * */
	public void save(File f) throws IOException {
		PrintWriter fout = new PrintWriter(
				new BufferedWriter(	new FileWriter(f) ) 
				);
		String lines[] = asLineStrings();
		for(int i=0; i<(varnames.length-1); i++)
			fout.print(varnames[i]+",");
		if(varnames.length>0)
			fout.println(varnames[varnames.length-1]);
		for(int i=0; i<lines.length; i++)
			fout.println(lines[i]);
		fout.close();
	}
	
	/**
	 * Instantiates a dataset from the given csv file
	 * */
	static Dataset load(File f) throws DatasetException {
		BufferedReader in;
		Dataset result;
		String[] fields;
		boolean hasNames=false;
		try {
			in = new BufferedReader(new FileReader(f));
			fields = in.readLine().split(",");
//	Translate the header
			try{
				Double.valueOf(fields[0]);
				hasNames=false;
			} catch(Exception e) {
				hasNames=true;
			}
			if(hasNames)
				result = new Dataset(fields);
			else {
				result = new Dataset(fields.length);
				result.addRow(stringsToDoubles(fields));
			}
//  Add line by line			
			while(in.ready())
				result.addRow(stringsToDoubles(in.readLine().split(",")));
		} catch(Exception e) {
			throw new DatasetException("file error");
		}		
		return result;
	}
	
	private static double[] stringsToDoubles(String[] s) {
		double [] res = new double[s.length];
		for(int i=0; i<s.length; i++)
			res[i] = Double.valueOf(s[i]).doubleValue();		
		return res;
	}

}
