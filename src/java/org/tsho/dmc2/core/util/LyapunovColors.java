/*
 * LyapunovColors.java
 * Written by Alexei Grigoriev
 *
 */

package org.tsho.dmc2.core.util;

import java.awt.Color;
import java.util.Hashtable;
/**
 *
 * @author  altair
 */
public class LyapunovColors {
    
    private String [] description;
    private ColorArray colorArray;
    private Hashtable colorMap;
    private Hashtable descriptionMap;
    private int partitionCardinality;
    private int numberOfExps;
    
    
    /** Creates a new instance of LyapunovColors */
    public LyapunovColors(int n) {//n is the number of model parameters
        numberOfExps=n;
        int m=lyapunovPartition(n);
        partitionCardinality=m;
        colorArray=new ColorArray(m);
        description=new String[m];
        lyapunovDescription(n);
        colorMap=new Hashtable(n);
        descriptionMap=new Hashtable(n);
        createMaps();
    }
    
    public Color getColor(int zer,int pos, int neg){
        String key="0"+zer+"+"+pos+"-"+neg;
        return (Color) colorMap.get(key);
    }
    
    public Color getColor(int i){
        return colorArray.getColor(i);
    }
    
    public String getDescription(int zer,int pos, int neg){
        String key="0"+zer+"+"+pos+"-"+neg;
        return (String) descriptionMap.get(key);
    }
    
    public String getDescription(int i){
        return description[i];
    }
    
    public int getCardinality(){
        return partitionCardinality;
    }
    
    
    
    private int lyapunovPartition(int n){
        int count=0;
        for (int i=0;i<=n;i++){
            for (int j=0;j<=n-i;j++){
                count++;
            }
        }
        return count;
    }
    
    private void lyapunovDescription(int n){
        int count=0;
        for (int i=0;i<=n;i++){
            for (int j=0;j<=n-i;j++){
                description[count] =" "+i+" zero,"+j+" positive,"+(n-i-j)+"negative ";
                count++;
            }
        }
    }
    
    private void createMaps(){
        int count=0;
        int n=numberOfExps;
        for (int i=0;i<=n;i++){
            for (int j=0;j<=n-i;j++){
                String key="0"+i+"+"+j+"-"+(n-i-j);
                colorMap.put(key, colorArray.getColor(count));
                descriptionMap.put(key,description[count]);
                count++;  
            }
        }
    }
    
    public static void main(String[] args){
        LyapunovColors l=new LyapunovColors(3);
        Color c=l.getColor(1,1,1);
        int b=c.getBlue();
        int r=c.getGreen();
    }
    
    
    
}
