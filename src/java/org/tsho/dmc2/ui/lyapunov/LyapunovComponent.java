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
package org.tsho.dmc2.ui.lyapunov;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;

import org.jfree.data.Range;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.managers.LyapunovManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;
import org.tsho.dmc2.core.model.ODE;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class LyapunovComponent extends AbstractPlotComponent
implements LyapunovSMItf {
    
    private final LyapunovControlForm2 privateControlForm;
    
    private final Action startAction = new StartAction();
    private final Action stopAction = new StopAction();
    private final Action clearAction = new ClearAction();
    
    private LyapunovFrameSM lyapunovFrameSM;
    
    
    public LyapunovComponent(Model model, MainFrame mainFrame) {
        super(model,mainFrame);
        
        privateControlForm = new LyapunovControlForm2(model,this);
        controlForm = privateControlForm;
        
        lyapunovFrameSM=new LyapunovFrameSM(this);
        init(new LyapunovManager(this),
        lyapunovFrameSM, privateControlForm,
        "Lyapunov exponents");
        
        privateControlForm.setType(LyapunovControlForm2.TYPE_VS_PAR);
        ((LyapunovManager) getManager()).setType(LyapunovControlForm2.TYPE_VS_PAR);
        
        getConnectDotsMenuItem().setSelected(true);
        ((LyapunovManager) getManager()).setConnectDots(true);
        
        stateMachine.addSensibleItem(privateControlForm);
        stateMachine.addSensibleItem(getGridLinesMenuItem());
        stateMachine.addSensibleItem(getBigDotsMenuItem());
        stateMachine.addSensibleItem(getConnectDotsMenuItem());
        stateMachine.parseInput(Input.go);
        finishInit(privateControlForm);
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
    
    
    protected JMenu createPlotMenu() {
        
        JMenu menu;
        JMenuItem menuItem;
        ButtonGroup group;
        
        menu = new JMenu("Plot");
        menu.setMnemonic(KeyEvent.VK_P);
        
        
        group = new ButtonGroup();
        
        menuItem = new JRadioButtonMenuItem("Parameter");
        menuItem.setMnemonic(KeyEvent.VK_P);
        group.add(menuItem);
        menuItem.setSelected(true);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setType(LyapunovControlForm2.TYPE_VS_PAR);
                callClearAction(lyapunovFrameSM);
                while (!lyapunovFrameSM.getState().toString().equals("ready")){
                    try{
                        Thread.sleep(100);
                    }
                    catch(Exception ee){
                    }
                }
                callClearAction(lyapunovFrameSM);
                privateControlForm.updateSamplesMenu();
            }
        });
        if (model.getNPar() > 0) {
            stateMachine.addSensibleItem(menuItem);
        }
        else {
            menuItem.setEnabled(false);
        }
        
       
        menuItem = new JRadioButtonMenuItem("Time");
        menuItem.setMnemonic(KeyEvent.VK_I);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setType(LyapunovControlForm2.TYPE_VS_TIME);
                callClearAction(lyapunovFrameSM);
                while (!lyapunovFrameSM.getState().toString().equals("ready")){
                    try{
                        Thread.sleep(100);
                    }
                    catch(Exception ee){
                    }
                }
                callClearAction(lyapunovFrameSM);
                privateControlForm.updateSamplesMenu();
            }
        });
        stateMachine.addSensibleItem(menuItem);
        
        menuItem = new JRadioButtonMenuItem("Parameter space");
        menuItem.setMnemonic(KeyEvent.VK_P);
        group.add(menuItem);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setType(LyapunovControlForm2.TYPE_PAR_SPACE);
                callClearAction(lyapunovFrameSM);
                while (!lyapunovFrameSM.getState().toString().equals("ready")){
                    try{
                        Thread.sleep(100);
                    }
                    catch(Exception ee){
                    }
                }
                callClearAction(lyapunovFrameSM);
                privateControlForm.updateSamplesMenu();
            }
        });
        //? 12.7.2004
        if (model.getNPar() < 2) {
            //?
            menuItem.setEnabled(false);
        }
        else {
        	menuItem.setEnabled(true);
            stateMachine.addSensibleItem(menuItem);
        }
        
        return menu;
    }
    
    private void setType(byte type) {
        LyapunovManager manager = (LyapunovManager) getManager();
        
        privateControlForm.setType(type);
        manager.setType(type);
        
        switch (type) {
            case LyapunovControlForm2.TYPE_VS_PAR:
            case LyapunovControlForm2.TYPE_VS_TIME:
                getConnectDotsMenuItem().setEnabled(true);
                getGridLinesMenuItem().setEnabled(true);
                getBigDotsMenuItem().setEnabled(true);
                break;
                
            case LyapunovControlForm2.TYPE_PAR_SPACE:
                getConnectDotsMenuItem().setEnabled(false);
                getGridLinesMenuItem().setEnabled(false);
                getBigDotsMenuItem().setEnabled(false);
                break;
        }
    }
    
    protected void fillDefaults(final int index) {
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
    
    public void callUponStart(){
        
    }
}
