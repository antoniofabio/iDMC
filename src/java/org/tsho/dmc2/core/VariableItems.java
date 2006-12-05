/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>.
 *
 *
 *
 * The software program was developed within a research project financed
 * by the Italian Ministry of Universities, the Universities of Udine and
 * Ca'Foscari of Venice, the Friuli-Venezia Giulia Region.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.tsho.dmc2.core;

// import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * VariableItems is a class that contains label/value keys. 
 * The label are univocally defined (no duplicates). Different VariableItems
 * with the same labels are said to be "of the same flavour".
 * 
 *  @author Daniele Pizzoni <auouo@tin.it>
 */
public class VariableItems implements Cloneable {

    private String[] labels;
    private Object[] values;

    /**
     * Constructs a VariableItems object from the specified label array.
     * 
     * @param l the array of strings containing the label strings.
     * @throws NullPointerException if the specified array is null.
     * @throws IllegalArgumentException if the specified array is empty. 
     */
    public VariableItems(String[] l) {
        if (l.length == 0) {
            throw new IllegalArgumentException();
        }

        int lenght = l.length;
        labels = new String[lenght];
        values = new Object[lenght];

        for (int i = 0; i < lenght; i++) {
            this.labels[i] = l[i];
        }
        
        // Arrays.sort(labels);
    }

    /**
     * Returns the number of pairs of the VariableItems object.
     * 
     * @return the number of pairs of the VariableItems object.
     */
    public int size() {
        return labels.length; 
    }
    
//    /**
//     * Sets the value of the lable/value pair at the specified index.
//     * 
//     * @param idx index of value to be stored.
//     * @param val value to be stored at the specified position.
//     * @throws IndexOutOfBoundsException if index out of bounds 
//     */
//    public void setValue(int idx, double val) {
//        values[idx] = val; 
//    }
    
    /**
     * Sets the value of label/value pair corresponding to the specified 
     * label.
     * 
     * @param l string defining the label of the pair.
     * @param val value to be stored.
     * @throws IllegalArgumentException if the specified string is not
     *         found on any pair of the VariableItem object.  
     */
    public void put(String l, Object val) {
        // int index = Arrays.binarySearch(labels, l);
        int index = search(l); 
        
        if (index < 0)
            throw new IllegalArgumentException("Label not found");

        values[index] = val;
    }

    /**
     * Set the values of all the pairs from the specified array.
     * 
     * @param v array containig the values to be stored.
     * @throws IllegalArgumentException if the size of the specified
     *         array does not match the number of pairs in the object.
     */
    protected void putAll(Object[] v) {
        if (v.length != labels.length) {
            throw new IllegalArgumentException("Size mismatch.");
        }
        
        for (int i = 0; i < size(); i++) {
            values[i] = v[i];
        }
    }

    /**
     * Set the values of all the pairs from the specified VariableItems.
     * 
     * @param v array containing the values to be stored.
     * @throws IllegalArgumentException if the specified VariableItems
     *         is not the same flavour of this object.
     */
    public void putAll(VariableItems v) {
        if (!v.sameFlavour(this)) {
            throw new IllegalArgumentException("NotSameFlavour.");
        }

        for (int i = 0; i < size(); i++) {
            //values[i] = v.values[i];
            put(v.labels[i], v.values[i]);
        }
    }
    
    public boolean containsLabel(String s) {
        // int index = Arrays.binarySearch(labels, s);
        int index = search(s);
        
        if (index >= 0) {
            return true;
        }

        return false;
    }


    /**
     * Returns the label of the label/value pair at the given index.
     * 
     * @param idx index specifing the pair.
     * @return label of the pair defined by index.
     * @throws IndexOutOfBoundsException if index is out of range. 
     */
//    public String getLabel(int idx) {
//        return labels[idx];
//    }

    /**
     * Returns an array of the labels of all the pairs of the
     * Object, in the same order.
     *    
     * @return array of all the labels of the object. 
     */
    public String[] labelsArray() {
        String[] ret = new String[labels.length];
        
        for (int i = 0; i < labels.length; i++) {
            ret[i] = labels[i];
        }
        
        return ret;
    }

    /**
     * Returns the value of the pair at the specified index.
     * 
     * @param idx index of the pair.
     * @return value of the pair at the specified index.
     */
//    public double getValue(int idx) {
//        return values[idx];
//    }

    /**
     * Returns the value of the pair with the specified label.
     * 
     * @param s label defining the pair.
     * @return the value corresponding to the label specified.
     * @throws IllegalArgumentException if the label was not found.
     */
    public Object get(String s) {
        // int index = Arrays.binarySearch(labels, s);
        int index = search(s);
        
        if (index >= 0) {
            return values[index];
        }

        throw new IllegalArgumentException();
    }
    
    /**
     * Returns an array of the values of all the pairs of the
     * Object, in the same order.
     *    
     * @return array of all the values of the object. 
     */
    public Object[] valuesArray() {
        Object[] ret = new Object[labels.length];
        
        for (int i = 0; i < size(); i++) {
            ret[i] = values[i];
        }

        return ret;
    }

    /**
     * Searches the VariableItems object for the pair with the specified
     * label returning its index.
     * 
     * @param s label to search
     * @return index of the requested pair if found; otherwise -1;  
     */
//    public int getIndex(String s) {
//        int index = Arrays.binarySearch(labels, s);
//        
//        if (index >= 0) {
//            return index;
//        }
//
//        return -1; 
//    }

    /**
     * Compares the specified object with this VariableItems. Returns
     * <tt>true</tt> if the given object is also a VariableItems with the
     * same labels. 
     *
     * @param o object to be compared with this object.
     * @return <tt>true</tt> if the specified object is the same flavour
     *         of this object.
     */
    public boolean sameFlavour(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VariableItems) || o == null)
            return false;

        VariableItems obj = (VariableItems)o;

        if (obj.size() != this.size())
            return false;

        for(int i = 0; i < this.size(); i++) {
            // if (obj.labels[i] != labels[i])
            if (search(obj.labels[i]) < 0)
                return false;
        }
        
        return true;
    }

    /**
     * Compares the specified object with this VariableItems for equality.
     * Returns <tt>true</tt> if the given object is VariableItems of the
     * same flavour and contains the same <bold>object references</bold>.
     *
     * @param o object to be compared for equality with this object.
     * @return <tt>true</tt> if the specified object is equal to this 
     *         VariableItems object.
     */
    public boolean equals(Object o) {
        if (sameFlavour(o) == false)
            return false;

        int idx;
        for(int i = 0; i < this.size(); i++) {
            idx = ((VariableItems)o).search(labels[i]);

            //Modification: 2 lines below are commented
            //assert idx >= 0;
            //assert ((VariableItems)o).labels[idx] == labels[i];

            if (!values[i].equals(((VariableItems)o).values[idx])) {
                 return false;           
            }
//            if (values[i] !=  ((VariableItems)o).values[idx]) {
//                return false;
//            }
        }

        return true;
    }

    /**
     * Create a duplicate of this object with the same flavour and values.
     * 
     * @return the duplicate object.
     */
    public Object clone() {
        VariableItems v = new VariableItems(labels);

        v.putAll(this);
        return v;
    }

    public void  dispose() {
        for (int i = 0; i < size(); i++) {
            labels[i] = null;
            values[i] = null;
        }
        
        labels = null;
        values = null;
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

//    String getLabel(int index) {
//        return labels[index];
//    }
//
//    int getIndex(String l) {
//        // int index = Arrays.binarySearch(labels, l);
//        int index = search(l);
//        
//        if (index >= 0) {
//            return index;
//        }
//
//        return -1; 
//    }

    public interface Iterator {
        public boolean hasNext();
        public String nextLabel();
        public Object nextValue();
        public String label();
        public Object value();
    }

    public Iterator iterator() {
        return new Iterator() {
            int i = 0;
            public boolean hasNext() {
                return i < size();
            }
            public String nextLabel() {
                if (i < size())
                    return labels[i++];
                else throw new NoSuchElementException();
            }
            public Object nextValue() {
                if (i < size())
                    return values[i++];
                else throw new NoSuchElementException();
            }
            public String label() {
                return labels[i - 1];
            }
            public Object value() {
                return values[i - 1];
            }
        };
    }
    
    private int search(String s) {
        for (int i = 0 ; i < labels.length; i++) {
            if (labels[i].equals(s))
                return i;
        }
        return -1;
    }
    
    public static int indexOf(VariableItems v, String label) {
        VariableItems.Iterator iterator = v.iterator();

        int i = 0;
        while (iterator.hasNext()) {
            if (label.equals(iterator.nextLabel())) {
                return i;
            }
            i++;
        }

        return -1;
    }
    
    public Object [] pair(int i){
        if (i>=labels.length || i<0){
            return null;
        }
        Object [] result=new Object[2];
        result[0]=labels[i];
        result[1]=values[i];
        return result;
    }

}
