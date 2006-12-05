/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
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
package org.tsho.dmc2.ui.coweb;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.data.Range;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.ui.AbstractControlForm;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.FormHelper;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.components.GetFloat;
import org.tsho.dmc2.ui.components.GetInt;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public final class CowebControlForm2 extends AbstractControlForm {
    
    public static final int TYPE_SHIFTED = 1;
    public static final int TYPE_COWEB = 2;
    
    private VariableItems varFields;
    private VariableItems parFields;
    private GetInt orderField;
    private GetInt transientsField;
    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    
    private int type;
    
    public CowebControlForm2(final Model model, final int type, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);
        
        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        varFields = FormHelper.createFields(model.getVarNames(), "value");
        
        orderField = new GetInt(
        "order", FormHelper.FIELD_LENGTH,
        new Range(1, Integer.MAX_VALUE));
        
        transientsField = new GetInt(
        "transients", FormHelper.FIELD_LENGTH,
        new Range(0, Integer.MAX_VALUE));
        
        lowerHRangeField = new GetFloat(
        "lower horizontal range", FormHelper.FIELD_LENGTH);
        upperHRangeField = new GetFloat(
        "upper horizontal range", FormHelper.FIELD_LENGTH);
        lowerVRangeField = new GetFloat(
        "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
        "upper vertical range", FormHelper.FIELD_LENGTH);
        
        this.type = type;
        
        FormLayout layout = new FormLayout("f:p:n", "");
        
        setLayout(layout);
        layout.appendRow(new RowSpec("f:p:n"));
        add(createPanel(), new CellConstraints(1, 1));
    }
    
    private JPanel createPanel() {
        
        FormHelper.FormBuilder builder;
        builder = FormHelper.controlFormBuilder(this,false);
        
        VariableItems.Iterator i;
        
        if (type == TYPE_COWEB) {
            builder.addTitle("Initial Value");
            i = varFields.iterator();
            while (i.hasNext()) {
                builder.addRow(i.nextLabel(), (Component) i.value());
            }
        }
        
        builder.addTitle("Parameters");
        i = parFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }
        
        builder.addTitle("Algorithm");
        builder.addRow("order", orderField);
        
        if (type == TYPE_COWEB) {
            builder.addRow("transients", transientsField);
        }
        
        builder.addTitle("Ranges");
        builder.addSubtitle("horizontal");
        builder.addRow("min", lowerHRangeField);
        builder.addRow("max", upperHRangeField);
        builder.addGap();
        builder.addSubtitle("vertical");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);
        
        return builder.getPanel();
    }
    
    // Enabled state
    
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        
        VariableItems.Iterator i;
        i = parFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
        }
        i = varFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
        }
        orderField.setEditable(flag);
        transientsField.setEditable(flag);
        lowerHRangeField.setEditable(flag);
        upperHRangeField.setEditable(flag);
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
    }
    
    // Type
    
    public void setPlotType(int type) {
        this.type = type;
        
        removeAll();
        add(createPanel(), new CellConstraints(1, 1));
        repaint();
    }
    
    public int getPlotType() {
        return type;
    }
    
    // Initial Values
    
    public final VariableDoubles getInitialValues() throws InvalidData {
        if (type == TYPE_COWEB) {
            return FormHelper.collectFieldValues(varFields);
        }
        else {
            throw new Error("invalid plot type");
        }
        
    }
    
    public void setInitialValues(final VariableDoubles init) {
        if (type == TYPE_COWEB) {
            FormHelper.setFieldValues(varFields, init);
        }
        else {
            throw new Error("invalid plot type");
        }
    }
    
    // Parameters
    
    public VariableDoubles getParameterValues() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }
    
    public void setParametersValues(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }
    
    // Power
    
    public int getPower() throws InvalidData {
        return orderField.getValue();
    }
    
    public void setPower(int value) {
        orderField.setValue(value);
    }
    
    // Transients
    
    public int getTransients() throws InvalidData {
        return transientsField.getValue();
    }
    
    public void setTransients(int t) {
        transientsField.setValue(t);
    }
    
    // Ranges
    
    public Range getXRange() throws InvalidData {
        if (lowerHRangeField.getValue() >= upperHRangeField.getValue()) {
            throw new InvalidData("Invalid horizontal range");
        }
        return new Range(
        lowerHRangeField.getValue(), upperHRangeField.getValue());
    }
    
    public void setXRange(Range range) {
        lowerHRangeField.setValue(range.getLowerBound());
        upperHRangeField.setValue(range.getUpperBound());
    }
    
    public Range getYRange() throws InvalidData {
        if (lowerVRangeField.getValue() >= upperVRangeField.getValue()) {
            throw new InvalidData("Invalid vertical range");
        }
        return new Range(
        lowerVRangeField.getValue(), upperVRangeField.getValue());
    }
    
    public void setYRange(Range range) {
        lowerVRangeField.setValue(range.getLowerBound());
        upperVRangeField.setValue(range.getUpperBound());
    }
    
    
    
    // Debug only
/*
    public static void main(String[] args) {
        Model model;
 
        try {
            File f = new File("/home/tsho-debian/workspace.common/dmcDue/models/lorenz.lua");
//            File f = new File("/home/tsho-debian/workspace.common/dmcDue/models/logist.lua");
            model = Lua.newModel(f);
        }
        catch (IOException e) {
            throw new Error(e);
        }
        catch (ModelException e) {
            throw new Error(e);
        }
 
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        CowebControlForm2 panel = new CowebControlForm2(model, TYPE_SHIFTED);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
    }
 */
    
    
    protected String getFormType() {
        String s="COWEB";
        if (type==this.TYPE_COWEB)
            return s+"_CO";
        else
            return s+"_SH";
    }
    
}

