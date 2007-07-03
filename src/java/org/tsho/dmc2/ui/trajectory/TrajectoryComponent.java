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
package org.tsho.dmc2.ui.trajectory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.Range;
import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.core.util.Dataset;
import org.tsho.dmc2.managers.ManagerListener2;
import org.tsho.dmc2.managers.TrajectoryManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;
import org.tsho.dmc2.ui.components.DmcAction;
import org.tsho.dmc2.ui.components.DmcButton;
import org.tsho.dmc2.ui.components.DmcMenuItem;


/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public final class TrajectoryComponent extends AbstractPlotComponent
                                implements ManagerListener2,
                                TrajectorySMItf {

    private final TrajectoryControlForm2 privateControlForm;

    JSlider slider;

    // TODO make a custom action that changes the visibilty
    // via properties
    /*
     * These are here because the machine needs to change
     * their visibility.
     */

    private JMenuItem autoBoundsMenuItem;
    private JMenuItem manualBoundsMenuItem;
    private JMenuItem normalPlotMenuItem;
    private JMenuItem timePlotMenuItem;
    private JMenuItem variationMenuItem;
    
    /* actions */
    private final Action startAction = new StartAction();
    private final Action stopAction = new StopAction();
    private final DmcAction continueAction = new ContinueAction();
    private final DmcAction redrawAction = new RedrawAction();
    private final Action clearAction = new ClearAction();
    private final DmcAction resetAction = new ResetAction();

    /* defaults */
    boolean autoBoundsDefault = true;
    int plotTypeDefault;

    
    
    public TrajectoryComponent(final Model model, MainFrame mainFrame) {
        super(model,mainFrame,new Dataset(model.getVarNames()));
        defaultsSection = ModelDefaults.SCATTER_SECTION;

        controlForm = privateControlForm = new TrajectoryControlForm2(model,this);

        init(new TrajectoryManager(this, privateControlForm),
             new TrajectorySM(this), controlForm,  "Trajectory");

        stateMachine.addSensibleItem(autoBoundsMenuItem);
        stateMachine.addSensibleItem(manualBoundsMenuItem);
        stateMachine.addSensibleItem(variationMenuItem);

        stateMachine.addNoRunItem(clearAction);

        stateMachine.addNoRunItem(getTransparencyAction());
        stateMachine.addNoRunItem(getConnectDotsMenuItem());
        stateMachine.addNoRunItem(getBigDotsMenuItem());
        stateMachine.addNoRunItem(getGridLinesMenuItem());

        variationMenuItem.setSelected(false);

        if (model instanceof SimpleMap) {
            getConnectDotsMenuItem().setSelected(false);
        }
        else if (model instanceof ODE) {
            getConnectDotsMenuItem().setSelected(true);
        }

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

        menu.add(new DmcMenuItem(continueAction));
        menu.add(new DmcMenuItem(redrawAction));
        menu.add(new DmcMenuItem(resetAction));
        menu.add(new JMenuItem(clearAction));

        return menu;
    }

    protected JMenu createPlotMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Plot");

        ButtonGroup group;

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("State space plot");
        menuItem.setMnemonic(KeyEvent.VK_N);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                stateMachine.parseInput(UserMenuInput.normalMode);
                controlForm.updateSamplesMenu();
                getSaveDataAction().setEnabled(true);
            }
        });
        normalPlotMenuItem = menuItem;

        menuItem = new JRadioButtonMenuItem("Time plot");
        menuItem.setMnemonic(KeyEvent.VK_T);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                stateMachine.parseInput(UserMenuInput.timeMode);
                controlForm.updateSamplesMenu();
                getSaveDataAction().setEnabled(true);
            }
        });
        timePlotMenuItem = menuItem;

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem("Variation");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                boolean state;
                state = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                privateControlForm.setVariation(state);
                privateControlForm.updateSamplesMenu();
                getSaveDataAction().setEnabled(false);
            }
        });
        variationMenuItem = menuItem;

        menu.addSeparator();

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Automatic bounds");
        menuItem.setMnemonic(KeyEvent.VK_A);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                privateControlForm.setAutoRanges(true);
                privateControlForm.updateSamplesMenu();
            }
        });
        autoBoundsMenuItem = menuItem;

        menuItem = new JRadioButtonMenuItem("Manual bounds");
        menuItem.setMnemonic(KeyEvent.VK_M);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                privateControlForm.setAutoRanges(false);
                controlForm.updateSamplesMenu();
            }
        });
        manualBoundsMenuItem = menuItem;
        setAutomaticBounds(autoBoundsDefault);

        /* Step Function */

        class SetStepFunctionPerform implements ActionListener {
                private int i;

                SetStepFunctionPerform(final int i) {
                    this.i = i;
                }

                public void actionPerformed(final ActionEvent e) {
                   ((TrajectoryManager) manager).setOdeStepFunction(i);
                }
        }

        if (model instanceof ODE) {

            menu.addSeparator();
           
            JMenu stepFuctionMenu = new JMenu("Step function");
            stepFuctionMenu.setMnemonic(KeyEvent.VK_S);
                                                                                                                        
            group = new ButtonGroup();
                                                                                                                        
            int i = 0;
            do {
                String name = Lua.getStepFunctionName(i);
                if (name == null) break;
                                                                                                                        
                menuItem = new JRadioButtonMenuItem(name);
                group.add(menuItem);
                stepFuctionMenu.add(menuItem);
                                                                                                                        
                menuItem.setToolTipText(Lua.getStepFunctionDescription(i));
                                                                                                                        
                if (name.equals("rkf45")) {
                    menuItem.setSelected(true);
                }
                menuItem.addActionListener(new SetStepFunctionPerform(i));
                i++;
                                                                                                                        
            } while (true);
            menu.add(stepFuctionMenu);
            stateMachine.addNoRunItem(stepFuctionMenu);
        }

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

        button = new DmcButton(continueAction);
        toolBar.add(button);

        button = new DmcButton(redrawAction);
        toolBar.add(button);

        toolBar.addSeparator();

        button = new DmcButton(resetAction);

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
                ((TrajectoryManager) getManager()).setDelay(s.getValue());
            }
        });
        toolBar.add(slider);
        slider.setValue(0);

        return toolBar;
    }


    /*
     * Defaults
     */
    protected void fillDefaults(final int index) {
        VariableItems items;
        VariableItems.Iterator ii;
        VariableDoubles doubles;
        VariableDoubles.Iterator di;

        /* initialization is important: if a key is not present use
         * the default */
        double hMin = 1, hMax = -1, vMin = 1, vMax = -1;
        boolean autoBoundsState = autoBoundsDefault;

        items = Lua.loadDefaults(model, defaultsSection, index);

        /* parameters */
        doubles = new VariableDoubles(model.getParNames());
        di = doubles.iterator();
        while (di.hasNext()) {
            if (items.containsLabel(di.nextLabel())) {
                double value = Double.parseDouble((String)items.get(di.label()));
                doubles.put(di.label(), value);
            }
        }
        privateControlForm.setParameterValues(doubles);

        /* variables */
        doubles = new VariableDoubles(model.getVarNames());
        di = doubles.iterator();
        while (di.hasNext()) {
            if (items.containsLabel(di.nextLabel())) {
                double value =
                        Double.parseDouble((String) items.get(di.label()));
                doubles.put(di.label(), value);
            }
        }
        privateControlForm.setInitialValues(doubles);

        /* other values */
        ii = items.iterator();
        while (ii.hasNext()) {
            String key = ii.nextLabel();
            String value = (String) items.get(ii.label());
            double doubleValue;

            System.out.println("key: " + key + ", value: " + value);

            if (key.equals(ModelDefaults.ITERATIONS_KEY)) {
                doubleValue = Double.parseDouble(value);
                privateControlForm.setIterations((int) doubleValue);
            }

            else if (key.equals(ModelDefaults.TRANSIENTS_KEY)) {
                doubleValue = Double.parseDouble(value);
                privateControlForm.setTransients((int) doubleValue);
            }

            else if (key.equals(ModelDefaults.SCATTER_AUTOBOUNDS_ITERATIONS_KEY)) {
                doubleValue = Double.parseDouble(value);
                privateControlForm.setRangeIterations((int) doubleValue);
            }

            /* manual bounds */
            else if (key.equals(ModelDefaults.SCATTER_BOUNDS_KEY)) {
                if (value.equals(ModelDefaults.SCATTER_BOUNDS_AUTO_VALUE)) {
                    autoBoundsState = true;
                }
                else if (value.equals(ModelDefaults.SCATTER_BOUNDS_MANUAL_VALUE)) {
                    autoBoundsState = false;
                }
            }
            else if (key.equals(ModelDefaults.SCATTER_AUTO_BOUNDS_HMIN_KEY)) {
                hMin = Double.parseDouble(value);
            }
            else if (key.equals(ModelDefaults.SCATTER_AUTO_BOUNDS_HMAX_KEY)) {
                hMax = Double.parseDouble(value);
            }
            else if (key.equals(ModelDefaults.SCATTER_AUTO_BOUNDS_VMIN_KEY)) {
                vMin = Double.parseDouble(value);
            }
            else if (key.equals(ModelDefaults.SCATTER_AUTO_BOUNDS_VMAX_KEY)) {
                vMax = Double.parseDouble(value);
            }

        }

        /* post processing */
        setAutomaticBounds(autoBoundsState);

        Range range = null;
        try {
            range = new Range(hMin, hMax);
        } catch (IllegalArgumentException e) {
            range = null;
        } finally {
            if (range != null) {
            	privateControlForm.setXRange(range);
            }
        }

        range = null;
        try {
            range = new Range(vMin, vMax);
        } catch (IllegalArgumentException e) {
            range = null;
        } finally {
            if (range != null) {
            	privateControlForm.setYRange(range);
            }
        }
    }


    // helper
    private void setAutomaticBounds(final boolean flag) {
        autoBoundsMenuItem.setSelected(flag);
        manualBoundsMenuItem.setSelected(!flag);
        privateControlForm.setAutoRanges(flag);
    }

    /**
     * @return Returns the normalPlotMenuItem.
     */
    public JMenuItem getNormalPlotMenuItem() {
        return normalPlotMenuItem;
    }

    /**
     * @return Returns the timePlotMenuItem.
     */
    public JMenuItem getTimePlotMenuItem() {
        return timePlotMenuItem;
    }

    /**
     * @return
     */
    public DmcAction getContinueAction() {
        return continueAction;
    }

    /**
     * @return
     */
    public DmcAction getRedrawAction() {
        return redrawAction;
    }

    /**
     * @return
     */
    public DmcAction getResetAction() {
        return resetAction;
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

