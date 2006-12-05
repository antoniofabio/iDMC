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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.data.Range;
//import org.tsho.dmc2.ui.AbstractControlForm;
//import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.FormHelper;
import org.tsho.dmc2.ui.InvalidData;
import org.tsho.dmc2.ui.components.GetFloat;
import org.tsho.dmc2.ui.components.GetInt;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;
import com.jgoodies.forms.layout.FormLayout;


public class CowebAlgorithmForm extends JPanel {

    static final String LABEL_POWER_SIZE = "Power";
    static final String LABEL_STEP_SIZE = "Step size";
    static final String LABEL_TRANSIENTS = "Transients";
    static final String LABEL_ITERATIONS = "Iterations";

    GetInt fieldPower  = new GetInt(
            LABEL_POWER_SIZE, FIELD_LENGTH, new Range(1, Double.MAX_VALUE));
    GetFloat fieldStepSize  = new GetFloat(
            LABEL_STEP_SIZE, FIELD_LENGTH, new Range(0, Double.MAX_VALUE));
    GetInt fieldTransients  = new GetInt(
            LABEL_TRANSIENTS, FIELD_LENGTH, new Range(0, Integer.MAX_VALUE));
    GetInt fieldIterations  = new GetInt(
            LABEL_ITERATIONS, FIELD_LENGTH, new Range(1, Integer.MAX_VALUE));

    private static final int FIELD_LENGTH = 50;

    public CowebAlgorithmForm(boolean step) {
        setOpaque(true);
        createForm(step);
    }

    private void createForm(boolean step) {
        boolean debug = false;

        FormLayout layout = new FormLayout(
              //"l:9dlu:n, r:p:g, l:4dlu:n, c:49dlu:n,  l:9dlu:n",
              FormHelper.JGOODIES_SHORT_COLUMN_SPECS,
              "");

        DefaultFormBuilder builder;

        if (debug)
            builder = new DefaultFormBuilder(new FormDebugPanel(), layout);
        else
            builder = new DefaultFormBuilder(this, layout);

        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);

        builder.appendSeparator("Algorithm");
        builder.nextLine();

        builder.appendRow(builder.getLineGapSpec());
        builder.nextLine();


        builder.append(LABEL_POWER_SIZE, fieldPower);
        builder.nextLine();

        builder.appendRow(builder.getLineGapSpec());
        builder.appendRow(builder.getLineGapSpec());
        builder.nextLine(2);


        if (step) {
            builder.append(LABEL_STEP_SIZE, fieldStepSize);
            builder.nextLine();

            builder.appendRow(builder.getLineGapSpec());
            builder.appendRow(builder.getLineGapSpec());
            builder.nextLine(2);
        }

        builder.append(LABEL_TRANSIENTS, fieldTransients);
        builder.nextLine();

        builder.append(LABEL_ITERATIONS, fieldIterations);
        builder.nextLine();

        if (debug) {
            setLayout(new BorderLayout());
            add(builder.getContainer());
            FormDebugUtils.dumpAll(builder.getPanel());
        }
    }

    public int getPower() throws InvalidData {
        return fieldPower.getValue();
    }

    public int getTransients() throws InvalidData {
        return fieldTransients.getValue();
    }

    public double getStepSize() throws InvalidData {
        return fieldStepSize.getValue();
    }

    public int getIterations() throws InvalidData {
        return fieldIterations.getValue();
    }


    public void setPower(int value) {
        fieldPower.setValue(value);
    }

    public void setTransients(int value) {
        fieldTransients.setValue(value);
    }

    public void setStepSize(double value) {
        fieldStepSize.setValue(value);
    }

    public void setIterations(int value) {
        fieldIterations.setValue(value);
    }

    public void setEnabled(final boolean flag) {
        super.setEnabled(flag);
        
        fieldPower.setEnabled(flag);
        fieldIterations.setEnabled(flag);
        fieldStepSize.setEnabled(flag);
        fieldTransients.setEnabled(flag);
    }
    
    public void setStepSizeEnabled(final boolean flag) {
        fieldStepSize.setEnabled(flag);
    }
    
    public void setTransientsEnabled(final boolean flag) {
        fieldTransients.setEnabled(flag);
    }
    
    public void setIterationsEnabled(final boolean flag) {
        fieldIterations.setEnabled(flag);
    }
    
    
    /**
     * Testing main.
     *  
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("TrajectoryStepForm");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        CowebAlgorithmForm panel = new CowebAlgorithmForm(true);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
    
}
