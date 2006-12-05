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
package org.tsho.dmc2.ui.bifurcation;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.jfree.chart.plot.DefaultDrawingSupplier;
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
import org.tsho.dmc2.ui.components.GetVector;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public final class BifurcationControlForm2 extends AbstractControlForm {
    
    private VariableItems varFields;
    private VariableItems parFields;
    
    private GetFloat lFirstParRange;
    private GetFloat uFirstParRange;
    private GetFloat lSecondParRange;
    private GetFloat uSecondParRange;
    private JComboBox box1;
    private JComboBox box2;
    private String item1;
    private String item2;
    
    private GetInt transientsField;
    private GetInt iterationsField;
    
    //private GetInt periodTransientsField;
    private GetInt periodField;
    
    private GetFloat epsilonField;
    private GetFloat infinityField;
    
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    
    private GetFloat timeField;
    private GetFloat stepField;
    
    private JComboBox verticalBox;
    
    private GetVector hyperplaneCoeffField;
    
    private Model model;
    
    public static final byte TYPE_SINGLE = 2;
    public static final byte TYPE_DOUBLE = 3;
    
    private byte type;
    
    public BifurcationControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        setOpaque(true);
        
        this.model=model;
        
        type = TYPE_DOUBLE;
        
        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        varFields = FormHelper.createFields(model.getVarNames(), "initial value");
        
        box1 = new JComboBox(model.getParNames());
        box2 = new JComboBox(model.getParNames());
        MyListener myListener = new MyListener();
        box1.addItemListener(myListener);
        box2.addItemListener(myListener);
        
        transientsField = new GetInt(
        "transients", FormHelper.FIELD_LENGTH,
        new Range(0, Integer.MAX_VALUE));
        
        iterationsField = new GetInt(
        "iterations", FormHelper.FIELD_LENGTH,
        new Range(1, Integer.MAX_VALUE));
        
        periodField = new GetInt(
        "maximal period", FormHelper.FIELD_LENGTH,
        new Range(0, DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length));
        
        epsilonField = new GetFloat(
        "epsilon", FormHelper.FIELD_LENGTH,
        new Range(0, Double.MAX_VALUE));
        
        infinityField = new GetFloat(
        "infinity", FormHelper.FIELD_LENGTH,
        new Range(0, Double.MAX_VALUE));
        
        lFirstParRange = new GetFloat(
        "first parameter lower value", FormHelper.FIELD_LENGTH);
        uFirstParRange = new GetFloat(
        "first parameter upper value", FormHelper.FIELD_LENGTH);
        lSecondParRange = new GetFloat(
        "second parameter upper value", FormHelper.FIELD_LENGTH);
        uSecondParRange  = new GetFloat(
        "second parameter upper value", FormHelper.FIELD_LENGTH);
        
        lowerVRangeField = new GetFloat(
        "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
        "upper vertical range", FormHelper.FIELD_LENGTH);
        
        verticalBox = new JComboBox(model.getVarNames());
        
        timeField=new GetFloat("time",FormHelper.FIELD_LENGTH);
        stepField=new GetFloat("step",FormHelper.FIELD_LENGTH);
        hyperplaneCoeffField =new GetVector("hyperplane coefficients",model.getNVar()+1);
        
        FormLayout layout = new FormLayout("f:p:n", "");
        
        setLayout(layout);
        layout.appendRow(new RowSpec("f:p:n"));
        add(createPanel(), new CellConstraints(1, 1));
        
        if (model.getNPar() >= 2) {
            box1.setSelectedIndex(0);
            box2.setSelectedIndex(1);
            item1=(String) box1.getSelectedItem();
            item2=(String) box2.getSelectedItem();
        }
    }
    
    
    protected String getFormType(){
        String s="BIFURCATION";
        if (type==BifurcationControlForm2.TYPE_DOUBLE)
            s=s+"_2";
        if (type==BifurcationControlForm2.TYPE_SINGLE)
            s=s+"_1";
        
        return s;
    }
    
    
    
    
    private JPanel createPanel() {
        if (type == TYPE_SINGLE) {
            return createSimpleParameterPanel();
        }
        else if (type == TYPE_DOUBLE) {
            return createDoubleParameterPanel();
        }
        throw new Error("invalid type");
    }
    
    private JPanel createSimpleParameterPanel() {
        
        FormHelper.FormBuilder builder;
        builder = FormHelper.controlFormBuilder(this,false);
        
        VariableItems.Iterator i;
        builder.addTitle("Inital values");
        i = varFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }
        
        builder.addTitle("Parameters");
        i = parFields.iterator();
        int idx = 0;
        String label;
        while (i.hasNext()) {
            label = i.nextLabel();
            if (idx++ == box1.getSelectedIndex()) {
                ((GetFloat) parFields.get(label)).setIgnoreValid(true);
                continue;
            }
            ((GetFloat) parFields.get(label)).setIgnoreValid(false);
            builder.addRow(label, (JComponent) i.value());
        }
        
        builder.addGap();
        builder.addRow("Horizontal axis", box1);
        builder.addRow("min", lFirstParRange);
        builder.addRow("max", uFirstParRange);
        builder.addGap();
        builder.addTitle("Vertical range");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);
        builder.addGap();
        
        if (model instanceof ODE){
            builder.addTitle("Poincare section");
            builder.addRow("plane coefficients",hyperplaneCoeffField);
            builder.addGap();
        }
        
        builder.addTitle("Algorithm");
        builder.addRow("transients",transientsField);
        if (model instanceof ODE){
            builder.addRow("time",timeField);
            builder.addRow("step",stepField);
        }
        else {
            builder.addRow("iterations", iterationsField);
        }
        builder.addGap();
        
        builder.addRow("vertical axis", verticalBox);
        
        return builder.getPanel();
    }
    
    private JPanel createDoubleParameterPanel() {
        
        FormHelper.FormBuilder builder;
        builder = FormHelper.controlFormBuilder(this,false);
        
        VariableItems.Iterator i;
        builder.addTitle("Inital values");
        i = varFields.iterator();
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }
        
        builder.addTitle("Parameters");
        i = parFields.iterator();
        int idx = 0;
        String label;
        while (i.hasNext()) {
            label = i.nextLabel();
            if (idx++ == box1.getSelectedIndex()) {
                ((GetFloat) parFields.get(label)).setIgnoreValid(true);
                continue;
            }
            if (idx - 1 == box2.getSelectedIndex()) {
                ((GetFloat) parFields.get(label)).setIgnoreValid(true);
                continue;
            }
            ((GetFloat) parFields.get(label)).setIgnoreValid(false);
            builder.addRow(label, (JComponent) i.value());
        }
        
        builder.addGap();
        builder.addRow("Horizontal axis", box1);
        builder.addRow("min", lFirstParRange);
        builder.addRow("max", uFirstParRange);
        builder.addGap();
        builder.addRow("Vertical axis", box2);
        builder.addRow("min", lSecondParRange);
        builder.addRow("max", uSecondParRange);
        
        builder.addTitle("Approximation");
        builder.addRow("epsilon", epsilonField);
        builder.addRow("infinity", infinityField);
        
        if (model instanceof ODE){
            builder.addTitle("Poincare section");
            builder.addRow("plane coefficients",hyperplaneCoeffField);
            builder.addGap();
        }
        
        builder.addTitle("Period");
        builder.addRow("maximal period", periodField);
        
        builder.addTitle("Algorithm");
        builder.addRow("transients",transientsField);
        if (model instanceof ODE){
            builder.addRow("maximal time",timeField);
            builder.addRow("step",stepField);
        }
        
        return builder.getPanel();
    }
    
    
    private class MyListener implements ItemListener {
        
        private String cb1,cb2;
        
        public void itemStateChanged(ItemEvent e) {
            //if (ignoreComboBoxChanges) return;
            //  catch deselected combo events
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            cb1=(String) box1.getSelectedItem();
            cb2=(String) box2.getSelectedItem();
            if (cb1.equals(cb2)){
                if (!cb1.equals(item1)){
                    box2.setSelectedItem(item1);
                }
                else{
                    box1.setSelectedItem(item2);
                }
            }
            item1=(String) box1.getSelectedItem();
            item2=(String) box2.getSelectedItem();
            
            removeAll();
            add(createPanel(), new CellConstraints(1, 1));
            revalidate();
            repaint();
        }
    }
    
    // Type
    
    public byte getType() {
        return type;
    }
    
    public void setType(byte t) {
        this.type = t;
        removeAll();
        add(createPanel(), new CellConstraints(1, 1));
        revalidate();
        repaint();
    }
    
    // Enabled state
    
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        
        VariableItems.Iterator i;
        i = varFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
        }
        i = parFields.iterator();
        while (i.hasNext()) {
            ((GetFloat) i.nextValue()).setEditable(flag);
        }
        
        lFirstParRange.setEditable(flag);
        uFirstParRange.setEditable(flag);
        lSecondParRange.setEditable(flag);
        uSecondParRange.setEditable(flag);
        box1.setEnabled(flag);
        box2.setEnabled(flag);
        
        iterationsField.setEditable(flag);
        epsilonField.setEditable(flag);
        
        transientsField.setEditable(flag);
        periodField.setEditable(flag);
        infinityField.setEditable(flag);
        
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
        
        verticalBox.setEnabled(flag);
    }
    
    // Initial values
    
    public VariableDoubles getInitialValues() throws InvalidData {
        return FormHelper.collectFieldValues(varFields);
    }
    
    public void setInitialValues(final VariableDoubles init) {
        FormHelper.setFieldValues(varFields, init);
    }
    
    // Parameters
    
    public VariableDoubles getParameters() throws InvalidData {
        return FormHelper.collectFieldValues(parFields);
    }
    
    public void setParameters(final VariableDoubles init) {
        FormHelper.setFieldValues(parFields, init);
    }
    
    public Range getFirstParameterRange() throws InvalidData {
        if (lFirstParRange.getValue() >= uFirstParRange.getValue()) {
            throw new InvalidData("Invalid \"" + box1.getSelectedItem() + "\" parameter range");
        }
        return new Range(lFirstParRange.getValue(), uFirstParRange.getValue());
    }
    
    public void setFirstParameterRange(Range range) {
        lFirstParRange.setValue(range.getLowerBound());
        uFirstParRange.setValue(range.getUpperBound());
    }
    
    public String getFirstParameterLabel() throws InvalidData {
        return (String) box1.getSelectedItem();
    }
    
    public Range getSecondParameterRange() throws InvalidData {
        if (box1.getSelectedItem() == box2.getSelectedItem()) {
            throw new InvalidData("Must select different parameters");
        }
        if (lSecondParRange.getValue() >= uSecondParRange.getValue()) {
            throw new InvalidData("Invalid \"" + box2.getSelectedItem() + "\" parameter range");
        }
        return new Range(lSecondParRange.getValue(), uSecondParRange.getValue());
    }
    
    public void setsecondParameterRange(Range range) {
        lSecondParRange.setValue(range.getLowerBound());
        uSecondParRange.setValue(range.getUpperBound());
    }
    
    public String getSecondParameterLabel() throws InvalidData {
        return (String) box2.getSelectedItem();
    }
    
    // Vertical axis label
    
    public String getVerticalAxisLabel() throws InvalidData {
        return (String) verticalBox.getSelectedItem();
    }
    
    // Epsilon
    
    public double getEpsilon() throws InvalidData {
        return epsilonField.getValue();
    }
    
    public void setEpsilon(double value) {
        epsilonField.setValue(value);
    }
    
    // Infinity
    
    public double getInfinity() throws InvalidData {
        return infinityField.getValue();
    }
    
    public void setInfinity(double value) {
        infinityField.setValue(value);
    }
    
    // Iterations
    
    public int getIterations() throws InvalidData {
        return iterationsField.getValue();
    }
    
    public void setIterations(int t) {
        iterationsField.setValue(t);
    }
    
    // Transients
    
    public int getTransients() throws InvalidData {
        return transientsField.getValue();
    }
    
    public void setTransients(int t) {
        iterationsField.setValue(t);
    }
    
    public void setTime(double time){
        timeField.setValue(time);
    }
    
    public double getTime() throws InvalidData{
        return timeField.getValue();
    }
    
    public void setStep(double step){
        stepField.setValue(step);
    }
    
    public double getStep() throws InvalidData{
        return stepField.getValue();
    }
    
    public double[] getHyperplaneCoeff() throws InvalidData{
        return hyperplaneCoeffField.getValue();
    }
    
    public void setHyperplaneCoeff(double[] coeffs){
        hyperplaneCoeffField.setValue(coeffs);
    }
    
    // Period
    
    public int getPeriod() throws InvalidData {
        return periodField.getValue();
    }
    
    public void setPeriod(int t) {
        periodField.setValue(t);
    }
    
    // Period transients
    /*
    public int getPeriodTransients() throws InvalidData {
        return periodTransientsField.getValue();
    }
    
    public void setPeriodTransients(int t) {
        periodTransientsField.setValue(t);
    }
    */
    // Vertical Range
    
    public Range getVerticalRange() throws InvalidData {
        if (lowerVRangeField.getValue() >= upperVRangeField.getValue()) {
            throw new InvalidData("Invalid vertical range.");
        }
        return new Range(
        lowerVRangeField.getValue(), upperVRangeField.getValue());
    }
    
    public void setVerticalRange(Range range) {
        lowerVRangeField.setValue(range.getLowerBound());
        upperVRangeField.setValue(range.getUpperBound());
    }
    
}

