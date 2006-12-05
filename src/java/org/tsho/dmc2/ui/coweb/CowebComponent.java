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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.managers.CowebManager;
import org.tsho.dmc2.managers.ManagerListener2;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;

public final class CowebComponent extends AbstractPlotComponent
                                implements ManagerListener2,
                                CowebSMItf {

    private final CowebControlForm2 privateControlForm;

    /* controlled menuitems */
    private JMenuItem defaultsMenu;

    JSlider slider;

    private JMenuItem cowebPlotMenuItem;
    private JMenuItem shiftedPlotMenuItem;

    /* actions */
    private final Action startAction = new StartAction();
    private final Action stopAction = new StopAction();
    private final Action clearAction = new ClearAction();

    /* defaults */
    boolean autoBoundsDefault = true;
    int plotTypeDefault;

    public CowebComponent(final Model model,MainFrame mainFrame) {
        super(model,mainFrame);
        defaultsSection = ModelDefaults.SCATTER_SECTION;

        controlForm = privateControlForm =new CowebControlForm2(model, plotTypeDefault,this);

        init(new CowebManager(model, privateControlForm, this),
             new CowebSM(this), controlForm,  "Trajectory");

        stateMachine.addNoRunItem(clearAction);

        stateMachine.addNoRunItem(getTransparencyAction());
        stateMachine.addNoRunItem(getConnectDotsMenuItem());
        stateMachine.addNoRunItem(getBigDotsMenuItem());
        stateMachine.addNoRunItem(getGridLinesMenuItem());

        stateMachine.addNoRunItem(shiftedPlotMenuItem);
        shiftedPlotMenuItem.setSelected(true);

        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
    }

    protected JMenu createCommandMenu() {
        JMenu menu;

        menu = new JMenu("Command");
        menu.setMnemonic(KeyEvent.VK_C);

        menu.add(new JMenuItem(startAction));
        menu.add(new JMenuItem(stopAction));

        menu.addSeparator();

        menu.add(new JMenuItem(clearAction));

        return menu;
    }

    protected JMenu createPlotMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Plot");

        ButtonGroup group;

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Shifted plot");
        menuItem.setMnemonic(KeyEvent.VK_N);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                stateMachine.parseInput(UserMenuInput.shiftMode);
            }
        });
        shiftedPlotMenuItem = menuItem;

        menuItem = new JRadioButtonMenuItem("Cobweb animation");
        menuItem.setMnemonic(KeyEvent.VK_T);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                stateMachine.parseInput(UserMenuInput.cowebMode);
            }
        });
        cowebPlotMenuItem = menuItem;

        //menu.addSeparator();

        //defaultsMenu = createDefaultsMenu();
        //menu.add(defaultsMenu);

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

        toolBar.addSeparator();

        button = new JButton(clearAction);

        toolBar.add(button);

        toolBar.addSeparator();
        toolBar.addSeparator();

        slider = new JSlider(0, 50, 10);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);

        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(final ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();

                float value = s.getValue();
                float fdelay = (10000 / (500 - 5 * value) - 20);
                int idelay = (int) fdelay;

                ((CowebManager) getManager()).setDelay(s.getValue());
            }
        });
        toolBar.add(slider);

        return toolBar;
    }


    /*
     * Defaults
     */
    protected void fillDefaults(final int index) {
    }

    // helper
//    private void setAutomaticBounds(final boolean flag) {
//        autoBoundsMenuItem.setSelected(flag);
//        manualBoundsMenuItem.setSelected(!flag);
//        controlForm.setAutomaticBounds(flag);
//    }

    /**
     * @return Returns the continueMenuItem.
     */
//    JMenuItem getContinueMenuItem() {
//        return continueMenuItem;
//    }
//    JButton getContinueButton() {
//        return continueButton;
//    }

    /**
     * @return Returns the normalPlotMenuItem.
     */
    public JMenuItem getCowebPlotMenuItem() {
        return cowebPlotMenuItem;
    }

    /**
     * @return
     */
    public Action getStartAction() {
        return startAction;
    }

    public Action getStopAction() {
        return stopAction;
    }

    public Action getClearAction() {
        return clearAction;
    }

    /**
     * @return
     */
    public int getDelayValue() {
        return slider.getValue();
    }

    public JSlider getSlider() {
        return slider; 
    }
    
    public Model getModel() {
        return model;
    }
    
    public void callUponStart() {
    }
    
}

