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
package org.tsho.dmc2.ui.absorbingArea;

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

public final class AbsorbingAreaControlForm2 extends AbstractControlForm {

    private VariableItems parFields;
    private VariableItems varFields;
    
    private GetFloat epsilonField;
    private GetInt orderField;
    private GetInt iterationsField;
    private GetInt transientsField;
    
    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    private int type=0;
    
    public AbsorbingAreaControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);

        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        
        iterationsField = new GetInt(
                "iterations", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));
        epsilonField= new GetFloat(
                "epsilon", FormHelper.FIELD_LENGTH);
        transientsField=new GetInt("transients",FormHelper.FIELD_LENGTH,new Range(1,Integer.MAX_VALUE));
        
        lowerHRangeField = new GetFloat(
                "lower horizontal range", FormHelper.FIELD_LENGTH);
        upperHRangeField = new GetFloat(
                "upper horizontal range", FormHelper.FIELD_LENGTH);
        lowerVRangeField = new GetFloat(
                "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
                "upper vertical range", FormHelper.FIELD_LENGTH);

        FormLayout layout = new FormLayout("f:p:n", "");
        
        setLayout(layout);
        layout.appendRow(new RowSpec("f:p:n"));
        add(createPanel(), new CellConstraints(1, 1));
    }

    private JPanel createPanel() {

        FormHelper.FormBuilder builder;
        builder = FormHelper.controlFormBuilder(this,false);

        VariableItems.Iterator i;
        builder.addTitle("Parameters");
        i = parFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }
        builder.addTitle("Algorithm");
        builder.addRow("epsilon",epsilonField);
        
        builder.addTitle("Attractor");
        
        
        builder.addRow("iterations", iterationsField);
        builder.addRow("transients", transientsField);
        
        builder.addGap();
        
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
        
        
        epsilonField.setEditable(flag);
        iterationsField.setEditable(flag);
        transientsField.setEditable(flag);
        lowerHRangeField.setEditable(flag);
        upperHRangeField.setEditable(flag);
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
    }

    // Parameters

    public VariableDoubles getParameterValues() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }

    public void setParameterValues(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }

    public int getIterations() throws InvalidData {
        return iterationsField.getValue();
    }

    public void setIterations(int t) {
        iterationsField.setValue(t);
    }


    
    public double getEpsilon() throws InvalidData {
        return epsilonField.getValue();
    }

    public void setEpsilon(int t) {
        epsilonField.setValue(t);
    }
    
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
        lowerHRangeField.setValue(range.getLowerBound());
        upperHRangeField.setValue(range.getUpperBound());
    }
    
    
    protected String getFormType() {
        return "AbsorbingArea";
    }
    
    public void setType(int type){
        this.type=type;
    }
    
}

