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
package org.tsho.dmc2.ui.lyapunov;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import org.tsho.dmc2.core.model.ODE;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public final class LyapunovControlForm2 extends AbstractControlForm {
    
    private Model model;
    
    private VariableItems varFields;
    private VariableItems parFields;
    
    private GetFloat lFirstParRange;
    private GetFloat uFirstParRange;
    private GetFloat lSecondParRange;
    private GetFloat uSecondParRange;
    
    private JComboBox box1;
    private JComboBox box2;
    //records the previous state of the combo boxes
    private String item1;
    private String item2;
    
    
    private GetInt iterationsField;
    private GetFloat epsilonField;
    
    private GetFloat lowerTRangeField;
    private GetFloat upperTRangeField;
    private GetFloat stepSizeField;
    private GetFloat timePeriodField;
    
    private GetFloat lowerVRangeField;
    private GetFloat upperVRangeField;
    
    public static final byte TYPE_VS_TIME = 1;
    public static final byte TYPE_VS_PAR = 2;
    public static final byte TYPE_PAR_SPACE = 3;
    
    private byte type;
    
    public LyapunovControlForm2(final Model model, AbstractPlotComponent frame) {
        super(frame);
        this.model=model;
        setOpaque(true);
        
        type = TYPE_VS_PAR;
        
        parFields = FormHelper.createFields(model.getParNames(), "parameter");
        varFields = FormHelper.createFields(model.getVarNames(), "initial value");
        
        box1 = new JComboBox(model.getParNames());
        box2 = new JComboBox(model.getParNames());
        MyListener myListener = new MyListener();
        box1.addItemListener(myListener);
        box2.addItemListener(myListener);
        
        iterationsField = new GetInt(
        "iterations", FormHelper.FIELD_LENGTH,
        new Range(1, Integer.MAX_VALUE));
        
        epsilonField = new GetFloat(
        "epsilon", FormHelper.FIELD_LENGTH,
        new Range(0, Double.MAX_VALUE));
        
        lFirstParRange = new GetFloat(
        "first parameter lower value", FormHelper.FIELD_LENGTH);
        uFirstParRange = new GetFloat(
        "first parameter upper value", FormHelper.FIELD_LENGTH);
        lSecondParRange = new GetFloat(
        "second parameter upper value", FormHelper.FIELD_LENGTH);
        uSecondParRange  = new GetFloat(
        "second parameter upper value", FormHelper.FIELD_LENGTH);
        //? 20.7.2004 <if (model instanceof ODE)> added to make lower time value start from 0 in ODE case
        if (model instanceof ODE){
            lowerTRangeField = new GetFloat(
            "lower time range", FormHelper.FIELD_LENGTH,
            new Range(0, Integer.MAX_VALUE));
        }
        else{
            lowerTRangeField = new GetFloat(
            "lower time range", FormHelper.FIELD_LENGTH,
            new Range(1, Integer.MAX_VALUE));
        }
        upperTRangeField = new GetFloat(
        "upper time range", FormHelper.FIELD_LENGTH);
        
        stepSizeField=new GetFloat("step",FormHelper.FIELD_LENGTH);
        
        timePeriodField=new GetFloat("time",FormHelper.FIELD_LENGTH);
        
        lowerVRangeField = new GetFloat(
        "lower vertical range", FormHelper.FIELD_LENGTH);
        upperVRangeField = new GetFloat(
        "upper vertical range", FormHelper.FIELD_LENGTH);
        
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
    
    private JPanel createPanel() {
        if (type == TYPE_VS_TIME) {
            return createTimePanel();
        }
        else if (type == TYPE_VS_PAR) {
            return createSimpleParameterPanel();
        }
        else if (type == TYPE_PAR_SPACE) {
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
        
        builder.addTitle("Algorithm");
        if (model instanceof ODE){
        builder.addRow("time period", timePeriodField);
        builder.addRow("step size",stepSizeField);
        }
        else{
            builder.addRow("iterations",iterationsField);
        }
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
        
        builder.addTitle("Algorithm");
        builder.addRow("epsilon", epsilonField);
        if (model instanceof ODE){
        builder.addRow("time period", timePeriodField);
        builder.addRow("step size",stepSizeField);
        }
        else{
            builder.addRow("iterations",iterationsField);
        }
        
        return builder.getPanel();
    }
    
    private JPanel createTimePanel() {
        
        //? 19.7.2004
        VariableItems.Iterator it;
        it = parFields.iterator();
        while (it.hasNext()) {
            String label;
            label = it.nextLabel();
            ((GetFloat) parFields.get(label)).setIgnoreValid(false);
        }
        //?
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
        while (i.hasNext()) {
            builder.addRow(i.nextLabel(), (Component) i.value());
        }
        
        builder.addTitle("Time range");
        builder.addRow("min", lowerTRangeField);
        builder.addRow("max", upperTRangeField);
        if (model instanceof ODE)
            builder.addRow("step size", stepSizeField);
        
        builder.addTitle("Vertical range");
        builder.addRow("min", lowerVRangeField);
        builder.addRow("max", upperVRangeField);
        
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
        //? remark: so every newly created timePanel or parameterPanel etc. are added
        // to the LyapunovControlForm2 which is a JPanel. Since they are built
        // from existing GetFloats etc., the values of the fields are kept from
        // one "panel" to the other. Thus setting ignoreValid to false in all GetFloats
        // when constructing a "panel" (i.e. timePanel), will indeed help in preventing
        // side effects from setting some GetFields ignoreValid to true in another panel
        // (such as the parameter panel).
        //?
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
        
        lowerTRangeField.setEditable(flag);
        upperTRangeField.setEditable(flag);
        
        lowerVRangeField.setEditable(flag);
        upperVRangeField.setEditable(flag);
    }
    
    // Initial values
    
    public VariableDoubles getInitialValues() throws InvalidData{
        return FormHelper.collectFieldValues(varFields);
    }
    
    public void setInitialValues(final VariableDoubles init) {
        FormHelper.setFieldValues(varFields, init);
    }
    
    // Parameters
    
    public VariableDoubles getParameters() throws InvalidData  {
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
    
    public String getLabelOnH() {
        return (String) box1.getSelectedItem();
    }
    
    public String getLabelOnV() {
        return (String) box2.getSelectedItem();
    }
    
    // Epsilon
    
    public double getEpsilon() throws InvalidData {
        return epsilonField.getValue();
    }
    
    public void setEpsilon(double value) {
        epsilonField.setValue(value);
    }
    
    // Iterations
    
    public int getIterations() throws InvalidData {
        return iterationsField.getValue();
    }
    
    public void setIterations(int t) {
        iterationsField.setValue(t);
    }
    
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
    
    // Vertical Range
    
    public Range getTimeRange() throws InvalidData {
        if (lowerTRangeField.getValue() >= upperTRangeField.getValue()) {
            throw new InvalidData("Invalid vertical range.");
        }
        return new Range(
        lowerTRangeField.getValue(), upperTRangeField.getValue());
    }
    
    public void setTimeRange(Range range) {
        lowerTRangeField.setValue(range.getLowerBound());
        upperTRangeField.setValue(range.getUpperBound());
    }
    
    public double getStepSize() throws InvalidData{
        return stepSizeField.getValue();
    }
    
    public void setStepSize(double stepSize) throws InvalidData{
        stepSizeField.setValue(stepSize);
    }
    
    public double getTimePeriod() throws InvalidData{
        return timePeriodField.getValue();
    }
    
    public void setTimePeriod(double stepSize) throws InvalidData{
        timePeriodField.setValue(stepSize);
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
        LyapunovControlForm2 panel = new LyapunovControlForm2(model);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
        panel.setType(TYPE_VS_TIME);
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        panel = new LyapunovControlForm2(model);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
        panel.setType(TYPE_VS_PAR);
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        panel = new LyapunovControlForm2(model);
        frame.getContentPane().add(panel);
        frame.setTitle(panel.getClass().getName());
        frame.pack();
        frame.show();
        panel.setType(TYPE_PAR_SPACE);
    }
    */
    
    
    
    
    protected String getFormType() {
        String s="LYAPUNOV";
        if (type==this.TYPE_VS_TIME){
            s=s+"_VT";
        }
        if (type==this.TYPE_VS_PAR){
            s=s+"_VP";
        }
        if (type==this.TYPE_PAR_SPACE){
            s=s+"_PS";
        }
        return s;
    }
    
}

