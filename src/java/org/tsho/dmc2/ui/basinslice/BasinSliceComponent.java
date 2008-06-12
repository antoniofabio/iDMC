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
package org.tsho.dmc2.ui.basinslice;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jfree.data.Range;
import org.tsho.dmc2.core.chart.BasinSliceRenderer;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.managers.BasinSliceManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;
import org.tsho.dmc2.core.util.*;
import org.tsho.dmc2.ui.basin.ColorSettingsDialog;

public class BasinSliceComponent extends AbstractPlotComponent
                        implements BasinSliceSMItf {

    private final BasinSliceControlForm privateControlForm;
    private BasinSliceManager manager;

    private final Action startAction = new StartAction();
    private final Action stopAction = new StopAction();
    private final Action clearAction = new ClearAction();
    private class ColorSettingsAction extends AbstractAction {
    	public ColorSettingsAction() {
    		putValue(NAME, "Color settings...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(SHORT_DESCRIPTION, "Edit color settings");
    	}
    	public void actionPerformed(ActionEvent ae) {
            //TODO: FIXME
            /*
            BasinSliceRenderer plotRenderer = manager.getPlotRenderer();            	
        	ColorSettingsDialog csd = new ColorSettingsDialog(
        			plotRenderer.getColorSettings());
        	csd.show();
        	plotRenderer.setColorSettings(csd.getColorSettings());
        	if(plotRenderer.getGrid()!=null) {//some data already computed
        		plotRenderer.drawImage();
        		manager.getChartPanel().repaint();
        	}    		
            */
    	}
    };
    private final Action colorSettingsAction = new ColorSettingsAction();

    public BasinSliceComponent(final SimpleMap model,MainFrame mainFrame) {
        super(model,mainFrame);

        privateControlForm = new BasinSliceControlForm(model,this);
        controlForm = privateControlForm;

        manager = new BasinSliceManager(this);
        init(manager,
             new BasinSliceFrameSM(this),
             controlForm, "Basin of attraction slice");

        stateMachine.addSensibleItem(controlForm);
        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
    }

    protected JMenu createPlotMenu() {
        JMenu menu;
        menu = new JMenu("Plot");
                
        menu.add(colorSettingsAction);
                
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
    
	/**
	 * @return the colorSettingsAction
	 */
	public Action getColorSettingsAction() {
		return colorSettingsAction;
	}
    
}
