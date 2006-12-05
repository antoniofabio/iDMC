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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.InvertibleMap;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class InfoPanel extends JPanel {

    private static Thread showMemoryThread;

    public InfoPanel(Model model) {
        super();

        String modelName = null;
        String modelType = null;
        String modelDesc = null;
        String modelText = null;
        String modelDim  = null;
        String modelPar  = null;

        if (model != null) {
            modelName = model.getName();

            if (model instanceof SimpleMap) {
                modelType = "discrete";
            }
            else if (model instanceof ODE) {
                modelType = "continuous";
            }
            else {
                modelType = "undefined (error)";
            }
            //modelDesc = model.getDescription();
            modelDesc = null;
            modelText = model.getModelText();
            modelDim  = Integer.toString(model.getNVar());
            modelPar  = Integer.toString(model.getNPar());
        }

        //DefaultComponentFactory compFactory = DefaultComponentFactory.getInstance();

        FormLayout layout = new FormLayout(
//              "l:9dlu:n, l:p:n, l:4dlu:n, l:90dlu:n, l:8dlu:n", 
                "l:9dlu:n, l:p:n, l:4dlu:n, l:p:g, l:8dlu:n",        		
              "");

        DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);
        CellConstraints cc = new CellConstraints();

        builder.setDefaultDialogBorder();
        builder.setLeadingColumnOffset(1);

        builder.appendSeparator("Model specification");

        JTextArea textField = new JTextArea(modelName);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Name:", textField);

        textField = new JTextArea(modelType);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Type:", textField);

        textField = new JTextArea(modelDim);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Dimension:", textField);

        textField = new JTextArea(modelPar);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Parameters:", textField);

        String str;
        if (model instanceof InvertibleMap) {
            str = "yes";
        }
        else {
            str = "no";
        }
        textField = new JTextArea(str);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Has inverse:", textField);

        if (model instanceof DifferentiableMap) {
            str = "yes";
        }
        else {
            str = "no";
        }
        textField = new JTextArea(str);
        textField.setEditable(false);
        textField.setLineWrap(true);
        builder.append("Has jacobian:", textField);

        if (modelDesc != null) {
            builder.appendSeparator("Model description");

            textField = new JTextArea(modelDesc);
            textField.setEditable(false);
            textField.setLineWrap(true);
            textField.setWrapStyleWord(true);
            builder.appendRow(builder.getLineGapSpec());
            builder.appendRow("t:p:n");
            builder.nextLine(2);
            //builder.append("Model description:");
            builder.add(textField, 
                        cc.xywh(builder.getColumn(), builder.getRow(), 3, 1));

            builder.nextLine();
        }

        if (modelText != null) {
            builder.appendSeparator("Model text");
            
            JPanel panel = new JPanel(new BorderLayout());
            JTextArea area = new JTextArea(modelText);
            area.setLineWrap(false);
            //area.setWrapStyleWord(true);
            area.setCaretPosition(0);
            area.setEditable(false);
            panel.add(new JScrollPane(area));
            builder.appendRow(builder.getLineGapSpec());
            builder.appendRow("t:50dlu:g");
            builder.nextLine(2);
            //builder.append("Model description:");
            builder.add(panel, 
                        cc.xywh(builder.getColumn(), builder.getRow(), 3, 1));

            builder.nextLine();
        }

        builder.appendSeparator("Memory");

        final JTextArea maxMem = new JTextArea("1");
        maxMem.setEditable(false);
        maxMem.setLineWrap(true);
        builder.append("Max memory:", maxMem);

        final JTextArea totMem = new JTextArea();
        totMem.setEditable(false);
        totMem.setLineWrap(true);
        builder.append("Total memory:", totMem);

        final JTextArea freeMem = new JTextArea();
        freeMem.setEditable(false);
        freeMem.setLineWrap(true);
        builder.append("Free memory:", freeMem);

        // TODO kludge
        if (showMemoryThread != null) {
            showMemoryThread.interrupt();
        }

        // TODO change this to a swing timer
        showMemoryThread = new Thread("showMemory") {
            public void run() {
                Runtime runtime;
                do {
                    runtime = Runtime.getRuntime();
                    maxMem.setText(Long.toString(runtime.maxMemory()/ (1024 * 1024)) + " MB");
                    totMem.setText(Long.toString(runtime.totalMemory() / (1024 * 1024)) + " MB");
                    freeMem.setText(Long.toString(runtime.freeMemory() / (1024 * 1024)) + " MB");

                    if (Thread.interrupted()) {
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } while(true);
            }
        };

        showMemoryThread.start();
    }

    public void dispose() {
//        showMemoryThread.interrupt();
//        showMemoryThread = null;
    }
}
