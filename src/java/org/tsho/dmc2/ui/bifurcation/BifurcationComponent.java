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
import javax.swing.JToolBar;

import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.managers.BifurcationManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class BifurcationComponent extends AbstractPlotComponent 
                                  implements BifurcationSMItf {

    //private final BifurcationManager manager;
    private final BifurcationControlForm2 privateControlForm;

    //private final BifurcationFrameSM myStateMachine;

    /* special menuitems */
    private JMenuItem singleParameterMenuItem;
    private JMenuItem doubleParameterMenuItem;

    private StartAction startAction = new StartAction();
    private StopAction stopAction = new StopAction();
    private RedrawAction redrawAction = new RedrawAction();
    private ClearAction clearAction = new ClearAction();
    private Model model;
    
    byte plotTypeDefault;


    public BifurcationComponent(final Model model, MainFrame mainFrame) {
        super(model,mainFrame);

        this.model=model;
        defaultsSection = ModelDefaults.BIFURCATION_SECTION;

        controlForm = privateControlForm = new BifurcationControlForm2(model,this);

        plotTypeDefault = BifurcationControlForm2.TYPE_SINGLE;

        init(new BifurcationManager(model, privateControlForm, this),
             new BifurcationFrameSM((BifurcationSMItf) this),
             controlForm, "Bifurcation");

        stateMachine.addSensibleItem(controlForm);

        stateMachine.addNoRunItem(getGridLinesMenuItem());
        stateMachine.addNoRunItem(getTransparencyAction());
        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
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

        /* Redraw */
        menuItem = new JMenuItem(redrawAction);
        menu.add(menuItem);

//        /* Reset */
//        menuItem = new JMenuItem(resetAction);
//        menu.add(menuItem);

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

        menuItem = new JCheckBoxMenuItem("Fixed initial point");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menu.add(menuItem);
        menuItem.setSelected(true);
        ((BifurcationManager) getManager()).setFixedInitialPoint(true);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                ((BifurcationManager) getManager()).setFixedInitialPoint(
                        ((JCheckBoxMenuItem) e.getSource()).isSelected());
            }
        });
        stateMachine.addSensibleItem(menuItem);

        menu.addSeparator();

        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Single parameter");
        menuItem.setSelected(true);
        menuItem.setMnemonic(KeyEvent.VK_S);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setPlotType(BifurcationControlForm2.TYPE_SINGLE);
                controlForm.updateSamplesMenu();
            }
        });
        stateMachine.addSensibleItem(menuItem);
        singleParameterMenuItem = menuItem;

        menuItem = new JRadioButtonMenuItem("Double parameter");
        menuItem.setMnemonic(KeyEvent.VK_D);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setPlotType(BifurcationControlForm2.TYPE_DOUBLE);
                controlForm.updateSamplesMenu();
            }
        });
        if (model.getNPar() <= 1) {
            menuItem.setEnabled(false);
        }
        else {
            stateMachine.addSensibleItem(menuItem);
        }
        doubleParameterMenuItem = menuItem;

        setPlotType(plotTypeDefault); // set default
        
        
        class SetStepFunctionPerform implements ActionListener {
                private int i;

                SetStepFunctionPerform(final int i) {
                    this.i = i;
                }

                public void actionPerformed(final ActionEvent e) {
                   ((BifurcationManager) manager).setOdeStepFunction(i);
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
                                                                                                                        
                if (i == 4) {
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

    private void setPlotType(final byte type) {
        switch (type) {

            case BifurcationControlForm2.TYPE_SINGLE:
                singleParameterMenuItem.setSelected(true);
            getGridLinesMenuItem().setEnabled(true);
                break;

            case BifurcationControlForm2.TYPE_DOUBLE:
                doubleParameterMenuItem.setSelected(true);
                getGridLinesMenuItem().setEnabled(false);
                break;

            default:
                //Modification: line below commented
                //assert false;
        }

        privateControlForm.setType(type);
    }


    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton button;
        button = new JButton(startAction);
        toolBar.add(button);

        button = new JButton(stopAction);
        toolBar.add(button);

        toolBar.addSeparator();

        button = new JButton(redrawAction);
        toolBar.add(button);

        toolBar.addSeparator();

        button = new JButton(clearAction);
        toolBar.add(button);

//        toolBar.addSeparator();
//
//        button = new JButton(clearAction);
//        button.setToolTipText(clearAction.getTooltipText());
//        toolBar.add(button);

        return toolBar;
    }

    /*
     * Defaults
     */
    protected void fillDefaults(int index) {
    }

    public Action getClearAction() {
        return clearAction;
    }

    public Action getRedrawAction() {
        return redrawAction;
    }

    public Action getStartAction() {
        return startAction;
    }

    public Action getStopAction() {
        return stopAction;
    }

    JMenuItem getDoubleParameterMenuItem() {
        return doubleParameterMenuItem;
    }

    JMenuItem getSingleParameterMenuItem() {
        return singleParameterMenuItem;
    }
    
    public void callUponStart(){ 
    }
}
