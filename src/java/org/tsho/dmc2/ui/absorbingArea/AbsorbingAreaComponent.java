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
package org.tsho.dmc2.ui.absorbingArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import org.tsho.dmc2.core.chart.AbsorbingAreaRenderer;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.managers.AbsorbingAreaManager;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.ui.AbstractPlotComponent;
import org.tsho.dmc2.ui.MainFrame;

public class AbsorbingAreaComponent extends AbstractPlotComponent
implements AbsorbingAreaSMItf {
    
    private final AbsorbingAreaControlForm2 privateControlForm;
    private AbsorbingAreaManager manager;
    
    private final Action startAction = new StartAction();//not used eventually in Absorbing Area part
    private final Action plotCriticalSetAction = new PlotCriticalSetAction();
    private final Action stopAction = new StopAction();
    private final Action CustomClearAction = new CustomClearAction();
    private final Action plotAttractorAction=new PlotAttractorAction();
    private final Action chooseSegmentsAction=new ChooseSegmentsAction();
    private final Action hideAttractorAction=new HideAttractorAction();
    private final Action iterateChosenSegmentsAction=new IterateChosenSegmentsAction();
    
    private JMenuItem plotCriticalSetItem;
    
    
    public Action getPlotCriticalSetAction(){
        return plotCriticalSetAction;
    }
    
    public Action getStartAction(){
        return startAction;
    }
    
    public Action getStopAction(){
        return stopAction;
    }
    
    public Action getClearAction(){
        return CustomClearAction;
    }
    
    public Action getPlotAttractorAction(){
        return plotAttractorAction;
    }
    
    public Action getChooseSegmentsAction(){
        return chooseSegmentsAction;
    }
    
    public Action getHideAttractorAction(){
        return hideAttractorAction;
    }
    
    public Action getIterateChosenSegmentsAction(){
        return iterateChosenSegmentsAction;
    }
    
    public AbsorbingAreaComponent(final DifferentiableMap model,MainFrame mainFrame) {
        super(model,mainFrame);
        
        
        privateControlForm = new AbsorbingAreaControlForm2(model,this);
        controlForm = privateControlForm;
        
        manager=new AbsorbingAreaManager(model, privateControlForm, this);
        init(manager,
        new AbsorbingAreaFrameSM(this),
        controlForm, "Absorbing area");
        
        stateMachine.addSensibleItem(controlForm);
        stateMachine.parseInput(Input.go);
        finishInit(controlForm);
        //disable all commands except for 'plot critical set' and 'clear'
        this.getStopAction().setEnabled(false);
        this.getIterateChosenSegmentsAction().setEnabled(false);
        this.getChooseSegmentsAction().setEnabled(false);
        this.getPlotAttractorAction().setEnabled(false);
        this.getHideAttractorAction().setEnabled(false);
    }
    
    protected JMenu createPlotMenu() {
        JMenu menu;
        menu = new JMenu("Plot");
        
        JMenuItem menuItem = new JMenuItem("Plot");
        menuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                
            }
        });
        
        return menu;
    }
    
    public JMenu createCommandMenu() {
        JMenu menu;
        JMenuItem menuItem;
        
        /* Command menu */
        menu = new JMenu("Command");
        
        /* plotCriticalSet */
        menuItem = new JMenuItem();
        menuItem.setAction(plotCriticalSetAction);
        menu.add(menuItem);
        plotCriticalSetItem=menuItem;
        
        menuItem = new JMenuItem();
        menuItem.setAction(plotAttractorAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setAction(hideAttractorAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setAction(chooseSegmentsAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setAction(iterateChosenSegmentsAction);
        menu.add(menuItem);
        
        /* stop */
        menuItem = new JMenuItem();
        menuItem.setAction(stopAction);
        menu.add(menuItem);
        
        menuItem = new JMenuItem();
        menuItem.setAction(CustomClearAction);
        menu.add(menuItem);
        
        /* Plot menu */
        //menu = createPlotMenu();
        
        return menu;
    }
    
    public JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        
        JButton button;
        button = new JButton(plotCriticalSetAction);
        toolBar.add(button);
        
        button = new JButton(stopAction);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        button = new JButton(CustomClearAction);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        button=new JButton(plotAttractorAction);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        button=new JButton(hideAttractorAction);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        button=new JButton(chooseSegmentsAction);
        toolBar.add(button);
        
        toolBar.addSeparator();
        
        button=new JButton(iterateChosenSegmentsAction);
        toolBar.add(button);
        
        return toolBar;
    }
    
    protected void fillDefaults(int index) {}
    
    public void callUponStart() {
    }
    
    public void changeType(AbsorbingAreaRenderer plotRenderer,AbsorbingAreaControlForm2 controlForm, int type){
        
    }
    
    private class ChooseSegmentsAction extends AbstractAction {
        public ChooseSegmentsAction() {
            super();
            
            putValue(NAME, "Choose segments");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(SHORT_DESCRIPTION, "choose segments of critical set");
        }
        
        public void actionPerformed(final ActionEvent e) {
            //stateMachine.parseInput(AbsorbingAreaUserActionInput.chooseSegments);
            manager.getPlotRenderer().setChooseSegments();
            stateMachine.parseInput(UserActionInput.start);
            
            //invoke directly the corresponding renderer method
            //manager.getPlotRenderer().doChooseSegments();
        }
    }
    
    private class PlotAttractorAction extends AbstractAction {
        public PlotAttractorAction() {
            super();
            
            putValue(NAME, "Plot attractor");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
            putValue(SHORT_DESCRIPTION, "plot attractor");
        }
        
        public void actionPerformed(final ActionEvent e) {
            //stateMachine.parseInput(AbsorbingAreaUserActionInput.plotAttractor);
            manager.getPlotRenderer().setPlotAttractor();
            stateMachine.parseInput(UserActionInput.start);
            
        }
    }
    
    private class HideAttractorAction extends AbstractAction {
        public HideAttractorAction() {
            super();
            
            putValue(NAME, "Hide attractor");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
            putValue(SHORT_DESCRIPTION, "hide the plotted attractor");
        }
        
        public void actionPerformed(final ActionEvent e) {
            //stateMachine.parseInput(AbsorbingAreaUserActionInput.iterateCriticalSet);
            manager.getPlotRenderer().setHideAttractor();
            stateMachine.parseInput(UserActionInput.start);
        }
    }
    
    private class IterateChosenSegmentsAction extends AbstractAction {
        public IterateChosenSegmentsAction() {
            super();
            
            putValue(NAME, "Iterate chosen segments");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
            putValue(SHORT_DESCRIPTION, "iterate the chosen segments of critical set");
        }
        
        public void actionPerformed(final ActionEvent e) {
            
            //stateMachine.parseInput(AbsorbingAreaUserActionInput.iterateChosenSegments);
            manager.getPlotRenderer().setIterateChosenSegments();
            stateMachine.parseInput(UserActionInput.start);
            
        }
    }
    
    private class PlotCriticalSetAction extends AbstractAction {
        public PlotCriticalSetAction() {
            super();
            putValue(NAME, "Plot critical set");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(SHORT_DESCRIPTION, "plot critical set");
        }
        
        public void actionPerformed(final ActionEvent e) {
            manager.getPlotRenderer().setFindCriticalSetAgain(true);
            stateMachine.parseInput(UserActionInput.start);
        }
    }
    
    private class CustomClearAction extends AbstractAction{
        
        public  CustomClearAction(){
            super();
            putValue(NAME, "Clear");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(SHORT_DESCRIPTION, "clear display");
        }
        
        public void actionPerformed(ActionEvent e) {
            //tell renderer to clear the stored display to avoid reploting it next time
            if (!manager.getPlotRenderer().getNotYetRendered()){
                manager.getPlotRenderer().setJustCleared();
                manager.getPlotRenderer().resetSegmentsIteratesCount();
                stateMachine.parseInput(UserActionInput.start);
            }
        }
        
    }
}