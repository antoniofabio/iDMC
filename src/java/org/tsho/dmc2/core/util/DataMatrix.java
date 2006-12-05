package org.tsho.dmc2.core.util;

import java.io.*;

public class DataMatrix implements DataObject {
	private int[] data;
	private int nc, nr;

	public DataMatrix() {
		
	}
	
	public DataMatrix(int[] data, int nr) {
		setContent(data, nr);
	}
	
	public void setContent(int[] data, int nr) {
		this.nr = nr;
		nc = data.length / nr;
		this.data = (int[]) data.clone();		
	}
	
	/**
	 * Saves the data in file 'f' in the following binary format:
	 * [int nr; int nc; char byte[1]; char byte[2]; ...; char byte[nr*nc]]
	 * */
	public void save(File f) throws IOException {
		if(data==null)
			throw new RuntimeException("No stored data to save");
		DataOutputStream out = new DataOutputStream(
				new BufferedOutputStream(
		          new FileOutputStream(f)));
		out.writeInt(nr);
		out.writeInt(nc);
		for(int i=0; i<data.length; i++)
			out.writeChar(data[i]);
		out.close();
	} 
}
