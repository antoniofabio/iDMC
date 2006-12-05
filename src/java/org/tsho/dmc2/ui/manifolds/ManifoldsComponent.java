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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.jfree.data.Range;
import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.chart.ManifoldsRenderer;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.InvertibleMap;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.managers.ManifoldsManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class ManifoldsComponent extends AbstractPlotComponent 
                                  implements ManifoldsSMItf {

    //private final ManifoldsManager manager;
    private final ManifoldsControlForm2 privateControlForm;

    //private final ManifoldsFrameSM myStateMachine;

    /* special menuitems */
    private JMenuItem singleParameterMenuItem;
    private JMenuItem doubleParameterMenuItem;

    private StartAction startAction = new StartAction();
    private StopAction stopAction = new StopAction();
    private ClearAction clearAction = new ClearAction();

    byte plotTypeDefault;

    private void henonValues(Model model) {
        VariableDoubles d;
        d = new VariableDoubles(model.getVarNames());
        d.put("x", 0.88);
        d.put("y", 0.88);
        privateControlForm.setNodeValues(d);
        
        d = new VariableDoubles(model.getParNames());
        d.put("a", 1.4);
        d.put("b", 0.3);
        privateControlForm.setParameterValues(d);
        
        privateControlForm.setEpsilon(10e-3);
        privateControlForm.setIterationsFirst(1);
        privateControlForm.setIterationsLast(20);
        
        privateControlForm.setHorizontalRange(new Range(-10, 10));
        privateControlForm.setVerticalRange(new Range(-10, 10));
        privateControlForm.setLockoutHRange(new Range(-50, 50));
        privateControlForm.setLockoutVRange(new Range(-50, 50));
    }


    public ManifoldsComponent(final Model model,MainFrame mainFrame) {
        super(model,mainFrame);

        defaultsSection = ModelDefaults.MANIFOLDS_SECTION;

        controlForm = privateControlForm = new ManifoldsControlForm2(model,this);
        //henonValues(model);

        init(new ManifoldsManager((DifferentiableMap) model, privateControlForm, this),
             new ManifoldsFrameSM((ManifoldsSMItf) this),
             controlForm, "Manifolds");

        stateMachine.addNoRunItem(controlForm);
        stateMachine.addNoRunItem(getGridLinesMenuItem());
        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
    }

    public void setImage(final AbstractPlotComponent otherPlot) {

        DmcChartPanel otherChartPanel = (DmcChartPanel) otherPlot.getManager().getChartPanel();

        DmcChartPanel myChartPanel = (DmcChartPanel) getManager().getChartPanel();
        Rectangle r = otherChartPanel.getBounds();

        resizeChart(new Dimension(r.width, r.height));
        myChartPanel.setBufferImage(otherChartPanel.getBufferImage());
    }

    protected JMenu createCommandMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Command");

        /* Start */
        menuItem = new JMenuItem(startAction);
        menu.add(menuItem);

        /* Stop */
        menuItem = new JMenuItem(stopAction);
        menu.add(menuItem);

        menu.addSeparator();

        /* Clear */
        menuItem = new JMenuItem(clearAction);
        menu.add(menuItem);

        return menu;
    }

    protected JMenu createPlotMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Plot");

        ButtonGroup group;

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Unstable");
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.setSelected(true);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    ManifoldsRenderer.TYPE_UNSTABLE);
            }
        });
        stateMachine.addNoRunItem(menuItem);

        menuItem = new JRadioButtonMenuItem("Stable");
        menuItem.setMnemonic(KeyEvent.VK_S);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    ManifoldsRenderer.TYPE_STABLE);
            }
        });
        if (model instanceof InvertibleMap) {
            stateMachine.addNoRunItem(menuItem);
        }
        else {
            menuItem.setEnabled(false);
        }

        menuItem = new JRadioButtonMenuItem("Both");
        menuItem.setMnemonic(KeyEvent.VK_D);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    (byte) (ManifoldsRenderer.TYPE_STABLE
                    | ManifoldsRenderer.TYPE_UNSTABLE));
            }
        });
        if (model instanceof InvertibleMap) {
            stateMachine.addNoRunItem(menuItem);
        }
        else {
            menuItem.setEnabled(false);
        }


        ((ManifoldsManager) getManager()).setPlotType(
            ManifoldsRenderer.TYPE_UNSTABLE);

        menu.addSeparator();

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Right");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menuItem.setSelected(true);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    ManifoldsRenderer.TYPE_RIGHT);
            }
        });
        stateMachine.addNoRunItem(menuItem);

        menuItem = new JRadioButtonMenuItem("Left");
        menuItem.setMnemonic(KeyEvent.VK_L);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    ManifoldsRenderer.TYPE_LEFT);
            }
        });
        stateMachine.addNoRunItem(menuItem);

        menuItem = new JRadioButtonMenuItem("Both");
        menuItem.setMnemonic(KeyEvent.VK_H);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((ManifoldsManager) getManager()).setPlotType(
                    (byte) (ManifoldsRenderer.TYPE_RIGHT
                    | ManifoldsRenderer.TYPE_LEFT));
            }
        });
        stateMachine.addNoRunItem(menuItem);

        ((ManifoldsManager) getManager()).setPlotType(
            ManifoldsRenderer.TYPE_RIGHT);

        //menu.addSeparator();

        //menu.add(createDefaultsMenu());

        return menu;
    }

    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton button;
        button = new JButton(startAction);
        toolBar.add(button);

        button = new JButton(stopAction);
        toolBar.add(button);

        toolBar.addSeparator();

        button = new JButton(clearAction);
        toolBar.add(button);

        return toolBar;
    }

    /*
     * Defaults
     */
    protected void fillDefaults(int index) {
    }

    /**
     * @return
     */
    public Action getClearAction() {
        return clearAction;
    }

    /**
     * @return
     */
    public Action getStartAction() {
        return startAction;
    }

    /**
     * @return
     */
    public Action getStopAction() {
        return stopAction;
    }

    /**
     * @return
     */
    JMenuItem getDoubleParameterMenuItem() {
        return doubleParameterMenuItem;
    }

    /**
     * @return
     */
    JMenuItem getSingleParameterMenuItem() {
        return singleParameterMenuItem;
    }
    
    public void callUponStart() {
    }
    
}
