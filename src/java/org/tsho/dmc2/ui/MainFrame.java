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
 * later version.*
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.tsho.dmc2.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;


import org.jfree.ui.RefineryUtilities;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.dmc2.core.model.ODE;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.sm.Input;


public class MainFrame extends JFrame {
    
    private static final String DEFAULT_TITLE = "iDmc";
    private static final String INFO_TAB_TITLE = "Model";
    private static final String CLOSE_ALL_MODELS_MESS =
    "All current model open and/or running plots \n"
    + "will be stopped and closed.";
    private static final String EXIT_MESS = "Exit iDmc?";
    
    private static int frameCount = 0;
    
    private final MainFrameSM stateMachine;
    
    private final Action openModelAction = new OpenModelAction();
    private final Action closeModelAction = new CloseModelAction();
    private final SaveImageAction saveAsAction = new SaveImageAction();
    private final SaveDataAction saveDataAction = new SaveDataAction();
    
    private final TrajectoryAction trajectoryAction = new TrajectoryAction();
    private final CyclesAction cyclesAction = new CyclesAction();
    private final CowebAction cowebAction = new CowebAction();
    private final BifurAction bifurAction = new BifurAction();
    private final BasinAction basinACtion = new BasinAction();
    private final LyapunovAction lyapunovAction = new LyapunovAction();
    private final ManifoldsAction manifoldsAction = new ManifoldsAction();
    private final AbsorbingAreaAction absorbingAreaAction =new AbsorbingAreaAction();
    
    private final ClosePlotTabAction closePlotTabAction = new ClosePlotTabAction();
    
    private HashMap plotComponents;
    
    private JTabbedPane tabbedPane;
    
    private JLabel openingPage;
    
    private static JFileChooser fileChooser;
    private JToolBar toolBar;
    private InfoPanel infoPanel;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu aboutMenu;
    private JMenu fakeCommandMenu;
    private JMenu fakePlotMenu;
    private JMenu fakeOptionsMenu;
    
    
    private Model model;
    private File modelFile;
    private SamplesSupplier samplesSupplier;
    
    public MainFrame(final File file) {
        super();
        
        this.modelFile=file;
        if (file!=null) {
            samplesSupplier=new SamplesSupplier(file);
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent evt) {
                
                if (frameCount == 1) {
                    exit();
                    return;
                }
                else if (tabbedPane.getTabCount() > 1) {
                    int res = JOptionPane.showConfirmDialog(
                    MainFrame.this, CLOSE_ALL_MODELS_MESS,
                    "Information.", JOptionPane.OK_CANCEL_OPTION);
                    if (res == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                    
                    closeAllPlotTabs();
                }
                
                infoPanel.dispose();
                dispose();
                frameCount--;
            }
        });
        
        plotComponents = new HashMap();
        
        stateMachine = new MainFrameSM(this);
        
        menuBar = new JMenuBar();
        fileMenu = createFileMenu();
        aboutMenu = createAboutMenu();
        setJMenuBar(menuBar);
        fakeCommandMenu = new JMenu("Command");
        fakeCommandMenu.setEnabled(false);
        fakePlotMenu = new JMenu("Plot");
        fakePlotMenu.setEnabled(false);
        fakeOptionsMenu = new JMenu("Options");
        fakeOptionsMenu.setEnabled(false);
        
        fileChooser = new JFileChooser(
        System.getProperty("user.dir") + "/models");
        
        updateMenubar(null);
        createToolBar();
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        ImageIcon chaosImage=new ImageIcon("logo_new.jpg");
        openingPage=new JLabel(chaosImage,SwingConstants.CENTER);
        
        stateMachine.parseInput(Input.go);
        
        if (file != null) {
            Model model = createModelFromFile(file);
            setTitle(DEFAULT_TITLE + ": " + model.getName());
            infoPanel = new InfoPanel(model);
            getContentPane().add(tabbedPane);
        }
        else {
            setTitle(DEFAULT_TITLE + ": no model opened");
            infoPanel = new InfoPanel(null);
            getContentPane().add(openingPage);
        }
        
        //order matters here because of what TabChangeListener class below does
        tabbedPane.add(infoPanel, INFO_TAB_TITLE, 0);
        tabbedPane.addChangeListener(new TabChangeListener());
        
        //?
        
        //?
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();        
        setSize(screenSize.width  - inset * 4,
        screenSize.height - inset * 3);
        
        frameCount++;
        this.setVisible(true);
        
    }
    
    /*
     * tabbed pane change listener
     */
    class TabChangeListener implements ChangeListener {
        
        public void stateChanged(final ChangeEvent e) {
            
            Container contentPane = getContentPane();
            
            if (tabbedPane.getSelectedIndex() == -1) {
                return;
            }
            if (tabbedPane.getSelectedIndex() == 0) {
                contentPane.removeAll();//? removing the toolbar?
                
                contentPane.add(tabbedPane, BorderLayout.CENTER);
                //contentPane.add(stat, BorderLayout.SOUTH);
                contentPane.add(toolBar, BorderLayout.NORTH);
                
                updateMenubar(null);
                
                stateMachine.parseInput(InternalInput.infoTab);
            }
            else {
                final PlotComponent comp =
                ((PlotComponent) plotComponents.get(
                tabbedPane.getSelectedComponent()));
                
                contentPane.removeAll();
                
                contentPane.add(tabbedPane, BorderLayout.CENTER);
                contentPane.add(comp.getToolBar(), BorderLayout.NORTH);
                contentPane.add(comp.getStatusBar(), BorderLayout.SOUTH);
                
                updateMenubar(comp);
                
                stateMachine.parseInput(InternalInput.plotTab);
            }
            
            repaint();
        }
    }
    
    /*
     * Toolbar
     */
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        toolBar.add(new JButton(openModelAction));
        
        toolBar.add(new JButton(closeModelAction));
        toolBar.addSeparator();
    }
    
    
    /*
     * Menubar and menus
     */
    public void updateMenubar(final PlotComponent comp) {
        
        menuBar.removeAll();
        
        menuBar.add(fileMenu);
        
        if (comp == null) {
            saveAsAction.setEnabled(false);
            saveDataAction.setEnabled(false);
            menuBar.add(fakeCommandMenu);
            menuBar.add(fakePlotMenu);
            menuBar.add(fakeOptionsMenu);
        }
        else {
            saveAsAction.setEnabled(true);
            saveAsAction.setPlotComponent(comp);
            saveDataAction.setEnabled(comp.getDataobject()!=null);
            saveDataAction.setPlotComponent(comp);
            menuBar.add(comp.getCommandMenu());
            menuBar.add(comp.getPlotMenu());
            menuBar.add(comp.getOptionsMenu());
            menuBar.add(comp.getSamplesMenu());
        }
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(aboutMenu);
    }
    
    
    private JMenu createFileMenu() {
        JMenuItem menuItem;
        
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        
        menu.add(new JMenuItem(openModelAction));
        menu.add(new JMenuItem(closeModelAction));
        
        menu.addSeparator();
        
        menu.add(createPlotTypeMenu());
        menu.add(new JMenuItem(closePlotTabAction));
        
        menu.addSeparator();
        
        menuItem = new JMenuItem(saveAsAction);
        menuItem.setEnabled(false);
        menu.add(menuItem);
        menuItem = new JMenuItem(saveDataAction);
        menuItem.setEnabled(false);
        menu.add(menuItem);        
        
        menu.addSeparator();
        
        /* Exit menuitem */
        menuItem = new JMenuItem("Quit");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                WindowEvent we
                = new WindowEvent(MainFrame.this,
                WindowEvent.WINDOW_CLOSING);
                
                processWindowEvent(we);
            }
        });
        menuItem.setToolTipText("Close the current window");
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic(KeyEvent.VK_X);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                exit();
            }
        });
        menuItem.setToolTipText("Quit the program");
        menu.add(menuItem);
        
        return menu;
    }
    
    /**
     *
     * @return
     */
    private JMenu createPlotTypeMenu() {
        JMenu plotMenu;
        JMenuItem menuItem;
        
        plotMenu = new JMenu("New plot");
        plotMenu.setMnemonic(KeyEvent.VK_P);
        
        menuItem = new JMenuItem(trajectoryAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(cowebAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(bifurAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(cyclesAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(basinACtion);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(lyapunovAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(manifoldsAction);
        plotMenu.add(menuItem);
        
        menuItem = new JMenuItem(absorbingAreaAction);
        plotMenu.add(menuItem);
        
        return plotMenu;
    }
    
    private JMenu createAboutMenu() {
        JMenu menu = new JMenu("About");
        
        JMenuItem menuItem = new JMenuItem("About");
        menuItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                about();
            }
        });
        menu.add(menuItem);
        return menu;
    }
    /*
     * Misc & helpers
     */
    
    private Model createModelFromFile(final File file) {
        Model model = null;
        if (file != null) {
            
            try {
                model = Lua.newModel(file);
            }
            catch (FileNotFoundException e) {
                showErrorDialog(
                "File \"" + file.getPath() + "\" not found");
                return null;
                
            }
            catch (IOException e) {
                showErrorDialog(
                "Problem opening file \"" + file.getPath() + "\"");
                return null;
            }
            catch (ModelException e) {
                showErrorDialog(e.getMessage());
                return null;
            }
            this.model=model;
        }
        
        if (model instanceof ODE) {
            stateMachine.parseInput(InternalInput.contiModel);
        }
        else if (model instanceof SimpleMap) {
            if (model.getNVar() == 2 && model instanceof DifferentiableMap) {
                stateMachine.parseInput(InternalInput.discr2DDiffModel);
            }
            else if (model.getNVar() == 1) {
                stateMachine.parseInput(InternalInput.discr1DModel);
            }
            else {
                stateMachine.parseInput(InternalInput.discrModel);
            }
        }
        else {
            throw new Error("Ivalid model type.");
        }
        
        return model;
    }
    
    private void showErrorDialog(final String message) {
        JOptionPane.showMessageDialog(
        this, message, "Error.", JOptionPane.ERROR_MESSAGE);
    }
    
    /*
     * SM handlers
     */
    boolean newModel() {
        
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = fileChooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        
        File tmpFile = fileChooser.getSelectedFile();
        
        new MainFrame(tmpFile);
        
        return true;
    }
    
    
    boolean openModel() {
        
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = fileChooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        
        try{
            File tmpFile = fileChooser.getSelectedFile();
            Model model = null;
            model = createModelFromFile(tmpFile);
            //?
            if (model!=null) {
                modelFile=tmpFile;
                samplesSupplier=new SamplesSupplier(modelFile);
            }
            //?
            setTitle(DEFAULT_TITLE + ": " + model.getName());
            tabbedPane.remove(0);
            plotComponents.remove(infoPanel);
            infoPanel.dispose();
            infoPanel = new InfoPanel(model);
            tabbedPane.add(infoPanel, INFO_TAB_TITLE, 0);
            return true;
        }
        catch(Exception e){ //quick & dirty way to correct a minor bug
            JOptionPane.showMessageDialog(this, 
                    e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
    }
    
    // TODO fix
    Model duplicateModel() {
        try {
            return Lua.newModel(modelFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    /*
     * if askUser == true ask the user before closing
     * returns true if the model was closed
     */
    boolean closeModel() {
        
        if (tabbedPane.getTabCount() > 1) {
            int res = JOptionPane.showConfirmDialog(
            this, CLOSE_ALL_MODELS_MESS,
            "Information.", JOptionPane.OK_CANCEL_OPTION);
            
            if (res == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            
            closeAllPlotTabs();
        }
        
        this.modelFile = null;
        this.model=null;
        
        tabbedPane.remove(0);
        infoPanel.dispose();
        infoPanel = new InfoPanel(null);
        tabbedPane.add(infoPanel, INFO_TAB_TITLE, 0);
        
        return true;
    }
    
    void newPlotComponent(final AbstractPlotComponent comp) {
        plotComponents.put(comp.getPanel(), comp);
        tabbedPane.add(comp.getPanel());
        tabbedPane.setSelectedComponent(comp.getPanel());
    }
    
    void closeCurrentTab() {        
        Component panel = tabbedPane.getSelectedComponent();
        AbstractPlotComponent comp;
        
        comp = (AbstractPlotComponent) plotComponents.get(panel);
        
        // JTabbedPane gets a change event iff the _index_ changes
        int selectedIndex = tabbedPane.getSelectedIndex();
        tabbedPane.setSelectedIndex(-1);
        tabbedPane.remove(comp);
        if (tabbedPane.getTabCount() <= selectedIndex) {
            selectedIndex--;
        }
        tabbedPane.setSelectedIndex(selectedIndex);
        
        plotComponents.remove(panel);
        
        comp.dispose();
        
        if (tabbedPane.getTabCount() == 1) {
            stateMachine.parseInput(InternalInput.noMorePlotTabs);
        }
    }
    
    // leaves tabbedPane index at -1
    void closeAllPlotTabs() {
        
        AbstractPlotComponent comp;
        
        tabbedPane.setSelectedIndex(-1);
        
        Iterator iter = plotComponents.values().iterator();
        while (iter.hasNext()) {
            comp = (AbstractPlotComponent) iter.next();
            
            tabbedPane.remove(comp);
            comp.dispose();
            iter.remove();
        }
        
        // TODO this is initialized in 2 places
        plotComponents = new HashMap();
    }
    
    private void exit() {
        int res = JOptionPane.showConfirmDialog(
        MainFrame.this, EXIT_MESS,
        "Information.", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.CANCEL_OPTION) {
            return;
        }
        
        System.exit(0);
    }
    
    /*
     * Actions
     */
    protected class OpenModelAction extends AbstractAction {
        public OpenModelAction() {
            super();
            
            putValue(NAME, "Open model...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(SHORT_DESCRIPTION, "Open a new model");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserMenuInput.openModel);
        }
    }
    
    protected class CloseModelAction extends AbstractAction {
        public CloseModelAction() {
            super();
            
            putValue(NAME, "Close model");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(SHORT_DESCRIPTION, "Close the current model");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserMenuInput.closeModel);
        }
    }
    
    protected class SaveImageAction extends AbstractAction {
        PlotComponent plotComponent;
        
        public SaveImageAction() {
            super();
            
            putValue(NAME, "Save Image As...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
            putValue(SHORT_DESCRIPTION, "Save current image to a file");
        }
        
        public void actionPerformed(final ActionEvent e) {
            try {
                plotComponent.saveImageAs();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        public PlotComponent getPlotComponent() {
            return plotComponent;
        }
        
        public void setPlotComponent(PlotComponent component) {
            plotComponent = component;
        }
        
    }
    
    public class SaveDataAction extends AbstractAction {
        PlotComponent plotComponent;
        
        public SaveDataAction() {
            super();
            
            putValue(NAME, "Save Data As...");
            putValue(SHORT_DESCRIPTION, "Save current data to a file");
        }
        
        public void actionPerformed(final ActionEvent e) {
            try {
                plotComponent.saveDataAs();
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        public PlotComponent getPlotComponent() {
            return plotComponent;
        }
        
        public void setPlotComponent(PlotComponent component) {
            plotComponent = component;
        }
        
    }
    
    protected class TrajectoryAction extends AbstractAction {
        public TrajectoryAction() {
            super();
            
            putValue(NAME, "Trajectory");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
            putValue(SHORT_DESCRIPTION, "Visualize the trajectory of a differential equation or of a map");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.discTraj);
        }
    }
    
    protected class CyclesAction extends AbstractAction {
        public CyclesAction() {
            super();
            
            putValue(NAME, "Cycles");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
            //putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.cycles);
        }
    }
    
    protected class CowebAction extends AbstractAction {
        public CowebAction() {
            super();
            
            putValue(NAME, "Shifted & cobweb");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            //putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.coweb);
        }
    }
    
    protected class BifurAction extends AbstractAction {
        public BifurAction() {
            super();
            
            putValue(NAME, "Bifurcation");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
            putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.bifurcat);
        }
    }
    
    protected class BasinAction extends AbstractAction {
        public BasinAction() {
            super();
            
            putValue(NAME, "Basin of attraction");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
            //putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.basin);
        }
    }
    
    protected class AbsorbingAreaAction extends AbstractAction {
        public AbsorbingAreaAction() {
            super();
            
            putValue(NAME, "Absorbing area");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.absorbingArea);
        }
    }
    
    protected class LyapunovAction extends AbstractAction {
        public LyapunovAction() {
            super();
            
            putValue(NAME, "Lyapunov exponents");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
            //putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.lyapunov);
        }
    }
    
    protected class ManifoldsAction extends AbstractAction {
        public ManifoldsAction() {
            super();
            
            putValue(NAME, "Manifolds");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
            //putValue(SHORT_DESCRIPTION, "bifurcation plot");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(NewPlotInput.manifolds);
        }
    }
    
    
    protected class ClosePlotTabAction extends AbstractAction {
        public ClosePlotTabAction() {
            super();
            
            putValue(NAME, "Close plot");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
            putValue(SHORT_DESCRIPTION, "close the current plot tab");
        }
        
        public void actionPerformed(final ActionEvent e) {
            stateMachine.parseInput(UserMenuInput.closeTab);
        }
    }
    
    //? this method switches between the opening page and tabbed pane
    public void setDisplay(int i){
        Container contentPane=getContentPane();
        contentPane.removeAll();
        
        if (i==1)
            contentPane.add(openingPage);
        else
            contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(toolBar, BorderLayout.NORTH);
        repaint();
    }
    
    //?
    
    
    /**
     * Action getters
     */
    Action getOpenModelAction() {
        return openModelAction;
    }
    Action getCloseModelAction() {
        return closeModelAction;
    }
    Action getClosePlotTabAction() {
        return closePlotTabAction;
    }
    Action getTrajectoryAction() {
        return trajectoryAction;
    }
    Action getCowebAction() {
        return cowebAction;
    }
    Action getCyclesACtion() {
        return cyclesAction;
    }
    Action getBasinACtion() {
        return basinACtion;
    }
    Action getBifurAction() {
        return bifurAction;
    }
    Action getLyapunovAction() {
        return lyapunovAction;
    }
    Action getManifoldsAction() {
        return manifoldsAction;
    }
    Action getAbsorbingAreaAction(){
        return absorbingAreaAction;
    }
    
    DmcAboutFrame aboutFrame;
    private void about() {
        
        if (this.aboutFrame == null) {
            this.aboutFrame = new DmcAboutFrame();
            this.aboutFrame.pack();
            RefineryUtilities.centerFrameOnScreen(this.aboutFrame);
        }
        this.aboutFrame.setVisible(true);
        this.aboutFrame.requestFocus();
    }
    
    public File getModelFile(){
        return modelFile;
    }
    
    public Model getModel(){
        return model;
    }
    
    public SamplesSupplier getSamplesSupplier(){
        return samplesSupplier;
    }

	public SaveDataAction getSaveDataAction() {
		return saveDataAction;
	}

	public SaveImageAction getSaveAsAction() {
		return saveAsAction;
	}


	/**
	 * @return the plotComponents
	 */
	public HashMap getPlotComponents() {
		return plotComponents;
	}


	/**
	 * @return the tabbedPane
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}
	
	/**
	 * @return the currently selected plot component
	 * */
	public AbstractPlotComponent getSelectedPlotComponent() {
		return (AbstractPlotComponent) getPlotComponents().get(
      		  getTabbedPane().getSelectedComponent());
	}
}

