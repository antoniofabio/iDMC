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
package org.tsho.dmc2.ui.manifolds;

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

public final class ManifoldsControlForm2 extends AbstractControlForm {

    private VariableItems nodeFields;
    private VariableItems parFields;

    private GetFloat epsilonField;

    private GetInt firstIterationField;
    private GetInt lastIterationField;

    private GetFloat lowerHLRangeField;
    private GetFloat upperHLRangeField;
    private GetFloat lowerVLRangeField;
    private GetFloat upperVLRangeField;

    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    
    public ManifoldsControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);

        nodeFields = FormHelper.createFields(
                model.getVarNames(), "node");
        parFields = FormHelper.createFields(model.getParNames(), "parameter");

        epsilonField = new GetFloat(
                "epsilon", FormHelper.FIELD_LENGTH,
                new Range(0, Double.MAX_VALUE));

        firstIterationField = new GetInt(
                "first iteration", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        lastIterationField = new GetInt(
                "last iteration", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        lowerHLRangeField = new GetFloat(
                "lower horizontal lockout range", FormHelper.FIELD_LENGTH);
        upperHLRangeField = new GetFloat(
                "upper horizontal lockout range", FormHelper.FIELD_LENGTH);
        lowerVLRangeField = new GetFloat(
                "lower vertical lockout range", FormHelper.FIELD_LENGTH);
        upperVLRangeField = new GetFloat(
                "upper vertical lockout range", FormHelper.FIELD_LENGTH);

        lowerHRangeField = new GetFloat(
                "lower horizontal plot range", FormHelper.FIELD_LENGTH);
        upperHRangeField = new GetFloat(
                "upper horizontal plot range", FormHelper.FIELD_LENGTH);
        lowerVRangeField = new GetFloat(
                "lower vertical plot range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
                "upper vertical plot range", FormHelper.FIELD_LENGTH);

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

        builder.addTitle("Node approximation");
        i = nodeFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }

        builder.addTitle("Algorithm");
        builder.addRow("epsilon", epsilonField);
        builder.addGap();
        builder.addSubtitle("iterations");
        builder.addRow("first", firstIterationField);
        builder.addRow("last", lastIterationField);

        builder.addTitle("Plot ranges");
        builder.addSubtitle("horizontal");
        builder.addRow("min", lowerHRangeField);
        builder.addRow("max", upperHRangeField);
        builder.addGap();
        builder.addSubtitle("vertical");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);

        builder.addTitle("Lockout region ranges");
        builder.addSubtitle("horizontal");
        builder.addRow("min", lowerHLRangeField);
        builder.addRow("max", upperHLRangeField);
        builder.addGap();
        builder.addSubtitle("vertical");
        builder.addRow("min", lowerVLRangeField);
        builder.addRow("max", upperVLRangeField);

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
        i = nodeFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
        }

        epsilonField.setEditable(flag);
        firstIterationField.setEditable(flag);
        lastIterationField.setEditable(flag);
        lowerHRangeField.setEditable(flag);
        upperHRangeField.setEditable(flag);
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
        
        lowerHLRangeField.setEditable(flag);
        upperHLRangeField.setEditable(flag);
        lowerVLRangeField.setEditable(flag);
        upperVLRangeField.setEditable(flag);
    }

    // Parameters

    public VariableDoubles getParameterValues() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }

    public void setParameterValues(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }

    // Node values

    public VariableDoubles getNodeValues() throws InvalidData {
        return FormHelper.collectFieldValues(nodeFields);
    }

    public void setNodeValues(final VariableDoubles init) {
        FormHelper.setFieldValues(nodeFields, init);
    }

    // epsilon

    public double getEpsilon() throws InvalidData {
        return epsilonField.getValue();
    }

    public void setEpsilon(double value) {
        epsilonField.setValue(value);
    }

    // Iterations

    public int getIterationsFirst() throws InvalidData {
        if (firstIterationField.getValue() > lastIterationField.getValue()) {
            throw new InvalidData("Invalid iterations values");
        }
        return firstIterationField.getValue();
    }

    public void setIterationsFirst(int t) {
        firstIterationField.setValue(t);
    }

    public int getIterationsLast() throws InvalidData {
        return lastIterationField.getValue();
    }

    public void setIterationsLast(int t) {
        lastIterationField.setValue(t);
    }

    // Plot ranges

    public Range getHorizontalRange() throws InvalidData {
        if (lowerHRangeField.getValue() >= upperHRangeField.getValue()) {
            throw new InvalidData("Invalid horizontal range");
        }
        return new Range(
                lowerHRangeField.getValue(), upperHRangeField.getValue());
    }

    public void setHorizontalRange(Range range) {
        lowerHRangeField.setValue(range.getLowerBound());
        upperHRangeField.setValue(range.getUpperBound());
    }

    public Range getVerticalRange() throws InvalidData {
        if (lowerVRangeField.getValue() >= upperVRangeField.getValue()) {
            throw new InvalidData("Invalid vertical range");
        }
        return new Range(
                lowerHRangeField.getValue(), upperHRangeField.getValue());
    }

    public void setVerticalRange(Range range) {
        lowerHRangeField.setValue(range.getLowerBound());
        upperHRangeField.setValue(range.getUpperBound());
    }

    // Lockout ranges

    public Range getLockoutHRange() throws InvalidData {
        if (lowerHLRangeField.getValue() >= upperHLRangeField.getValue()) {
            throw new InvalidData("Invalid horizontal range");
        }
        return new Range(
                lowerHLRangeField.getValue(), upperHLRangeField.getValue());
    }

    public void setLockoutHRange(Range range) {
        lowerHLRangeField.setValue(range.getLowerBound());
        upperHLRangeField.setValue(range.getUpperBound());
    }

    public Range getLockoutVRange() throws InvalidData {
        if (lowerVLRangeField.getValue() >= upperVLRangeField.getValue()) {
            throw new InvalidData("Invalid vertical range");
        }
        return new Range(
                lowerHLRangeField.getValue(), upperHLRangeField.getValue());
    }

    public void setLockoutVRange(Range range) {
        lowerHLRangeField.setValue(range.getLowerBound());
        upperHLRangeField.setValue(range.getUpperBound());
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
        ManifoldsControlForm2 panel = new ManifoldsControlForm2(model);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
    }
  */  
    
    
    protected String getFormType() {
        return "MANIFOLDS";
    }
    
}

