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
package org.tsho.dmc2.ui.trajectory;

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
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.ui.AbstractControlForm;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.FormHelper;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.components.GetFloat;
import org.tsho.dmc2.ui.components.GetInt;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public final class TrajectoryControlForm2 extends AbstractControlForm {

    private VariableItems varFields;
    private VariableItems parFields;
    private VariableItems varDeltaFields;
    private VariableItems parDeltaFields;

    private GetInt variationField;
    private GetInt transientsField;
    private GetInt iterationsField;
    private GetFloat stepSizeField;
    private GetInt rangesIterationsField;
    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    private JComboBox domainBox;
    private JComboBox rangeBox;

    private boolean variation;
    private boolean autoRanges;
    private boolean time;
    private boolean ode;
    
    private AbstractPlotComponent frame;
    
    public TrajectoryControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        ode = (model instanceof ODE);

        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        varFields = FormHelper.createFields(model.getVarNames(), "initial value");
        parDeltaFields = FormHelper.createFields(model.getParNames(), "parameter variation");
        varDeltaFields = FormHelper.createFields(model.getVarNames(), "intial value variation");

        variationField = new GetInt(
                "variation", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        transientsField = new GetInt(
                "transients", FormHelper.FIELD_LENGTH,
                new Range(0, Integer.MAX_VALUE));

        iterationsField = new GetInt(
                "iterations", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        stepSizeField = new GetFloat(
                "step size", FormHelper.FIELD_LENGTH,
                new Range(0, Double.MAX_VALUE));

        rangesIterationsField = new GetInt(
                "ranges iterations", FormHelper.FIELD_LENGTH,
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
        builder.addTitle("Inital values");
        i = varFields.iterator();
        while (i.hasNext()) {
            if (variation) {
                builder.addRow(
                        i.nextLabel(),
                        (Component) i.value(),
                        (Component) varDeltaFields.get(i.label()));
            }
            else {
                builder.addRow(i.nextLabel(), (Component) i.value());
            }
        }

        builder.addTitle("Parameters");
        i = parFields.iterator();
        while (i.hasNext()) {
            if (variation) {
                builder.addRow(
                        i.nextLabel(),
                        (Component) i.value(),
                        (Component) parDeltaFields.get(i.label()));
            }
            else {
                builder.addRow(i.nextLabel(), (Component) i.value()); 
            }
        }

        builder.addTitle("Algorithm");
        if (variation) {
            builder.addRow("variation", variationField);
            builder.addGap();
        }
        if (ode) {
            builder.addRow("step size", stepSizeField);
            builder.addGap();
        }
        builder.addRow("transients", transientsField);
        builder.addRow("iterations", iterationsField);

        if (autoRanges) {
            builder.addTitle("Auto ranges");
            builder.addRow("iterations", rangesIterationsField);
        }
        else {
            if (!time) {
                builder.addTitle("Ranges");
                builder.addSubtitle("horizontal");
                builder.addRow("min", lowerHRangeField);
                builder.addRow("max", upperHRangeField);
                builder.addGap();
                builder.addSubtitle("vertical");
            }
            else {
                builder.addTitle("Vertical ranges");
            }
            builder.addRow("min", lowerVRangeField);
            builder.addRow("max", upperVRangeField);
        }

        if (time) {
            builder.addTitle("Range axis");
            builder.addRow("", rangeBox);
        }
        else {
            builder.addTitle("Axes");
            builder.addRow("domain", domainBox);
            builder.addRow("range", rangeBox);
        }

        return builder.getPanel();
    }

    // Enabled state

    public void setEnabled(boolean flag) {
        super.setEnabled(flag);

        VariableItems.Iterator i;
        i = varFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
            ((GetFloat) varDeltaFields.get(i.label())).setEditable(flag);
        }
        i = parFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
            ((GetFloat) parDeltaFields.get(i.label())).setEditable(flag);
        }
        variationField.setEditable(flag);
        stepSizeField.setEditable(flag);
        transientsField.setEditable(flag);
        iterationsField.setEditable(flag);
        lowerHRangeField.setEditable(flag);
        upperHRangeField.setEditable(flag);
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
        domainBox.setEnabled(flag);
        rangeBox.setEnabled(flag);
        rangesIterationsField.setEditable(flag);
    }

    public void setIterationsEnabled(boolean flag) {
        iterationsField.setEditable(flag);
    }

    // Initial values

    public VariableDoubles getInitialValues() throws InvalidData {
        return FormHelper.collectFieldValues(varFields);
    }

    public void setInitialValues(final VariableDoubles init) {
        FormHelper.setFieldValues(varFields, init);
    }

    // Parameters

    public VariableDoubles getParameterValues() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }

    public void setParameterValues(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }

    // Delta Initial values

    public VariableDoubles getDeltaVarValues() throws InvalidData {
        return FormHelper.collectFieldValues(varDeltaFields);
    }

    public void setDeltaVarValues(final VariableDoubles init) {
        FormHelper.setFieldValues(varDeltaFields, init);
    }

    // Delta Parameters

    public VariableDoubles getDeltaParameters() throws InvalidData {
        return FormHelper.collectFieldValues(parDeltaFields);
    }

    public void setDeltaParameters(final VariableDoubles init) {
        FormHelper.setFieldValues(parDeltaFields, init);
    }

    // Variation count

    public int getVariationCount() throws InvalidData {
        return variationField.getValue();
    }

    public void setVariationCount(int value) {
        variationField.setValue(value);
    }

    // Step Size

    public double getStepSize() throws InvalidData {
        return stepSizeField.getValue();
    }

    public void setStepSize(int value) {
        stepSizeField.setValue(value);
    }

    // Transients

    public int getTransients() throws InvalidData {
        return transientsField.getValue();
    }

    public void setTransients(int t) {
        transientsField.setValue(t);
    }

    // Iterations

    public int getIterations() throws InvalidData {
        return iterationsField.getValue();
    }

    public void setIterations(int t) {
        iterationsField.setValue(t);
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

    // Iterations

    public int getRangeIterations() throws InvalidData {
        return rangesIterationsField.getValue();
    }

    public void setRangeIterations(int t) {
        rangesIterationsField.setValue(t);
    }

    // Axes
    
    public String getLabelOnX() {
        return (String) domainBox.getSelectedItem();
    }

    public String getLabelOnY() {
        return (String) rangeBox.getSelectedItem();
    }

    // flags
    
    public boolean isAutoRanges() {
        return autoRanges;
    }

    public void setAutoRanges(boolean b) {
        autoRanges = b;
        removeAll();
        add(createPanel(), new CellConstraints(1, 1));
        repaint();
    }

    public boolean isTime() {
        return time;
    }

    public void setTime(boolean b) {
        time = b;

        removeAll();
        add(createPanel(), new CellConstraints(1, 1));
        repaint();
    }

    public boolean isVariation() {
        return variation;
    }

    public void setVariation(boolean b) {
        variation = b;
        
        removeAll();
        add(createPanel(), new CellConstraints(1, 1));
        repaint();
    }

    
    // Debug only

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
        TrajectoryControlForm2 panel = new TrajectoryControlForm2(model,null);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.setVisible(true);
        
        
        JFrame frame2 = new JFrame();
        frame2.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //panel = new TrajectoryControlForm2(model,null,null);
        panel.setVariation(true);
        frame2.getContentPane().add(panel);
        frame2.setTitle(panel.getClass().getName());
        frame2.pack();
        frame2.setVisible(true);
    }
    
    protected String getFormType() {
        String r="TRAJECTORY";
        if (time)
            r=r+"_T1";
        else
            r=r+"_T0";
        if (variation)
            r=r+"_V1";
        else
            r=r+"_V0";
        if (autoRanges)
            r=r+"_A1";
        else
            r=r+"_A0";
        if (ode)
            r=r+"_O1";
        else
            r=r+"_O0";
        return r;
    }
    
}
