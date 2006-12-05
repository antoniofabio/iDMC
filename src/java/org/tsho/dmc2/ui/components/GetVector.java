/***************************************************************************
 * DMC, Dynamical Model Cruncher, simulates and analyzes low-dimensional
 * dynamical systems
 * Copyright (C) 2002   Alfredo Medio and Giampaolo Gallo
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * For a copy of the GNU General Public, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Contact:  Alfredo Medio   Medio@unive.it
 *
 * adapted by Daniele Pizzoni <auouo@tin.it>
 *
 ***************************************************************************/
package org.tsho.dmc2.ui.components;
import java.awt.Color;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jfree.data.Range;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.core.util.SampleParser;


/**
 *  @author Giampaolo Gallo
 *  @author Daniele Pizzoni <auouo@tin.it>
 */
public final class GetVector extends JTextField implements Serializable {
    protected double[] values;
    protected int numberOfValues;
    
    protected boolean valid;
    protected boolean outOfRange;
    protected boolean ignoreValid;
    
    protected String name;
    
    public GetVector(String name,int n) {
        super();
        this.name=name;
        numberOfValues=n;
        init();
    }
    
    
    private void init() {
        valid = false;
        ignoreValid = false;
        outOfRange = false;
        
    }
    
    
    public void setValue(double [] values) {
        String string="";
        for (int i=1;i<values.length;i++){
            this.values[i]=values[i];
            string=string+values[i]+" ";
        }
        setText(string);
    }
    
    public void setValue(String values) {
        setText(values);
    }
    
    public double[] getValue() throws InvalidData {
        //parse and if erroneous throw exception with error description
        int n=numberOfValues;
        String data=this.getText();
        double [] result=new double[n];
        if (!valid && !ignoreValid) {
            String s;
            for (int i=0;i<n;i++){
                s=SampleParser.find(data,i+1);
                if (s==null) throw new InvalidData(name+": "+"too few values");
                try{
                    result[i]=Double.valueOf(s).doubleValue();
                }
                catch(NumberFormatException e){
                    throw new InvalidData(name+": "+"one of values not a number");
                }
            }
            s=SampleParser.find(data,n+1);
            if (s!=null)
                throw new InvalidData(name+": "+"too many values");
        }
        return result;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    
    /**
     * @return
     */
    public boolean isIgnoreValid() {
        return ignoreValid;
    }
    
    /**
     * @param b
     */
    public void setIgnoreValid(boolean b) {
        ignoreValid = b;
    }
}
