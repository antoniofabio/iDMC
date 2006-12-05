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
package org.tsho.dmc2.ui.cycles;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.managers.CyclesManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class CyclesComponent extends AbstractPlotComponent
                        implements CyclesSMItf {

    private final CyclesControlForm2 privateControlForm;

    private final Action startAction = new StartAction();
    private final Action stopAction = new StopAction();
    private final Action clearAction = new ClearAction();

    public CyclesComponent(final SimpleMap model, MainFrame mainFrame) {
        super(model,mainFrame);

        controlForm = privateControlForm = new CyclesControlForm2(model,this);

        init(new CyclesManager(model, privateControlForm, this),
             new CyclesFrameSM(this),
             controlForm, "Cycles");

        stateMachine.addSensibleItem(controlForm);
        stateMachine.addSensibleItem(getBigDotsMenuItem());
        stateMachine.parseInput(Input.go);
        getBigDotsMenuItem().setSelected(true);
        ((CyclesManager) getManager()).setBigDots(true);
        finishInit(controlForm);
    }

    protected JMenu createPlotMenu() {
        JMenu menu;

        menu = new JMenu("Plot");

        //menu.add(createDefaultsMenu());

        return menu;
    }

    public JMenu createCommandMenu() {
        JMenu menu;
        JMenuItem menuItem;

        /* Command menu */
        menu = new JMenu("Command");

        /* start */
        menuItem = new JMenuItem();
        menuItem.setAction(startAction);
        menu.add(menuItem);

        /* stop */
        menuItem = new JMenuItem();
        menuItem.setAction(stopAction);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setAction(clearAction);
        menu.add(menuItem);

        /* Plot menu */
        //menu = createPlotMenu();

        return menu;
    }

    public JToolBar createToolBar() {
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

    protected void fillDefaults(int index) {}

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
    
    public void callUponStart() {
    }
    
}
