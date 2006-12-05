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
package org.tsho.dmc2.ui.cycles;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;
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

public final class CyclesControlForm2 extends AbstractControlForm {

    private VariableItems parFields;
    private GetFloat epsilonField;
    private GetInt periodField;
    private GetInt triesField;
    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    private JComboBox domainBox;
    private JComboBox rangeBox;

    
    public CyclesControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);

        if (model.getNVar() < 1) {
            throw new Error("models must be with dimension >= 2");
        }

        parFields = FormHelper.createFields(model.getParNames(), "parameter");

        epsilonField = new GetFloat(
                "epsilon", FormHelper.FIELD_LENGTH,
                new Range(0, Double.MAX_VALUE));

        periodField = new GetInt(
                "period", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        triesField = new GetInt(
                "max. tries", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        lowerHRangeField = new GetFloat(
                "lower horizontal range", FormHelper.FIELD_LENGTH);
        upperHRangeField = new GetFloat(
                "upper horizontal range", FormHelper.FIELD_LENGTH);
        lowerVRangeField = new GetFloat(
                "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
                "upper vertical range", FormHelper.FIELD_LENGTH);

        domainBox = new JComboBox(model.getVarNames());
        rangeBox = new JComboBox(model.getVarNames());

        domainBox.setSelectedIndex(0);
        if (model.getNVar() > 1) {
            rangeBox.setSelectedIndex(1);
        }

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
        builder.addRow("epsilon", epsilonField);
        builder.addRow("period", periodField);
        builder.addRow("max. tries", triesField);
        
        builder.addTitle("Ranges");
        builder.addSubtitle("horizontal");
        builder.addRow("min", lowerHRangeField);
        builder.addRow("max", upperHRangeField);
        builder.addGap();
        builder.addSubtitle("vertical");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);

        builder.addTitle("Axes");
        builder.addRow("domain", domainBox);
        builder.addRow("range", rangeBox);

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
        periodField.setEditable(flag);
        triesField.setEditable(flag);
        lowerHRangeField.setEditable(flag);
        upperHRangeField.setEditable(flag);
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
        domainBox.setEnabled(flag);
        rangeBox.setEnabled(flag);
    }

    // Parameters

    public VariableDoubles getParameterValues() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }

    public void setParameterValues(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }

    // Power

    public double getEpsilon() throws InvalidData {
        return epsilonField.getValue();
    }

    public void setEpsilon(double value) {
        epsilonField.setValue(value);
    }

    // Limit

    public int getPeriod() throws InvalidData {
        return periodField.getValue();
    }

    public void setPeriod(int t) {
        periodField.setValue(t);
    }

    // Limit

    public int getMaxTries() throws InvalidData {
        return triesField.getValue();
    }

    public void setMaxTries(int t) {
        triesField.setValue(t);
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

    // Axes
    
    public String getLabelOnX() {
        return (String) domainBox.getSelectedItem();
    }

    public String getLabelOnY() {
        return (String) rangeBox.getSelectedItem();
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
        CyclesControlForm2 panel = new CyclesControlForm2(model);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
    }
    */
    
    protected String getFormType() {
        return "CYCLES";
    }
    
}

