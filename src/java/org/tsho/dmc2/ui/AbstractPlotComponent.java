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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.dlua.LuaModel;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.util.SampleParser;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;
import org.tsho.dmc2.managers.AbstractManager;
import org.tsho.dmc2.managers.ManagerListener2;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.ui.components.DmcAction;
import org.tsho.dmc2.ui.SamplesSupplier;
import org.tsho.dmc2.core.util.*;

public abstract class AbstractPlotComponent extends JPanel
implements CoreStatusListener,
ManagerListener2, PlotComponent {
    
    protected Model model;
    protected DataObject dataobject;
    protected AbstractManager manager;
    protected ComponentStateMachine stateMachine;
    protected AbstractControlForm controlForm;
    
    private JComponent chart;
    private JScrollPane chartScrollPane;
    
    protected String defaultsSection = null;
    
    /* Plot menu items */
    private JMenu chartSizeSubMenu;
    private JMenuItem gridLinesMenuItem;
    private JMenuItem connectDotsMenuItem;
    private JMenuItem bigDotsMenuItem;
    
    private TransparencyAction transparencyAction;
    protected boolean transparencyDefault = false;
    
    // exported items
    private JToolBar statusBar;
    protected JMenu commandMenu;
    private JMenu optionsMenu;
    private JMenu plotMenu;
    private JPanel panel;
    
    //?
    private JMenu samplesMenu;
    private MainFrame mainFrame;
    //private boolean isChooseToRemove;
    //private boolean isSampleChosen;
    //?
    
    protected JToolBar toolBar;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private String progressString;
    private long progressCount;
    private SamplesMenuItemListener samplesMenuItemListener;
    private SamplesMenuListener samplesMenuListener;
    
    public AbstractPlotComponent(final Model model, MainFrame mainFrame) {
    	super();
    	commonConstructor(model, mainFrame, null);
    }
    
    public AbstractPlotComponent(final Model model, MainFrame mainFrame, DataObject dataobject) {
        super();
        commonConstructor(model, mainFrame, dataobject);
    }
    
    private void commonConstructor(final Model model, MainFrame mainFrame, DataObject dataobject) {
        this.mainFrame=mainFrame;
        this.model = model;
        this.dataobject = dataobject;
        samplesMenuItemListener=new SamplesMenuItemListener();
        samplesMenuListener=new SamplesMenuListener();    	
    }
    
    protected void init(
    final AbstractManager man,
    final ComponentStateMachine machine,
    final JComponent controlForm,
    final String title) {
        
        this.controlForm=(AbstractControlForm) controlForm;
        this.manager  = man;
        this.stateMachine = machine;
        
        createPanel(controlForm, title);
        
        optionsMenu = createOptionsMenu();
        commandMenu = createCommandMenu();
        plotMenu = createPlotMenu();
        
        statusBar = createStatusBar();
        toolBar = createToolBar();
        toolBar.setFloatable(false);
        
        // fit chart to window
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                resizeChart(chartScrollPane.getViewport().getExtentSize().getSize());
                chart.revalidate();
            }
        });
    }
    
    //actions to be taken only after all parameters are set
    public void finishInit(JComponent controlForm){
        //requires that all control forms inherit from AbstractControlForm
        samplesMenu= createSamplesMenu(((AbstractControlForm) controlForm).getFormType());
    }
    
    public void dispose() {
        manager.stopRendering();
        //stateMachine.parseInput()
        //stateMachine = null;
        manager = null;
        panel = null;
    }
    
    private void createPanel(
    final JComponent controlForm,
    final String title) {
        
        panel = this;
        
        panel.setDoubleBuffered(false);
        panel.setOpaque(true);
        panel.setName(title);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JComponent controlsScrollPane;
        JSplitPane splitPane;
        
        chart = manager.getChartPanel();
        
        chartScrollPane = createChartScrollPane(chart);
        
        controlsScrollPane = new JScrollPane(
        controlForm,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        controlsScrollPane,
        chartScrollPane);
        
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(
        (int) controlsScrollPane.getPreferredSize().getWidth() + 10);
        
        panel.add(splitPane);
        
    }
    
    private static JScrollPane createChartScrollPane(final JComponent chart) {
        Box hBox;
        Box vBox;
        
        vBox = new Box(BoxLayout.Y_AXIS);
        vBox.setBackground(Color.black);
        vBox.add(Box.createVerticalGlue());
        vBox.add(chart);
        vBox.add(Box.createVerticalGlue());
        
        hBox = new Box(BoxLayout.X_AXIS);
        hBox.add(Box.createHorizontalGlue());
        hBox.add(vBox);
        hBox.add(Box.createHorizontalGlue());
        
        return new JScrollPane(hBox,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    /*
     * Status bar
     */
    
    private JToolBar createStatusBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        tb.setLayout(gridbag);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setMaximum(99);
        progressBar.setVisible(false);
        
        //
        //statusLabel = new JTextField("");
        //?
        statusLabel = new JLabel("");//modified from :""
        //?
        //statusLabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 80;
        gridbag.setConstraints(statusLabel, c);
        tb.add(statusLabel);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 20;
        gridbag.setConstraints(progressBar, c);
        
        // make the status bar the same size the progressbar
        // to avoid "jumping". Should be the max between the label
        // and the progress bar.
        Dimension dim = progressBar.getPreferredSize();
        dim.width = -1;
        tb.setPreferredSize(dim);
        
        tb.add(progressBar);
        
        return tb;
    }
    
    public void progressPercent(final int p) {
        progressBar.setValue(p);
    }
    
    public void progressString(final String s) {
        progressString = s;
        updateProgressBar();
    }
    
    public void progressCount(final long c) {
        if (c <= 0) {
            progressCount = 0;
        }
        else {
            progressCount = c;
        }
        
        updateProgressBar();
    }
    
    private void updateProgressBar() {
        if (progressCount > 0) {
            // progressBar.setString(progressString + " - " + progressCount);
            statusLabel.setText(progressString + " " + progressCount);
        }
        else {
            // progressBar.setString(progressString);
            statusLabel.setText(progressString);
        }
    }
    
    /*
     * abstracts
     */
    protected abstract JToolBar createToolBar();
    protected abstract JMenu createCommandMenu();
    protected abstract JMenu createPlotMenu();
    
    protected abstract void fillDefaults(int index);
    
    
    protected JMenu createSamplesMenu(String plotType){
        JMenu menu=new JMenu("Samples");
        menu.addMenuListener(samplesMenuListener);
        JMenuItem menuItem;
        
        menuItem=new JMenuItem("Add sample");
        menu.add(menuItem);
        menuItem.addActionListener(samplesMenuItemListener);
        
        SamplesSupplier samplesSupplier=mainFrame.getSamplesSupplier();
        Vector sampleNames=samplesSupplier.menuSamples(controlForm.getFormType());
        if (sampleNames.size()>0){
            //isChooseToRemove=false;
            menuItem=new JMenuItem("Remove sample");
            menu.add(menuItem);
            menuItem.addActionListener(samplesMenuItemListener);
            menu.addSeparator();
            for (int i=0;i<sampleNames.size();i++){
                menuItem=new JMenuItem( SampleParser.toString((String) sampleNames.elementAt(i)) );
                menuItem.addActionListener(samplesMenuItemListener);
                menu.add(menuItem);
            }
        }
        
        return menu;
    }
    
    
    public void updateSamplesMenu(){
        JMenu menu=this.getSamplesMenu();
        JMenuItem menuItem;
        
        menu.removeAll();
        menuItem=new JMenuItem("Add sample");
        menu.add(menuItem);
        menuItem.addActionListener(samplesMenuItemListener);
        
        SamplesSupplier samplesSupplier=mainFrame.getSamplesSupplier();
        Vector sampleNames=samplesSupplier.menuSamples(controlForm.getFormType());
        if (sampleNames.size()>0){
            
            
            menuItem=new JMenuItem("Remove sample");
            menu.add(menuItem);
            menuItem.addActionListener(samplesMenuItemListener);
            
            menu.addSeparator();
            for (int i=0;i<sampleNames.size();i++){
                menuItem=new JMenuItem( SampleParser.toString((String) sampleNames.elementAt(i)) );
                menuItem.addActionListener(samplesMenuItemListener);
                menu.add(menuItem);
            }
        }
    }
    
    protected JMenu createOptionsMenu() {
        JMenu menu;
        JMenu subMenu;
        JMenuItem menuItem;
        
        menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_P);
        
        /*
         * Size
         */
        chartSizeSubMenu = new JMenu("Size");
        
        menuItem = new JMenuItem("500x500");
        menuItem.setMnemonic(KeyEvent.VK_5);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resizeChart(new Dimension(500, 500));
                chart.revalidate();
            }
        });
        
        menuItem = new JMenuItem("600x600");
        menuItem.setMnemonic(KeyEvent.VK_6);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resizeChart(new Dimension(600, 600));
                chart.revalidate();
            }
        });
        
        menuItem = new JMenuItem("Fit to window");
        menuItem.setMnemonic(KeyEvent.VK_F);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resizeChart(chartScrollPane.getViewport().getExtentSize().getSize());
                chart.revalidate();
            }
        });
        
        menuItem = new JMenuItem("Custom size...");
        //menuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Dimension d = chart.getPreferredSize();
                WindowsSizePanel pan = new WindowsSizePanel(d.width, d.height);
                int result = JOptionPane.showConfirmDialog(
                null, pan,
                "Change plot dimensions",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    d.setSize(pan.getWidth(), pan.getHeight());
                    resizeChart(d);
                    chart.revalidate();
                }
            }
        });
        chartSizeSubMenu.add(menuItem);
        
        stateMachine.addSensibleItem(chartSizeSubMenu);
        menu.add(chartSizeSubMenu);
        
        /*
         * Axes visibility
         */
        if (manager instanceof AbstractManager.AxesVisibility) {
            final AbstractManager.AxesVisibility myManager
            = (AbstractManager.AxesVisibility) manager;
            
            subMenu = new JMenu("Show axes");
            ButtonGroup group = new ButtonGroup();
            
            menuItem = new JRadioButtonMenuItem("Both");
            menuItem.setMnemonic(KeyEvent.VK_B);
            subMenu.add(menuItem);
            group.add(menuItem);
            if (myManager.isXAxisVisible() && myManager.isYAxisVisible()) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    myManager.setXAxisVisible(
                    ((JRadioButtonMenuItem) e.getSource()).isSelected());
                    myManager.setYAxisVisible(
                    ((JRadioButtonMenuItem) e.getSource()).isSelected());
                }
            });
            
            menuItem = new JRadioButtonMenuItem("None");
            menuItem.setMnemonic(KeyEvent.VK_N);
            subMenu.add(menuItem);
            group.add(menuItem);
            if (!myManager.isXAxisVisible() && !myManager.isYAxisVisible()) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    myManager.setXAxisVisible(
                    (!((JRadioButtonMenuItem) e.getSource()).isSelected()));
                    myManager.setYAxisVisible(
                    (!((JRadioButtonMenuItem) e.getSource()).isSelected()));
                }
            });
            
            menuItem = new JRadioButtonMenuItem("Domain only");
            menuItem.setMnemonic(KeyEvent.VK_D);
            subMenu.add(menuItem);
            group.add(menuItem);
            if (myManager.isXAxisVisible() && !myManager.isYAxisVisible()) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    myManager.setXAxisVisible(
                    (((JRadioButtonMenuItem) e.getSource()).isSelected()));
                    myManager.setYAxisVisible(
                    (!((JRadioButtonMenuItem) e.getSource()).isSelected()));
                }
            });
            
            menuItem = new JRadioButtonMenuItem("Range only");
            menuItem.setMnemonic(KeyEvent.VK_R);
            subMenu.add(menuItem);
            group.add(menuItem);
            if (!myManager.isXAxisVisible() && myManager.isYAxisVisible()) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    myManager.setXAxisVisible(
                    (!((JRadioButtonMenuItem) e.getSource()).isSelected()));
                    myManager.setYAxisVisible(
                    (((JRadioButtonMenuItem) e.getSource()).isSelected()));
                }
            });            

            menu.add(subMenu);
            stateMachine.addSensibleItem(subMenu);            
        }
        
        /*
         * Colors
         */
        
        subMenu = createColorsMenu();
        menu.add(subMenu);
        stateMachine.addNoRunItem(subMenu);
        
         /*
          * Transparency
          */
        // TODO get rid of those orrible actions!
        if (manager instanceof AbstractManager.Transparency) {
            
            menu.addSeparator();
            
            final AbstractManager.Transparency trManager =
            (AbstractManager.Transparency) manager;
            
            TransparencyFactorAction trfAction = new TransparencyFactorAction() {
                public void actionPerformed(final ActionEvent e) {
                    Float result;
                    // TODO parent should not be null
                    // and I should delegate this to someone else
                    result = showDialog(null, trManager.getAlphaValue());
                    if (result != null) {
                        trManager.setAlphaValue(result.floatValue());
                    }
                }
            };
            
            transparencyAction = new TransparencyAction() {
                public void actionPerformed(final ActionEvent e) {
                    boolean selected = ((AbstractButton) e.getSource()).isSelected();
                    trManager.setAlpha(selected);
                }
            };
            
            // follow the default
            trfAction.setEnabled(false);
            
            menuItem = new JCheckBoxMenuItem(transparencyAction);
            
            menuItem.addItemListener(trfAction);
            menuItem.setSelected(transparencyDefault);
            menu.add(menuItem);
            
            menuItem = new JMenuItem(trfAction);
            menu.add(menuItem);
        }
        
        // separator
        if (manager instanceof AbstractManager.GridLines
        || manager instanceof AbstractManager.ConnectDots
        || manager instanceof AbstractManager.BigDots
        || manager instanceof AbstractManager.Crosshair) {
            menu.addSeparator();
        }
        
         /*
          * Gridlines
          */
        if (manager instanceof AbstractManager.GridLines) {
            menuItem = new JCheckBoxMenuItem("Gridlines");
            menuItem.setMnemonic(KeyEvent.VK_G);
            menu.add(menuItem);
            menuItem.setSelected(((AbstractManager.GridLines)manager).isGridlines());
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((AbstractManager.GridLines)manager).setGridlines(
                    ((JCheckBoxMenuItem)e.getSource()).isSelected());
                }
            });
            //compState.setUpSensibleMenuItems(menuItem);
            menuItem.setSelected((
            (AbstractManager.GridLines) manager).isGridlines());
            gridLinesMenuItem = menuItem;
        }
        
         /*
          * Big dots
          */
        if (manager instanceof AbstractManager.BigDots) {
            menuItem = new JCheckBoxMenuItem("Big dots");
            menuItem.setMnemonic(KeyEvent.VK_B);
            menu.add(menuItem);
            menuItem.setSelected(((AbstractManager.BigDots)manager).isBigDots());
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((AbstractManager.BigDots)manager).setBigDots(
                    ((JCheckBoxMenuItem)e.getSource()).isSelected());
                }
            });
            menuItem.setSelected((
            (AbstractManager.BigDots) manager).isBigDots());
            bigDotsMenuItem = menuItem;
        }
        
         /*
          * Connect dots
          */
        if (manager instanceof AbstractManager.ConnectDots) {
            menuItem = new JCheckBoxMenuItem("Connect dots");
            menuItem.setMnemonic(KeyEvent.VK_C);
            menu.add(menuItem);
            menuItem.setSelected(((AbstractManager.ConnectDots)manager).isConnectDots());
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((AbstractManager.ConnectDots)manager).setConnectDots(
                    ((JCheckBoxMenuItem)e.getSource()).isSelected());
                }
            });
            menuItem.setSelected((
            (AbstractManager.ConnectDots) manager).isConnectDots());
            connectDotsMenuItem = menuItem;
        }
        
         /*
          * Crosshair
          */
        if (manager instanceof AbstractManager.Crosshair) {
            menuItem = new JCheckBoxMenuItem("Crosshair");
            menuItem.setMnemonic(KeyEvent.VK_B);
            menu.add(menuItem);
            menuItem.setSelected(((AbstractManager.Crosshair)manager).isCrosshair());
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((AbstractManager.Crosshair)manager).setCrosshair(
                    ((JCheckBoxMenuItem)e.getSource()).isSelected());
                }
            });
            menuItem.setSelected((
            (AbstractManager.Crosshair) manager).isCrosshair());
        }

        /*
         * Set rng seed
         * */
        menu.addSeparator();
        
        menuItem = new JMenuItem("Set rng seed...");
        menuItem.setMnemonic(KeyEvent.VK_R);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
            	int ans=123;
            	String s = (String)JOptionPane.showInputDialog(
            	                    mainFrame, null, "Insert a non-negative integer as RNG seed",
            	                    JOptionPane.PLAIN_MESSAGE, null,
            	                    null, ""+ans);
            	boolean valid=true;
            	try {
                        if(s!=null)
                            ans = Integer.valueOf(s).intValue();
            	} catch(NumberFormatException nfe) {
            		valid = false;
            	}
            	valid &= (ans >= 0);
            	if(!valid) {
            		JOptionPane.showMessageDialog(null, 
            				"Invalid seed: should be a non negative integer", 
            				"Error", JOptionPane.ERROR_MESSAGE);
            		return;
            	}
                ((LuaModel) model).setRngSeed(ans);
            }
        });
        
        return menu;
    }
    
    protected JMenu createColorsMenu() {
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu("Colors");
        menu.setMnemonic(KeyEvent.VK_C);
        
        menuItem = new JMenuItem("Chart Background...");
        menuItem.setMnemonic(KeyEvent.VK_C);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Color c = JColorChooser.showDialog(
                AbstractPlotComponent.this, "Chart Background Color",
                Color.gray);
                if (c != null) {
                    manager.setChartBackgroundColor(c);
                }
            }
        });
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Plot Background...");
        menuItem.setMnemonic(KeyEvent.VK_P);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                Color c = JColorChooser.showDialog(
                AbstractPlotComponent.this, "Plot background color",
                Color.white);
                if (c != null) {
                    manager.setPlotBackgroundColor(c);
                }
            }
        });
        menu.add(menuItem);
        
        return menu;
    }
    
       /*
        * Defaults menu
        */
    protected JMenu createDefaultsMenu() {
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu("Insert defaults");
        menu.setMnemonic(KeyEvent.VK_D);
        
        if (defaultsSection == null) {
            menu.setEnabled(false);
            return menu;
        }
        
        int index = 1;
        while (true) {
            String description, name;
            
            description = Lua.checkDefaults(
            model, defaultsSection, index, ModelDefaults.DESCRIPTION_KEY);
            
            if (description == null) break;
            
            name = Lua.checkDefaults(
            model, defaultsSection, index, ModelDefaults.NAME_KEY);
            if (name.equals("")) {
                menuItem = new JMenuItem("default " + index);
            }
            else {
                menuItem = new JMenuItem(name);
            }
            
            //             TODO currently tooltips don't like \n.
            //             create tooltips with \n
            //               String toolTipText = "";
            //               VariableItems items = Lua.loadDefaults(model, defaultsSection, index);
            //               VariableItems.Iterator i;
            //
            //               i = items.iterator();
            //               while (i.hasNext()) {
            //                   System.out.println(i.nextLabel() + ": " + i.value());
            //                   toolTipText = toolTipText + i.label() + ": " + i.value()  +" \n";
            //               }
            //               System.out.println("tooltip:\n" + toolTipText);
            //               if (!toolTipText.equals("")) {
            //                   menuItem.setToolTipText(toolTipText);
            //               }
            
            if (!description.equals("")) {
                menuItem.setToolTipText(description);
            }
            
            menuItem.setActionCommand(Integer.toString(index));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    String c = ((AbstractButton) e.getSource()).getActionCommand();
                    fillDefaults(Integer.parseInt(c));
                }
            });
            
            menu.add(menuItem);
            index++;
        }
        
        if (index == 1) {
            menu.setEnabled(false);
        }
        else {
            stateMachine.addSensibleItem(menu);
        }
        
        return menu;
    }
    
    
    /*
     * Helpers
     */
    protected void resizeChart(Dimension dim) {
        chart.setPreferredSize(dim);
        chart.setMinimumSize(dim);
        chart.setMaximumSize(dim);
    }
    
    //TODO all the layout thing is completely broken...
    public void fitToWindow() {
        resizeChart(chartScrollPane.getViewport().getExtentSize().getSize());
        chart.revalidate();
    }
    
    /*
     * interfaces implementors
     */
    public void showInvalidDataDialog(String message) {
        // TODO parent should not be null
        // and I should delegate this to someone else
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void showRuntimeErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Runtime error", JOptionPane.ERROR_MESSAGE);
    }

    
    public AbstractManager getManager() {
        return manager;
    }
    
    // stub, override in childern
    public int getDelayValue() {
        return 0;
    }
    
    
    public void sendCoreStatus(final CoreStatusEvent event) {
        /* let the manager intercept the START and FINISHED types */
        
        if ((event.getType() & CoreStatusEvent.STARTED) != 0) {
            progressBar.setVisible(true);
        }
        
        if ((event.getType() & CoreStatusEvent.FINISHED) != 0) {
            progressBar.setVisible(false);
        }
        
        if ((event.getType() & CoreStatusEvent.STRING) != 0) {
            progressString(event.getStatusString());
        }
        
        if ((event.getType() & CoreStatusEvent.PERCENT) != 0) {
            progressPercent(event.getPercent());
        }
        
        if ((event.getType() & CoreStatusEvent.COUNT) != 0) {
            progressCount(event.getCount());
        }
    }
    
    //? 30.7.2004 internal call to simulate the action of Clear button
    public void callClearAction(ComponentStateMachine stateMachine){
        stateMachine.parseInput(UserActionInput.clear);
    }
    
    
    /**
     * Notifies the starting (true) or ending (false) of the rendering thread
     */
    public void jobNotify(final boolean status) {
        progressBar.setVisible(status);
        if (status) {
            stateMachine.parseInput(ManagerInput.start);
        }
        else {
            stateMachine.parseInput(ManagerInput.end);
        }
    }
    
    public void errorNotify(String error) {
        stateMachine.parseInput(new ManagerError(error));
    }
    
    
    //to remove later
    protected class SamplesMenuListener implements MenuListener{
        
        public void menuCanceled(MenuEvent e){
            
        }
        
        public void menuSelected(MenuEvent e){
            
        }
        
        public void menuDeselected(MenuEvent e){
            
        }
        
    }
    
    
    
    protected class SamplesMenuItemListener implements ActionListener{
        
        public void actionPerformed(ActionEvent e) {
            DmcChartPanel chartPanel = (DmcChartPanel) manager.getChartPanel();
            
            chartPanel.setCrosshairNotBlocked(false);
            
            if (e.getActionCommand().equals("Add sample")){
                String sampleNameWithSpaces=JOptionPane.showInputDialog("Choose sample name:",controlForm.formDefaultName());
                if (sampleNameWithSpaces!="" && sampleNameWithSpaces!=null){
                    String sampleName=SampleParser.toWord(sampleNameWithSpaces);
                    controlForm.addFormDataToSamples(sampleName);
                }
            }
            else{
                if (e.getActionCommand().equals("Remove sample")){
                    SamplesSupplier samplesSupplier=mainFrame.getSamplesSupplier();
                    String [] samples= samplesSupplier.supplyMenu();
                    String sampleToRemove=(String) JOptionPane.showInputDialog(chartPanel,
                    "Choose sample to remove:",null,JOptionPane.PLAIN_MESSAGE,null, samples, samples[samples.length-1]);
                    if (sampleToRemove!=null){
                        samplesSupplier.removeSample(SampleParser.toWord(sampleToRemove),controlForm.getFormType());
                        updateSamplesMenu();
                    }
                }
                else{
                    String sampleNameWithSpaces=e.getActionCommand();
                    String sampleName=SampleParser.toWord(sampleNameWithSpaces);
                    controlForm.loadFormData(sampleName, controlForm.getFormType());
                }
            }
            chartPanel.setDisableCrosshairTillLeavesDisplay(true);
            chartPanel.setCrosshairNotBlocked(true);
        }
    }
    /*
     * Actions
     */
    protected class StartAction extends AbstractAction {
        public StartAction() {
            super();
            
            putValue(NAME, "Start");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(SHORT_DESCRIPTION, "reset ranges & start");
        }
        
        public void actionPerformed(final ActionEvent e) {
            callUponStart();
            stateMachine.parseInput(UserActionInput.start);
        }
    }
    
    protected class StopAction extends AbstractAction {
        public StopAction() {
            super();
            putValue(NAME, "Stop");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
            putValue(SHORT_DESCRIPTION, "stop any calculation");
        }
        
        public void actionPerformed(final ActionEvent e) {
            manager.stopRendering();
        }
    }
    
    protected class ContinueAction extends DmcAction {
        public ContinueAction() {
            super();
            putValue(NAME, "Continue");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
            putValue(SHORT_DESCRIPTION, "continue adding iterations");
            //putValue(ACTION_COMMAND_KEY, UserActionInput.continua.toString());
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserActionInput.continua);
        }
    }
    
    protected class RedrawAction extends DmcAction {
        public RedrawAction() {
            super();
            putValue(NAME, "Redraw");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
            putValue(SHORT_DESCRIPTION, "redraw with current ranges");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserActionInput.redraw);
        }
    }
    
    protected class ClearAction extends AbstractAction {
        public ClearAction() {
            super();
            putValue(NAME, "Clear");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(SHORT_DESCRIPTION, "clear");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserActionInput.clear);
        }
    }
    
    
    protected class ResetAction extends DmcAction {
        public ResetAction() {
            super();
            putValue(NAME, "Reset");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
            putValue(SHORT_DESCRIPTION, "quit this session and release the start button or menu");
        }
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserActionInput.reset);
        }
    }
    
    protected abstract class TransparencyAction
    extends AbstractAction {
        
        protected TransparencyAction() {
            super();
            putValue(NAME, "Transparency");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
            putValue(SHORT_DESCRIPTION, "use a transparent ink");
        }
    }
    
    protected abstract class TransparencyFactorAction
    extends AbstractAction
    implements ItemListener {
        
        protected TransparencyFactorAction() {
            super();
            putValue(NAME, "Transparency factor...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
            putValue(SHORT_DESCRIPTION, "set the paint transparency factor");
        }
        
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEnabled(true);
                return;
            }
            else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setEnabled(false);
                return;
            }
            // Modification: line below commented
            //   assert (false);
        }
        
        protected Float showDialog(
        final Component component, final float defaultValue) {
            
            String result;
            float value;
            
            result = JOptionPane.showInputDialog(
            component,
            "Transparency (between 0 and 1):",
            Float.toString(defaultValue));
            
            if (result != null) {
                value = Float.parseFloat(result);
                if (value >= 0 && value <= 1) {
                    return new Float(value);
                }
            }
            
            return null;
        }
    }
    
    
    /**
     * @return
     */
    public JMenu getOptionsMenu() {
        return optionsMenu;
    }
    
    
    /**
     * @return
     */
    public JMenu getCommandMenu() {
        return commandMenu;
    }
    
    public JMenu getPlotMenu() {
        return plotMenu;
    }
    
    public JMenu getSamplesMenu(){
        return samplesMenu;
    }
    
    
    public JToolBar getToolBar() {
        return toolBar;
    }
    
    
    public JToolBar getStatusBar() {
        return statusBar;
    }
    
    public JMenuItem getGridLinesMenuItem() {
        return gridLinesMenuItem;
    }
    
    public TransparencyAction getTransparencyAction() {
        return transparencyAction;
    }
    
    
    public void set1Visible(final boolean visible) {
        super.setVisible(visible);
        
        System.out.println("AbstractPlotComponent: " + this);
        System.out.println("                     : " + visible);
        
        if (visible == false) {
            getToolBar().setVisible(visible);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.println("PlotComponent later");
                    getToolBar().setVisible(visible);
                }
            });
        }
        
        //getToolBar().invalidate();
        getOptionsMenu().setVisible(visible);
        getCommandMenu().setVisible(visible);
        getPlotMenu().setVisible(visible);
        getStatusBar().setVisible(visible);
    }
    
    /**
     * @return
     */
    public JPanel getPanel() {
        return panel;
    }
    
    protected JMenuItem getConnectDotsMenuItem() {
        return connectDotsMenuItem;
    }
    
    protected JMenuItem getBigDotsMenuItem() {
        return bigDotsMenuItem;
    }
    
    public void saveImageAs() throws IOException {
        manager.saveImageAs();
    }

    /**
     * Saves current dataset to a user picked file
     * */    
    public void saveDataAs() throws IOException {
        if(dataobject==null) {
            JOptionPane.showMessageDialog(null, "No data associated to current chart", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            try {
                    dataobject.save(new File(filename));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Can't save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    protected void finalize() {
        System.out.println("finalizing: " + getClass());
    }
    
    //?
    public void writeToStatusBar(String s){
        statusLabel.setText(s);
        //repaint();
    }
    
    //?
    
    //?
    public abstract void callUponStart();
    
    public MainFrame getMainFrame(){
        return mainFrame;
    }
    //?

	/**
	 * @return the dataobject
	 */
	public DataObject getDataobject() {
		return dataobject;
	}

	/**
	 * @param dataobject the dataobject to set
	 */
	public void setDataobject(DataObject dataobject) {
		this.dataobject = dataobject;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}
        
        public AbstractAction getSaveDataAction() {
            return mainFrame.getSaveDataAction();
        }

	public AbstractControlForm getControlForm() {
		return controlForm;
	}

}
