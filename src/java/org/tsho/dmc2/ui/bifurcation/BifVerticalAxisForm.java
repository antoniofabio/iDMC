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


import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.tsho.dmc2.ui.FormHelper;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;
import com.jgoodies.forms.layout.FormLayout;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class BifVerticalAxisForm extends JPanel {
    JComboBox box1;
    // JComboBox box2;

    public BifVerticalAxisForm(String[] labels) {
        setOpaque(true);

        createForm(labels);
        box1.setSelectedIndex(0);

    }

    private void createForm(String[] labels) {
           
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


        builder.appendSeparator("Vertical axis");
        builder.nextLine();

        builder.appendRow(builder.getLineGapSpec());
        builder.nextLine(1);


        box1 = new JComboBox(labels);
        builder.append("variable", box1);
        builder.nextLine();

//        box2 = new JComboBox(labels);
//        box2.addItemListener(myListener);
//        builder.append("Range axis", box2);
//        builder.nextLine();
//
//        int box1Width = box1.getPreferredSize().width;
//        int box1Height = box1.getPreferredSize().height;
//        int box2Width = box2.getPreferredSize().width;
//        int box2Height = box2.getPreferredSize().height;
//        
//        int max = (box1Width > box2Width ? box1Width : box2Width);
//        box1.setPreferredSize(new Dimension(max, box1Height));
//        box2.setPreferredSize(new Dimension(max, box2Height));

        if (debug) {
            setLayout(new BorderLayout());
            add(builder.getContainer());
            FormDebugUtils.dumpAll(builder.getPanel());
        }
    }
    
    public String getFirstLabel() {
        return (String)box1.getSelectedItem();
    }

    public void setFirstLabel(int idx) {
        box1.setSelectedIndex(idx);
    }
    
    public void setEnabled(boolean flag) {
        box1.setEnabled(flag);
    }


    /**
     * Testing main.
     *  
     * @param args
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setTitle("AxisForm");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JComponent panel = new BifVerticalAxisForm(new String[] {"asd", "sdf", "wert"});
        frame.getContentPane().add(panel);
        frame.pack();
		frame.setVisible(true);
    }
}

