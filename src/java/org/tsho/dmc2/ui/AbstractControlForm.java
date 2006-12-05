/*
 * AbstractControlForm.java
 *
 * Created on August 26, 2004, 3:04 PM
 */

package org.tsho.dmc2.ui;

import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComponent;


import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.core.util.SampleParser;
import org.tsho.dmc2.ui.components.GetVector;
/**
 *
 * Defines common methods to get and set data in the form of strings in the fields
 * of control forms. All Control forms inherit from this class.
 * @author  altair
 */
public abstract class AbstractControlForm extends JPanel{
    
    
    private Hashtable nameToFieldMap;
    private Vector formNames;
    private Vector names;//names of the fields by which they will be fetched from file
    private AbstractPlotComponent frame;
    private SamplesSupplier samplesSupplier;
    
    //protected boolean ignoreComboBoxChanges=false;
    
    
    /** Creates a new instance of AbstractControlForm */
    public AbstractControlForm(AbstractPlotComponent frame) {
        super();
        setOpaque(true);
        
        this.frame=frame;
        samplesSupplier=frame.getMainFrame().getSamplesSupplier();
        nameToFieldMap=new Hashtable();
        names=new Vector();
        formNames=new Vector();
        
    }
    
    
    
    /** gets the menu and updates the GUI */
    public void updateSamplesMenu(){
        frame.updateSamplesMenu();
    }
    
    public void addFormDataToSamples(String sampleName){
        String s = formDataToString(sampleName);
        samplesSupplier.addSample(s,sampleName,this.getFormType());
        samplesSupplier.flush();
        updateSamplesMenu();
    }
    
    
    
    /** sampleName is the item chosen from the program's Samples menu */
    public void loadFormData(String sampleName,String plotType){
            String ls=System.getProperty("line.separator");
            String s=  samplesSupplier.longString(sampleName,plotType);
            if (s!=null){
                if (s.length()>=ls.length()){
                    s=s.substring(0,s.length()-ls.length());
                    this.stringToFormData(s);
                    this.stringToFormData(s);//second time needed if JComboBox-es choice was changed
                }
            }
    }
    
    
    // adds "--" at the beginning of the string, so that it becomes a Lua language remark
    private String formDataToString(String sampleName){
        String s="--% ";// -- marks a comment line in Lua. % is added
        // to mark a comment line which refers to sample parameters.
        if (sampleName==null || sampleName.equals(""))
            s=s+" ft| "+getFormType()+" ";
        else
            s=s+" ft| "+getFormType()+" sn| "+sampleName+" ";
        for (int i=0;i<names.size();i++){
            s=s+"n| "+(String) names.elementAt(i)+" ";
            s=s+"d| "+ getData((String) names.elementAt(i))+" ";
        }
        String lineSeparator=System.getProperty("line.separator");
        return s+lineSeparator;
    }
    
    public String formDefaultName(){

        String s="";
        int l;
        if (names.size()>=8)
            l=8;
        else
            l=names.size();
        for (int i=0;i<l;i++){
            JComponent field=(JComponent) nameToFieldMap.get("#"+i);
            String formName =(String) formNames.elementAt(i);
            int strlen;
            if (formName.length()>=4)
                strlen=4;
            else
                strlen=formName.length();
            s=s+formName.substring(0,strlen)+": ";
            String str=getData("#"+i);
            s=s+str+"  ";
        }
        return s;
    }
    
    
    private String stringToFormData(String dataString){
        String sampleName=null;
        int pos=findNextColon(dataString,0);
        String name,data;
        //setFormType(findNextSubstring(dataString,pos));
        int ref;
        //contains --% at the start or not?
        if (SampleParser.find(dataString,1).equals("ft|"))
            ref=1;
        else
            ref=2;
        if ((SampleParser.find(dataString, ref+2)).equals("sn|")){
            sampleName=SampleParser.find(dataString, ref+3);
            pos=findNextColon(dataString,pos);
            pos=findNextColon(dataString,pos);
        }
        else
            pos=findNextColon(dataString,pos);
        
        while (pos>=0){
            name=findNextSubstring(dataString,pos);
            pos=findNextColon(dataString,pos);
            if (pos>=0){
                data=findSetOfSubstrings(dataString,pos);
                setData(name,data);
                pos=findNextColon(dataString,pos);
            }
        }
        return sampleName;
    }
    
    /** the actual control form must add name-field pairs (field can be an instance of JTextField,
     * for example) to the private Hashtable nameToFieldMap using this method. Only in this
     * case the value of the field would be saved as a "sample parameter".
     */
    protected void addEntry(String name, Object o){
        nameToFieldMap.put(name,o);
        names.addElement(name);
    }
    
    protected void addEntry(String label, String name, Object o){
        nameToFieldMap.put(name,o);
        names.addElement(name);
        formNames.addElement(label);
    }
    
    
    
    //assuming all fields are JTextFields
    protected void addEntry(String prefix,VariableItems vi){
        for (int i=0;i<vi.size();i++){
            Object [] c=vi.pair(i);
            addEntry(prefix+(String) c[0], (JTextField) c[1]);
        }
    }
    
    protected void addEntry(VariableItems vi){
        for (int i=0;i<vi.size();i++){
            Object [] c=vi.pair(i);
            addEntry((String) c[0], (JTextField) c[1]);
        }
    }
    
    protected void removeAllEntries(){
        names.clear();
        nameToFieldMap.clear();
        formNames.clear();
    }
    
   
    
    
    private String getData(String name){
        Object field=nameToFieldMap.get(name);
        if (field instanceof JTextField){
            JTextField tf=(JTextField) field;
            return tf.getText();
        }
        if (field instanceof JComboBox){
            JComboBox cb=(JComboBox) field;
            return (String) cb.getSelectedItem();
        }
        if (field instanceof VariableItems){
            String valuesString=" ";
            VariableItems vi=(VariableItems) field;
            String [] nameArray=vi.labelsArray();
            for (int i=0;i<nameArray.length;i++){
                if (vi.get(nameArray[i]) instanceof JTextField ){
                    JTextField tf=(JTextField) vi.get(nameArray[i]);
                    valuesString=valuesString+removeSpaces(tf.getText())+" ";
                }
            }
            return valuesString;
        }
        return "";
    }
    
    
    
    private void setData(String name, String data){
        Object field=nameToFieldMap.get(name);
        if (field instanceof GetVector){
            GetVector tf=(GetVector) field;
            tf.setValue(data);
            return;
        }
        if (field instanceof JTextField){
            JTextField tf=(JTextField) field;
            tf.setText(removeSpaces(data));
            return;
        }
        if (field instanceof JComboBox){
            JComboBox cb=(JComboBox) field;
            cb.setSelectedItem(removeSpaces(data));
            return;
        }
        if (field instanceof VariableItems){
            
            VariableItems vi=(VariableItems) field;
            String [] nameArray=vi.labelsArray();
            int pos=0;
            for (int i=0;i<nameArray.length;i++){
                if (vi.get(nameArray[i]) instanceof JTextField ){
                    JTextField tf=(JTextField) vi.get(nameArray[i]);
                    tf.setText(removeSpaces(findNextSubstring(data,pos)));
                    pos=positionAfterNextSubstring(data,pos);
                }
            }
            return;
        }
    }
    
    /*finds all strings delimited by spaces between position=pos
     *and the next colon (omitting the substring with colon).
     *The result is a string which is the concatenation
     *of substrings delimited by " " (e.g. " str1 str2 ").
     */
    private String findSetOfSubstrings(String dataString, int pos){
        if (dataString==null) return null;
        String res="";
        String s=findNextSubstring(dataString,pos);
        pos=positionAfterNextSubstring(dataString,pos);
        if (s.equals("") || s.indexOf('|')!=-1)
            return "";
        res=" "+s+" ";
        while (s!=""){
            s=findNextSubstring(dataString,pos);
            pos=positionAfterNextSubstring(dataString,pos);
            if (s!=null){
                if (s.indexOf('|')==-1)
                    res=res+" "+s+" ";
                else
                    s="";
            }
            else
                s="";
        }
        return res;
        
    }
    
    private String findNextSubstring(String dataString,int pos){
        return (String)(SampleParser.nextWord(dataString,pos))[0];
    }
    
    //finds next colon starting from position pos+1. If there is none returns -1.
    private int findNextColon(String dataString, int pos){
        if (dataString==null) return -1;
        if (pos<0 ||pos>=dataString.length())
            return -1;
        return dataString.indexOf('|',pos+1);
    }
    
    private int positionAfterNextSubstring(String dataString, int pos){
        if (dataString==null) return -1;
        if (pos<0 || pos>=dataString.length())
            return -1;
        int pos1=((Integer) SampleParser.nextWord(dataString,pos)[1]).intValue();
        if (pos1<=dataString.length()-1 && pos1>=0) {
            return pos1+1;
        }
        else
            return -1;
    }
    
    private String removeSpaces(String string){//needed?
        if (string==null) return null;
        String s="";
        for (int i=0;i<string.length();i++){
            if (string.charAt(i)!=' ')
                s=s+string.charAt(i);
        }
        return s;
    }
    
    
    protected abstract String getFormType();
   
    
}
