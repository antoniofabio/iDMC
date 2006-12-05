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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WindowsSizePanel extends JPanel
        implements ChangeListener, ItemListener {

    private final JSpinner wSpinner = new JSpinner();
    private final JSpinner hSpinner = new JSpinner();
    private final JCheckBox checkBox;
    
    private final SpinnerNumberModel hModel;
    private final SpinnerNumberModel vModel;

    private static final boolean DEFAULT_CHECKBOX_SELECTED = true; 

    /**
     *
     */
    public WindowsSizePanel(int width, int height) {
        super();

        hModel = new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 10);
        vModel = new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 10);
        wSpinner.setModel(hModel);
        hSpinner.setModel(vModel);

        checkBox = new JCheckBox();
        checkBox.addItemListener(this);

        if (DEFAULT_CHECKBOX_SELECTED == true) {
            checkBox.setSelected(true);
        }

        FormLayout layout = new FormLayout(
                        "right:max(40dlu;pref), 3dlu, fill:min(30dlu;pref), 20dlu",
                        "");

        DefaultFormBuilder builder;

        builder = new DefaultFormBuilder(this, layout);

        builder.setDefaultDialogBorder();

        builder.appendSeparator("Plot dimensions");
        builder.nextLine();
        
        builder.appendRow(builder.getLineGapSpec());
        builder.nextLine();

        builder.append("Width", wSpinner);
        builder.nextLine();

        builder.append("Height",   hSpinner);
        builder.nextLine();

        builder.append("Square", checkBox);
        builder.nextLine();
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            hModel.addChangeListener(this);
            vModel.addChangeListener(this);
        }
        else {
            hModel.removeChangeListener(this);
            vModel.removeChangeListener(this);
        }
    }


    public void stateChanged(final ChangeEvent e) {
        if (e.getSource() == hModel) {
            vModel.setValue(hModel.getValue());
        }
        else if (e.getSource() == vModel) {
            hModel.setValue(vModel.getValue());
        }
    }

    public int getWidth() {
        return ((Number) wSpinner.getModel().getValue()).intValue();
    }

    public int getHeight() {
        return ((Number) hSpinner.getModel().getValue()).intValue();
    }

    public static void main(String[] args) {

        WindowsSizePanel panel = new WindowsSizePanel(640, 480);
        
        int result = 
            JOptionPane.showConfirmDialog(null, panel, 
                                          "WindowSizePAnel",
                                          JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            System.out.println(panel.getWidth());
            System.out.println(panel.getHeight());
        }
    }
}
