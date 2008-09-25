/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2008 Marji Lines and Alfredo Medio.
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
package org.tsho.dmc2.ui.basinslice;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.jfree.data.Range;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.ui.AbstractControlForm;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.FormHelper;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.components.GetFloat;
import org.tsho.dmc2.ui.components.GetInt;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public final class BasinSliceControlForm extends AbstractControlForm {

    private VariableItems parFields;
    private GetFloat lowerHRangeField;
    private GetFloat upperHRangeField;
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    private GetFloat epsilonField;
    private GetInt limitField;
    private GetInt iterationsField;
    private GetInt trialsField;
    private JComboBox xBox;
    private JComboBox yBox;

    private VariableItems varFields;
    private Model model;
    
    public BasinSliceControlForm(final Model model, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);
        this.model = model;

        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        
        lowerHRangeField = new GetFloat(
            "lower horizontal range", FormHelper.FIELD_LENGTH);
        upperHRangeField = new GetFloat(
            "upper horizontal range", FormHelper.FIELD_LENGTH);
        lowerVRangeField = new GetFloat(
            "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
            "upper vertical range", FormHelper.FIELD_LENGTH);

        epsilonField = new GetFloat(
                "epsilon", FormHelper.FIELD_LENGTH,
                new Range(0, Double.MAX_VALUE));

        limitField = new GetInt(
                "limit", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));

        iterationsField = new GetInt(
                "limits", FormHelper.FIELD_LENGTH,
                new Range(1, Integer.MAX_VALUE));
        
        trialsField = new GetInt(
                "trials",FormHelper.FIELD_LENGTH, 
                new Range(1,Integer.MAX_VALUE));

        varFields = FormHelper.createFields(model.getVarNames(), "variables");
        
        xBox = new JComboBox(model.getVarNames());
        yBox = new JComboBox(model.getVarNames());
        /*listen for variables selection changes*/
        MyListener myListener = new MyListener();
        xBox.addItemListener(myListener);
        yBox.addItemListener(myListener);

        xBox.setSelectedIndex(0);
        if (model.getNVar() > 1) {
            yBox.setSelectedIndex(1);
        }

        FormLayout layout = new FormLayout("f:p:n", "");
        
        setLayout(layout);
        layout.appendRow(new RowSpec("f:p:n"));
        add(createPanel(), new CellConstraints(1, 1));
    }

    private JPanel createPanel() {

        FormHelper.FormBuilder builder;
        builder = FormHelper.controlFormBuilder(this, false);

        VariableItems.Iterator i;
        builder.addTitle("Parameters");
        i = parFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }

        builder.addTitle("Algorithm");

        builder.addSubtitle("attractor");
        builder.addRow("transients", limitField);
        builder.addRow("iterations", iterationsField);
        builder.addRow("trials",trialsField);
        builder.addRow("epsilon",epsilonField);
        builder.addSubtitle("axes");
        builder.addRow("horizontal", xBox);
        builder.addRow("vertical", yBox);
        builder.addGap();
        
        builder.addTitle("Ranges");
        builder.addSubtitle("horizontal");
        builder.addRow("min", lowerHRangeField);
        builder.addRow("max", upperHRangeField);
        builder.addGap();
        builder.addSubtitle("vertical");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);
        builder.addGap();
        
        if (model.getVarNames().length > 2) {
            builder.addTitle("Variables");
            i = varFields.iterator();
            int idx = -1;
            String label;
            while (i.hasNext()) {
                idx++;
                label = i.nextLabel();
                if ((idx == xBox.getSelectedIndex()) || (idx == yBox.getSelectedIndex())) {
                    ((GetFloat) varFields.get(label)).setIgnoreValid(true);
                    continue;
                }
                ((GetFloat) varFields.get(label)).setIgnoreValid(false);
                builder.addRow(label, (Component) i.value());
            }
        }

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
        limitField.setEditable(flag);
        iterationsField.setEditable(flag);
        trialsField.setEditable(flag);
        epsilonField.setEditable(flag);
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

    // Variables
    public VariableDoubles getVariableValues() throws InvalidData {
        return FormHelper.collectFieldValues(varFields);
    }

    // Limit
    public int getLimit() throws InvalidData {
        return limitField.getValue();
    }

    public void setLimit(int t) {
        limitField.setValue(t);
    }

    // iterations
    public int getIterations() throws InvalidData {
        return iterationsField.getValue();
    }

    public void setIterations(int t) {
        iterationsField.setValue(t);
    }

    //trials    
    public int getTrials() throws InvalidData{
        return trialsField.getValue();
    }
    
    public void setTrials(int t){
        trialsField.setValue(t);
    }
    
    //epsilon
    public double getEpsilon() throws InvalidData{
         return epsilonField.getValue();
    }
    public void setEpsilon(double epsilon){
         epsilonField.setValue(epsilon);
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
    
    public int getXVar() {
        return xBox.getSelectedIndex();
    }
    
    public int getYVar() {
        return yBox.getSelectedIndex();
    }
    
    public int getNVar() {
        return model.getNVar();
    }
    
    protected String getFormType() {
        return "BASINSLICE_B";
    }
    
    private class MyListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            removeAll();
            add(createPanel(), new CellConstraints(1, 1));
            revalidate();
            repaint();
        }
    }

}

