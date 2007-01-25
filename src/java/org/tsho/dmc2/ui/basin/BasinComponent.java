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
package org.tsho.dmc2.ui.basin;

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
import org.tsho.dmc2.core.chart.BasinRenderer;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.managers.BasinManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;
import org.tsho.dmc2.core.util.*;
import org.tsho.dmc2.core.chart.ColorSettings;

public class BasinComponent extends AbstractPlotComponent
                        implements BasinSMItf {

    private final BasinControlForm2 privateControlForm;
    private BasinManager manager;

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
            BasinRenderer plotRenderer = manager.getPlotRenderer();            	
        	ColorSettingsDialog csd = new ColorSettingsDialog(
        			plotRenderer.getColorSettings());
        	csd.show();
        	plotRenderer.setColorSettings(csd.getColorSettings());
        	if(plotRenderer.getGrid()!=null) {//some data already computed
        		plotRenderer.drawImage();
        		manager.getChartPanel().repaint();
        	}    		
    	}
    };
    private final Action colorSettingsAction = new ColorSettingsAction();

    public BasinComponent(final SimpleMap model,MainFrame mainFrame) {
        super(model,mainFrame);

        privateControlForm = new BasinControlForm2(model,this);
        controlForm = privateControlForm;

        manager = new BasinManager(this);
        init(manager,
             new BasinFrameSM(this),
             controlForm, "Basin of attraction");

        stateMachine.addSensibleItem(controlForm);
        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
    }

    protected JMenu createPlotMenu() {
        JMenu menu;
        menu = new JMenu("Plot");
        
        ButtonGroup group;

        group = new ButtonGroup();
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem("Fast algorithm");
        menuItem.setMnemonic(KeyEvent.VK_F);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                //change flag in basin control form and in basin renderer
                BasinRenderer plotRenderer=manager.getPlotRenderer();
                plotRenderer.setType(BasinRenderer.FAST_ALGORITHM);
                privateControlForm.setType(BasinRenderer.FAST_ALGORITHM);
                controlForm.updateSamplesMenu();
            }
        });
        menuItem.setSelected(true);

        menuItem = new JRadioButtonMenuItem("Slow algorithm");
        menuItem.setMnemonic(KeyEvent.VK_S);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                //change flag in basin control form and in basin renderer
                BasinRenderer plotRenderer=manager.getPlotRenderer();
                plotRenderer.setType(BasinRenderer.SLOW_ALGORITHM);
                privateControlForm.setType(BasinRenderer.SLOW_ALGORITHM);
                privateControlForm.updateSamplesMenu();
            }
        });
        
        menuItem = new JRadioButtonMenuItem("Native algorithm");
        menuItem.setMnemonic(KeyEvent.VK_N);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                //change flag in basin control form and in basin renderer
                BasinRenderer plotRenderer=manager.getPlotRenderer();
                plotRenderer.setType(BasinRenderer.NATIVE_ALGORITHM);
                privateControlForm.setType(BasinRenderer.NATIVE_ALGORITHM);
                controlForm.updateSamplesMenu();
            }
        });
        menuItem.setSelected(true);
        
        
        menu.addSeparator();
        
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
    
    public void changeType(BasinRenderer plotRenderer,BasinControlForm2 controlForm, int type){
        
    }

	/**
	 * @return the colorSettingsAction
	 */
	public Action getColorSettingsAction() {
		return colorSettingsAction;
	}
    
}
