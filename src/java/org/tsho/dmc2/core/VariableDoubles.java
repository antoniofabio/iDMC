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

// TODO add a double[] toArray() method (look LuaIterator)
// TODO add a fill(double[]) method (look DmcBifurcation)

/**
 * VariableItems is a class that contains label/value keys.
 * The label are univocally defined (no diplicates) and
 * the pairs are label lexicographically ordinate. VariableItems with
 * the same labels are said to be "of the same flavour".
 *
 *  @author Daniele Pizzoni <auouo@tin.it>
 */
public class VariableDoubles implements Cloneable {
    
    private VariableItems items;
    
    public VariableDoubles(String[] l) {
        items = new VariableItems(l);
        
        /* initialize to 0.0 */
        VariableItems.Iterator i = items.iterator();
        while (i.hasNext()) {
            items.put(i.nextLabel(), new Double(0.0));
        }
    }
    
    /**
     * @param s
     * @return
     */
    public double get(String s) {
        return ((Double)items.get(s)).doubleValue();
    }
    
    /**
     * @return
     */
    public String[] labelsArray() {
        return items.labelsArray();
    }
    
    /**
     * @param l
     * @param val
     */
    public void put(String l, double val) {
        items.put(l, new Double(val));
    }
    
    /**
     * @param v
     */
    public void putAll(VariableDoubles v) {
        items.putAll(v.items.valuesArray());
    }
    
    public boolean containsLabel(String s) {
        return items.containsLabel(s);
    }
    
    /**
     * @return
     */
    //    public Double[] valuesArray() {
    //        Double[] d = new Double[items.size()];
    //        System.arraycopy(items.valuesArray(), 0, d, 0, items.size());
    //        return d;
    //    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (sameFlavour(obj) == false)
            return false;
        
        VariableDoubles v = (VariableDoubles)obj;
        
        VariableItems.Iterator i = items.iterator();
        while (i.hasNext()) {
            if (get(i.nextLabel()) != v.get(i.label()))
                return false;
        }
        
        return true;
    }
    
    public interface Iterator {
        public boolean hasNext();
        public String nextLabel();
        public double nextValue();
        public String label();
        public double value();
    }
    
    /**
     * @return
     */
    public Iterator iterator() {
        return new Iterator() {
            VariableItems.Iterator itemsIterator = items.iterator();
            public boolean hasNext() {
                return itemsIterator.hasNext();
            }
            public String nextLabel() {
                return itemsIterator.nextLabel();
            }
            public double nextValue() {
                return ((Double)itemsIterator.nextValue()).doubleValue();
            }
            public String label() {
                return itemsIterator.label();
            }
            public double value() {
                return ((Double)itemsIterator.value()).doubleValue();
            }
        };
    }
    
    /**
     * Compares the specified object with this VariableDoubles. Returns
     * <tt>true</tt> if the given object is a VariableDouble <b>or</b>
     * a VariableItems with the same labels.
     *
     * @param o object to be compared with this object.
     * @return <tt>true</tt> if the specified object is the same flavour
     *         of this object.
     */
    public boolean sameFlavour(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        
        if (o instanceof VariableDoubles)
            return items.sameFlavour(((VariableDoubles)o).items);
        if (o instanceof VariableItems)
            return items.sameFlavour(((VariableItems)o));
        else
            return false;
    }
    
    /**
     * @return
     */
    public int size() {
        return items.size();
    }
    
    public Object clone() {
        
        VariableDoubles clone;
        try {
            clone = (VariableDoubles)super.clone();
        }
        catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        clone.items = (VariableItems)items.clone();
        return clone;
    }
    
    //    /**
    //     * Crates a VariableItems object of the same flavour of this.
    //     *
    //     * @return The created VariableItems.
    //     */
    //    public VariableItems createVariableItems() {
    //        return new VariableItems(items.labelsArray());
    //    }
    
    
    
    public void dispose() {
        items.dispose();
        items = null;
    }
    
    public static void printOut(VariableDoubles v) {
        VariableDoubles.Iterator i = v.iterator();
        while (i.hasNext()) {
            System.out.print("label: " + i.nextLabel());
            System.out.println(" value: " + i.value());
        }
    }
    
    public static double[] toArray(VariableDoubles v) {
        VariableDoubles.Iterator iterator = v.iterator();
        int i = 0;
        double[] d = new double[v.size()];
        
        while (iterator.hasNext()) {
            d[i++] = iterator.nextValue();
        }
        
        return d;
    }
    
    public static int indexOf(VariableDoubles v, String label) {
        VariableDoubles.Iterator iterator = v.iterator();
        
        int i = 0;
        while (iterator.hasNext()) {
            if (label.equals(iterator.nextLabel())) {
                return i;
            }
            i++;
        }
        
        return -1;
    }
    
    
    public static void fill(VariableDoubles v, double[] array) {
        if (v.size() != array.length) throw new IllegalArgumentException();
        
        VariableDoubles.Iterator k = v.iterator();
        int l = 0;
        while (k.hasNext()) {
            v.put(k.nextLabel(), array[l++]);
        }
    }
    
}
