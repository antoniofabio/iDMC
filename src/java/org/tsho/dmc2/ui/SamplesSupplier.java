/*
 * SamplesSupplier.java
 *
 * Created on August 28, 2004, 7:09 PM
 */

package org.tsho.dmc2.ui;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Vector;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tsho.dmc2.core.util.SampleParser;

/**
 * The methods of this class may corrupt a binary file
 * if it is passed as an argument. Care should be taken to pass
 * only text files as an argument (for example,verifying the
 * file extension).
 * @author  altair
 */
public class SamplesSupplier {
    
    private File modelFile;
    private Vector buffer;//buffer which holds the "sample" lines read from the file
    private Vector shortBuffer;
    private Vector menu;
    private Vector menuIndex;
    private HashSet setOfShorts;
    private String restOfFile;
    private String currentContents;
    private String lineSeparator=System.getProperty("line.separator");
    
    /** Creates a new instance of SamplesSupplier:
     * plotType is obtained from the method getFormType of
     * AbstractControlForm.
     */
    public SamplesSupplier(File modelFile) {
        
        this.modelFile=modelFile;
        setOfShorts=new HashSet();
        restOfFile="";
        currentContents="";
        buffer=new Vector();
        shortBuffer=new Vector();
        menu=new Vector();
        menuIndex=new Vector();
        
        try{
            BufferedReader is=new BufferedReader(new FileReader(modelFile));
            String line=" ";
            String line1="";
            try{
                while (line!=null){
                    line=is.readLine();
                    if (line!=null && !line.equals("--@@")){
                        line1=line+lineSeparator;//restoring newline
                        
                        if (line.substring(0, min(line.length(),3) ).equals("--%")){
                            String s=new String(line1);
                            buffer.add(s);//
                            String s1;
                            
                            s1=" ft: "+SampleParser.find(line,3)+" sn: "+SampleParser.find(line,5);
                            shortBuffer.addElement(s1);
                            setOfShorts.add(s1);
                        }
                        else{
                            restOfFile=restOfFile+line1;
                        }
                        currentContents=currentContents+line1;
                    }
                }
            }
            catch(IOException e){
                System.out.println(e);
            }
            is.close();
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
    
    
    public void addSample(String s, String sampleName, String plotType){
        if (!buffer.contains(s)){
            if (s==null || s.length()<3)
                s="--%";
            else{
                if (!s.substring(0,3).equals("--%"))
                    s="--%"+s;
            }//this to avoid corruption of .lua model files
            
            String s1;
            
            s1=" ft: "+plotType+" sn: "+sampleName;
            String s2;
            while (this.setOfShorts.contains(s1)){
                s2=JOptionPane.showInputDialog("Name already exists, choose another:");
                if (s2!=null){
                    s2=SampleParser.toWord(s2);
                    s1=" ft: "+plotType+" sn: "+s2;
                }
                else{
                    s1="";
                }
            }
            if (SampleParser.numberOfWords(s1)==4){
                buffer.addElement(s);
                shortBuffer.addElement(s1);
                setOfShorts.add(s1);
            }
        }
    }
    
    public void removeSample(String sampleName,String plotType){
        String t;
        if (sampleName!=null){
            int p=menu.indexOf(sampleName);
            int plong= ((Integer) menuIndex.elementAt(p)).intValue();
            buffer.removeElementAt(plong);
            shortBuffer.removeElementAt(plong);
            setOfShorts.remove(" ft: "+plotType+" sn: "+sampleName);
            this.flush();
        }
    }
    
    public Vector getShortBuffer(){
        return shortBuffer;
    }
    
    private void buildMenu(String plotType){
        menu.clear();
        menuIndex.clear();
        for (int i=0;i<shortBuffer.size();i++){
            String s=(String) shortBuffer.elementAt(i);
            if (s.length()>5+plotType.length()){
                if (s.substring(0,5+plotType.length()).equals(" ft: "+plotType)){
                    if (SampleParser.find(s,3).equals("sn:")){
                        menu.addElement(SampleParser.find(s,4));
                        menuIndex.addElement(new Integer(i));
                    }
                    else{
                        String s1=s.substring(5+plotType.length(),s.length());
                        menu.addElement(s1);
                        menuIndex.addElement(new Integer(i));
                    }
                }
            }
        }
    }
    
    /** returns a vector with names of samples corresponding to plotType given*/
    public Vector menuSamples(String plotType){
        buildMenu(plotType);
        return menu;
    }
    
    
    public String [] supplyMenu(){
        String [] result=new String[menu.size()];
        for (int i=0; i<menu.size();i++){
            result[i]=SampleParser.toString((String) menu.elementAt(i));
        }
        return result;
    }
    
    public Vector getBuffer(){
        return buffer;
    }
    
    public String longString(String sampleName, String plotType){
        String s;
        
        int pos = shortBuffer.indexOf(" ft: "+plotType+" sn: "+sampleName);
        return (String) buffer.elementAt(pos);
        
    }
    
    public void flush(){
        try{
            BufferedWriter os=new BufferedWriter(new FileWriter(modelFile));
            String contents="";
            for (int i=0;i<buffer.size();i++){
                contents=contents+buffer.elementAt(i);
            }
            contents=contents+"--@@"+this.lineSeparator;
            contents=contents+restOfFile;
            os.write(contents);
            currentContents=contents;
            os.flush();
            os.close();
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
    
    private int min(int a ,int b){
        if (a<b) return a; else return b;
    }
    
}
