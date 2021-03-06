/**
 * 
 */
package org.tsho.dmc2.core.chart;
import java.awt.Color;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Antonio
 * 
 * Encapsulates basin/attractor colors settings. 
 * Automatically store/retrieve them, and has methods for getting/setting 
 * singular colors
 */
public class ColorSettings implements Cloneable {
	private Preferences prefs; 
	private Class dCl;
	
	/**
	 * 'empty' and 'infinity' colors
	 * */
	private int cEmpty, cInfinity;
	
	public static final int defaultEmpty = Color.black.getRGB();
	public static final int defaultInfinity = Color.white.getRGB();

	/**
	 * Encapsulates the full colors sequence
	 * */
	private Vector data;
	/** Default pairs colors. Can be obtained with the following R code:
 	npairs <- 16
	colors <- gsub("#","0x",rainbow(npairs*2))
	for(i in 1:npairs)
		cat("\t{ ",colors[i*2-1], " , ", colors[i*2], " },\n")
	 * */
	public static final int[][] defaultPairs = new int[][] {
        {-43776, -16640},
        {-27392, -256},
        {-16711935, -5308672},
        {-16772097, -16729857},
        {-16730113, 65535},
        {-8584961, -65282}};
	
	public ColorSettings(Class dCl) {
		this.dCl = dCl;
		prefs = Preferences.userNodeForPackage(dCl);
		load();
	}

	/**
	 * add color pair
	 * */
	public void addPair(int[] pair) {
		data.add(pair);
	}

	/**
	 * set a specific colors pair
	 * */
	public void setPair(int[] pair, int index) {
		data.set(index, pair);
	}
	
	/**
	 * Remove a specific colors pair
	 * */
	public void removePair(int index) {
		data.remove(index);
	}
	
	/**
	 * get color pair at given index
	 * */
	public int[] getPair(int index) {
		return (int[]) data.get(index);
	}
	
	/**
	 * get empty color
	 * */
	public int getEmpty() {
		return cEmpty;
	}
	
	public void setEmpty(int color) {
		cEmpty = color;
	}
	
	public void setInfinity(int color) {
		cInfinity = color;
	}	
	
	/**
	 * get infinity color
	 * */
	public int getInfinity() {
		return cInfinity;
	}
	
	/**
	 * Get full colors data as a single index array of integers
	 * */
	public int[] getArray() {
		int [] ans = new int[2*data.size()+2];
		ans[0] = cEmpty;
		ans[1] = cInfinity;
		for(int i=0; i<data.size(); i++) {
			ans[2*i+2] = ((int[]) data.get(i))[0];
			ans[2*i+3] = ((int[]) data.get(i))[1];			
		}
		return ans;
	}

	/**
	 * Store settings
	 * */
	public void save() {
		prefs.putInt("empty", cEmpty);
		prefs.putInt("infinity", cInfinity);
		prefs.putInt("numPairs", data.size());
		int[] tmpPair;
		for(int i=0; i<data.size(); i++) {
			tmpPair = (int[]) data.get(i);
			prefs.putInt("p"+i+"b", tmpPair[0]);
			prefs.putInt("p"+i+"a", tmpPair[1]);
		}
	}
	
	/**
	 * Retrieve settings
	 * */
	public void load() {
		cEmpty = prefs.getInt("empty", defaultEmpty);
		cInfinity = prefs.getInt("infinity", defaultInfinity);
		int numPairs = prefs.getInt("numPairs", defaultPairs.length);
		data = new Vector();
		int[] tmpPair = new int[2];
		for(int i=0; i<numPairs; i++) {
			tmpPair[0] = prefs.getInt("p"+i+"b", 
					defaultPairs[Math.min(i,defaultPairs.length-1)][0]);
			tmpPair[1] = prefs.getInt("p"+i+"a", 
					defaultPairs[Math.min(i,defaultPairs.length-1)][1]);
			data.add( (int[]) tmpPair.clone() );
		}		
	}
	
	/**
	 * Reset settings (and saves them)
	 * */
	public void reset() {
		cEmpty = defaultEmpty;
		cInfinity = defaultInfinity;
		int numPairs = defaultPairs.length;
		int [] tmpPair = new int[2];
		data = new Vector();
		for(int i=0; i<numPairs; i++){
			tmpPair[0] = defaultPairs[Math.min(i,defaultPairs.length-1)][0];
			tmpPair[1] = defaultPairs[Math.min(i,defaultPairs.length-1)][1];
			data.add( (int[]) tmpPair.clone() );
		}
		prefs = Preferences.userNodeForPackage(dCl);
		save();
		load();
	}
	
	public Vector getPairsVector() {
		return data;
	}
	
	private void setPairsVector(Vector data) {
		this.data = data;
	}
	
	public Object clone() {
		ColorSettings ans = new ColorSettings(dCl);
		ans.setEmpty(cEmpty);
		ans.setInfinity(cInfinity);
		ans.setPairsVector((Vector) data.clone());
		return ans;
	}
}
