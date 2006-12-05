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
package org.tsho.dmc2.ui;

import java.awt.Component;

import javax.swing.JPanel;

import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.ui.components.GetFloat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class FormHelper {
    
    public static String JGOODIES_SHORT_COLUMN_SPECS =
    "l:11dlu:n, r:max(30dlu;pref):g, l:4dlu:n, c:32dlu:n,  l:9dlu:n";
    
    public static String JGOODIES_LONG_COLUMN_SPECS =
    "l:11dlu:n, r:max(30dlu;pref):g, l:4dlu:n, r:17dlu:n, l:3dlu:n, r:13dlu:n, l:9dlu:n";
    
    public static final int FIELD_LENGTH = 6;
    
    private FormHelper() {
        super();
    }
    
    public interface FormBuilder {
        void addTitle(String s);
        void addSubtitle(String s);
        void addRow(String label, Component field);
        void addRow(String label, Component field, Component vField);
        void addGap();
        JPanel getPanel();
    }
    
    // Normal builder
    
    private static class FormBuilderImpl implements FormBuilder {
        JPanel panel;
        DefaultFormBuilder builder;
        AbstractControlForm controlForm=null;
        int id=0;//field identifier
        
        protected FormBuilderImpl() {
        }
        
        protected FormBuilderImpl(AbstractControlForm controlForm, boolean debug){
            this.controlForm=controlForm;
            controlForm.removeAllEntries();
            FormLayout layout = new FormLayout(JGOODIES_LONG_COLUMN_SPECS, "");
            
            if (debug) {
                panel = new FormDebugPanel();
                builder = new DefaultFormBuilder(panel, layout);
            } else {
                panel = new JPanel();
                panel.setOpaque(true);
                builder = new DefaultFormBuilder(panel, layout);
            }
            
            builder.setDefaultDialogBorder();
            builder.setLeadingColumnOffset(1);
        }
        
        FormBuilderImpl(boolean debug) {
            FormLayout layout = new FormLayout(JGOODIES_LONG_COLUMN_SPECS, "");
            
            if (debug) {
                panel = new FormDebugPanel();
                builder = new DefaultFormBuilder(panel, layout);
            } else {
                panel = new JPanel();
                panel.setOpaque(true);
                builder = new DefaultFormBuilder(panel, layout);
            }
            
            builder.setDefaultDialogBorder();
            builder.setLeadingColumnOffset(1);
        }
        
        public void addTitle(String s) {
            builder.appendSeparator(s);
            builder.nextLine();
            builder.appendRow(builder.getLineGapSpec());
            builder.nextLine();
        }
        
        public void addRow(String label, Component field) {
            builder.append(label, field, 3);
            if (controlForm!=null){
                String stringId="#"+id;
                id++;
                controlForm.addEntry(label,stringId,field);
            }
            builder.nextLine();
        }
        
        public void addRow(String label, Component field, Component vField) {
            builder.append(label, field, vField);
            builder.nextLine();
        }
        
        public void addSubtitle(String s) {
            CellConstraints cc = new CellConstraints();
            builder.appendRow(builder.getLineGapSpec());
            builder.nextLine();
            
            builder.appendRow(new RowSpec("c:p:n"));
            builder.addSeparator(s, cc.xywh(2, builder.getRow(),
            5, 1, "fill, fill"));
            builder.nextLine();
            builder.appendRow(builder.getLineGapSpec());
            builder.nextLine();
        }
        
        public void addGap() {
            builder.appendRow(builder.getLineGapSpec());
            builder.nextLine();
        }
        
        public JPanel getPanel() {
            return panel;
        }
    }
    
    public static FormBuilder controlFormBuilder(boolean debug) {
        return new FormBuilderImpl(debug);
    }
    
    
     public static FormBuilder controlFormBuilder(AbstractControlForm controlForm,boolean debug) {
        return new FormBuilderImpl(controlForm,debug);
    }
    
    // "Variation" builder
    
    //    private static class VFormBuilderImpl extends FormBuilderImpl {
    //
    //        VFormBuilderImpl(boolean debug) {
    //            FormLayout layout = new FormLayout(JGOODIES_LONG_COLUMN_SPECS, "");
    //
    //            if (debug) {
    //                panel = new FormDebugPanel();
    //                builder = new DefaultFormBuilder(panel, layout);
    //            } else {
    //                panel = new JPanel();
    //                panel.setOpaque(true);
    //                builder = new DefaultFormBuilder(panel, layout);
    //            }
    //
    //            builder.setDefaultDialogBorder();
    //            builder.setLeadingColumnOffset(1);
    //        }
    //
    //        public void addRow(String label, Component field) {
    //            builder.append(label, field, 3);
    //            builder.nextLine();
    //        }
    //
    //        public void addRow(String label, Component field, Component vField) {
    //            builder.append(label, field, vField);
    //            builder.nextLine();
    //        }
    //
    //        public void addSubtitle(String s) {
    //            CellConstraints cc = new CellConstraints();
    //            builder.appendRow(builder.getLineGapSpec());
    //            builder.nextLine();
    //
    //            builder.appendRow(new RowSpec("c:p:n"));
    //            builder.addSeparator(s, cc.xywh(2, builder.getRow(),
    //                                 5, 1, "fill, fill"));
    //            builder.nextLine();
    //            builder.appendRow(builder.getLineGapSpec());
    //            builder.nextLine();
    //        }
    //    }
    //
    //    public static FormBuilder controlVFormBuilder(boolean debug) {
    //        return new VFormBuilderImpl(debug);
    //    }
    
    
    // helpers
    
    public static VariableItems createFields(String[] names, String label) {
        VariableItems.Iterator i;
        VariableItems fields = new VariableItems(names);
        i = fields.iterator();
        while (i.hasNext()) {
            String text = "\"" + i.nextLabel() + "\" " + label;
            GetFloat field = new GetFloat(text, FormHelper.FIELD_LENGTH);
            fields.put(i.label(), field);
        }
        return fields;
    }
    
    public static VariableDoubles collectFieldValues(
    VariableItems fields) throws InvalidData {
        
        VariableDoubles result;
        VariableDoubles.Iterator i;
        
        result = new VariableDoubles(fields.labelsArray());
        i = result.iterator();
        
        while (i.hasNext()) {
            String label = i.nextLabel();
            result.put(label, ((GetFloat) fields.get(label)).getValue());
        }
        
        return result;
    }
    
    public static void setFieldValues(
    VariableItems fields, VariableDoubles init) {
        
        VariableItems.Iterator i = fields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setValue(init.get(i.label()));
        }
    }
}
