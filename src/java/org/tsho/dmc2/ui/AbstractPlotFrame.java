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

import java.awt.BorderLayout;
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
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.tsho.dmc2.ModelDefaults;
import org.tsho.dmc2.core.CoreStatusEvent;
import org.tsho.dmc2.core.CoreStatusListener;
import org.tsho.dmc2.core.dlua.Lua;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.managers.AbstractManager;
import org.tsho.dmc2.managers.ManagerListener;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.ui.components.DmcAction;


public abstract class AbstractPlotFrame extends JFrame
                                        implements CoreStatusListener,
                                                   ManagerListener {

    protected JProgressBar progressBar;
    protected JLabel statusLabel;
    private String progressString;
    private long progressCount;

    protected JComponent chart;
    protected JScrollPane chartScrollPane;
    protected JSplitPane splitPane;

    protected Model model;

    protected String defaultsSection = null;

    private TransparencyAction transparencyAction;
//    private JMenuItem transprcyMenuItem = null;
//    private JMenuItem transprcyFactorMenuItem = null;
    protected boolean transparencyDefault = false;

    /* Plot menu items */
    private JMenu chartSizeSubMenu;
    private JMenuItem chartSizeMenuItem;
    private JMenuItem gridLinesMenuItem;
    private JMenuItem connectDotsMenuItem;
    private JMenuItem bigDotsMenuItem;
    private JMenuItem crossHairMenuITem;


    protected ComponentStateMachine stateMachine;
    private AbstractManager manager; 

    private AbstractPlotFrame() {
    }

    protected AbstractPlotFrame(final Model model, final String title) {

        this.model = model;
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
    }

    protected void init(
            final AbstractManager manager,
            final ComponentStateMachine machine,
            final JComponent controlForm) {

        this.manager  = manager;
        this.stateMachine = machine;

        createSplitPane(manager, controlForm);
        createMenubar();
        createToolbar();
        createStatusBar();

//        addInternalFrameListener(new InternalFrameAdapter() {
//            public void frameClosed(final InternalFrameEvent e) {
//                manager.stopRendering();
//            }
//        });

        pack();
        setSize(800, 600); // TODO set this as a property

        setVisible(true);
        
        // TODO fill window someway! 
        //resizeChart(chartScrollPane.getBounds().getSize());
        //resizeChart(chartScrollPane.getViewport().getExtentSize().getSize());
        //chartScrollPane.revalidate();
        //validate();
        //repaint();
    }

    /*
     * Abstract members
     */
    abstract protected void createMenubar();
    abstract protected void createToolbar();


    /*
     * CoreStatusListener
     */
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


    /*
     * Status bar
     */
    private void createStatusBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        toolBar.setLayout(gridbag);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("");
        progressBar.setMaximum(99);
        progressBar.setVisible(false);

        //statusLabel = new JTextField("");
        statusLabel = new JLabel("");
        //statusLabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 80;
        gridbag.setConstraints(statusLabel, c);
        toolBar.add(statusLabel);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 20;
        gridbag.setConstraints(progressBar, c);
        toolBar.add(progressBar);

        getContentPane().add(toolBar, BorderLayout.SOUTH);
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
     * Invalid data dialog
     */
    public void showInvalidDataDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid data.", JOptionPane.ERROR_MESSAGE);
    }

    protected void showInvalidDataDialog() {
        JOptionPane.showMessageDialog(this, "Invalid data insterted.", "Invalid data.", JOptionPane.ERROR_MESSAGE);
    }


    /*
     * create frame
     */
     protected void createSplitPane(AbstractManager manager, JComponent controlForm) {
         JComponent controlsScrollPane;
         Dimension dim = new Dimension(500, 500);

         
         
         chart = manager.getChartPanel();
         resizeChart(dim);

         chartScrollPane = createChartScrollPane(chart); 

         controlsScrollPane = new JScrollPane(
                         controlForm,
                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

         splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                    controlsScrollPane,
                                    chartScrollPane);

         splitPane.setOneTouchExpandable(true);
         splitPane.setDividerLocation((int)controlsScrollPane.getPreferredSize().getWidth() + 10);

         getContentPane().add(splitPane, BorderLayout.CENTER);

//         chartScrollPane.revalidate();

         repaint();
     }

     private JScrollPane createChartScrollPane(JComponent chart) {
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

     protected void resizeChart(Dimension dim) {
         chart.setPreferredSize(dim);
         chart.setMinimumSize(dim);
         chart.setMaximumSize(dim);
     }

    public void setFixedSize(boolean flag) {
        if (flag == true) {
            splitPane.remove(chart);
            // resizeChart(new Dimension (500, 500));
            chartScrollPane = createChartScrollPane(chart);
            splitPane.setRightComponent(chartScrollPane);
        }
        else {
            splitPane.remove(chartScrollPane);
            splitPane.setRightComponent(chart);
            resizeChart(new Dimension (-1,-1));
        }
    }


    /*
     * Common user Actions
     */
     
//    public abstract class PlotAction extends AbstractAction {
//        final protected static String VISIBILITY_KEY = "visible";
//         
//            public PlotAction() {
//                super();
//
//                putValue(VISIBILITY_KEY, new Boolean(true));
//            }
//
//        public void setVisible(final boolean visible) {
//
//            Boolean oldValue = (Boolean) this.getValue(VISIBILITY_KEY);
//
//            if (visible == oldValue.booleanValue()) {
//                return;
//            }
//
//            Boolean b = new Boolean(visible);
//            putValue(VISIBILITY_KEY, b);
//
//            firePropertyChange(VISIBILITY_KEY, oldValue, b);
//        }
//
//        public boolean isVisible() {
//            return ((Boolean) getValue(VISIBILITY_KEY)).booleanValue();
//        }
//     }
     
     
     protected class StartAction extends AbstractAction {
         public StartAction() {
             super();

             putValue(NAME, "Start");
             putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
             putValue(SHORT_DESCRIPTION, "reset ranges & start");
         }

        public void actionPerformed(final ActionEvent e) {
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
             //Modification: line below commented
             //assert (false);
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


     protected JMenuItem createCloseMenuItem() {
         JMenuItem menuItem;
         
         menuItem = new JMenuItem("Close");
         menuItem.setMnemonic(KeyEvent.VK_C);
         menuItem.addActionListener(new ActionListener()
         {
             public void actionPerformed(final ActionEvent e) {
                 stateMachine.parseInput(UserActionInput.close);
                 processWindowEvent(
                         new WindowEvent(
                                AbstractPlotFrame.this,
                                WindowEvent.WINDOW_CLOSING));
// this is fo internal frames                                
//                try {
//                    setClosed(true);
//                }
//                catch (PropertyVetoException e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }                                
                                
             }
         });

        return menuItem;
     }

     /*
      * Plot menu
      */
    protected JMenu createPlotMenu() {
        JMenu menu;
        JMenuItem menuItem;
        ButtonGroup group;

        menu = new JMenu("Plot");
        menu.setMnemonic(KeyEvent.VK_P);

        /*
         * Size
         */

        chartSizeSubMenu = new JMenu("Size");
//
//        menuItem = new JCheckBoxMenuItem("Fixed Size");
//        menuItem.setMnemonic(KeyEvent.VK_F);
//        menu.add(menuItem);
//        menuItem.setSelected(true);
//        menuItem.addActionListener(new ActionListener() {
//            public void actionPerformed(final ActionEvent e) {
//                boolean flag = ((AbstractButton) e.getSource()).isSelected(); 
//                setFixedSize(flag);
//                chartSizeSubMenu.setEnabled(flag);
//                chart.revalidate();
//            }
//        });
//        chartSizeMenuItem = menuItem;


        group = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("500x500");
        menuItem.setSelected(true);
        menuItem.setMnemonic(KeyEvent.VK_6);
        group.add(menuItem);
        chartSizeSubMenu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resizeChart(new Dimension(500, 500));
                chart.revalidate();
            }
        });

        menuItem = new JRadioButtonMenuItem("600x600");
        menuItem.setMnemonic(KeyEvent.VK_8);
        group.add(menuItem);
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
                //resizeChart(chartScrollPane.getBounds().getSize());
                resizeChart(chartScrollPane.getViewport().getExtentSize().getSize());
                chart.revalidate();
            }
        });
        
        
        
        menuItem = new JMenuItem("Custom size...");
        //menuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Dimension d = chart.getPreferredSize();
                WindowsSizePanel panel = new WindowsSizePanel(d.width, d.height); 
                int result = JOptionPane.showConfirmDialog(
                                null, panel, 
                                "Change plot dimensions",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    d.setSize(panel.getWidth(), panel.getHeight());
                    resizeChart(d);
                    chart.revalidate();
                }
            }
        });
        chartSizeSubMenu.add(menuItem);

        stateMachine.addSensibleItem(chartSizeSubMenu);
        menu.add(chartSizeSubMenu);

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
                    result = showDialog(AbstractPlotFrame.this, trManager.getAlphaValue());
                    if (result != null) {
                        trManager.setAlphaValue(result.floatValue());
                    }
                }
            };

            transparencyAction = new TransparencyAction() {
                public void actionPerformed(final ActionEvent e) {
                    boolean selected = ((AbstractButton)e.getSource()).isSelected();
                    trManager.setAlpha(selected);
                }
            };

            // follow the default
            trfAction.setEnabled(false);

            menuItem = new JCheckBoxMenuItem(transparencyAction);

            menuItem.addItemListener(trfAction);
            menuItem.setSelected(transparencyDefault);
            //transprcyMenuItem = menuItem;
            menu.add(menuItem);


            menuItem = new JMenuItem(trfAction);
//            menuItem.setToolTipText(trfAction.getTooltipText());
            menu.add(menuItem);
//            menuItem.setEnabled(transparencyAction.isSelected());
            //transprcyFactorMenuItem = menuItem;
        }

        // separator
        if (manager instanceof AbstractManager.GridLines ||
            manager instanceof AbstractManager.ConnectDots ||
            manager instanceof AbstractManager.BigDots ||
            manager instanceof AbstractManager.Crosshair) {
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
            crossHairMenuITem = menuItem;
        }

        return menu;
      }
      
//    protected void setTransparency(
//            AbstractManager.Transparency manager, boolean flag) {
//           
//          transparencyAction.setSelected(flag);
//          manager.setAlpha(flag);
//      }
      
    /*
     * Defaults menu
     */
    protected JMenu createDefaultsMenu() {
        JMenu menu;
        JMenuItem menuItem;

        if (defaultsSection == null) return null;

        menu = new JMenu("Insert defaults");
        menu.setMnemonic(KeyEvent.VK_D);

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

//          TODO currently tooltips don't like \n.
//          create tooltips with \n 
//            String toolTipText = "";
//            VariableItems items = Lua.loadDefaults(model, defaultsSection, index);
//            VariableItems.Iterator i;
//
//            i = items.iterator();
//            while (i.hasNext()) {
//                System.out.println(i.nextLabel() + ": " + i.value());
//                toolTipText = toolTipText + i.label() + ": " + i.value()  +" \n";
//            }
//            System.out.println("tooltip:\n" + toolTipText);
//            if (!toolTipText.equals("")) {
//                menuItem.setToolTipText(toolTipText);
//            }

            if (!description.equals("")) {
                menuItem.setToolTipText(description);
            }

            menuItem.setActionCommand(Integer.toString(index));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String c = ((AbstractButton)e.getSource()).getActionCommand(); 
                    fillDefaults(Integer.parseInt(c));
                }
            });

            menu.add(menuItem);
            index++;
        }

        if (index == 1) return null;
        return menu;
    }

    protected abstract void fillDefaults(int index);

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


    /**
     * @return Returns the model.
     */
    public Model getModel() {
        return model;
    }
    /**
     * @return Returns the chartSizeMenuItem.
     */
    public JMenuItem getChartSizeMenuItem() {
        return chartSizeMenuItem;
    }
    /**
     * @return Returns the chartSizeSubMenu.
     */
    public JMenu getChartSizeSubMenu() {
        return chartSizeSubMenu;
    }
    /**
     * @return Returns the connectDotsMenuItem.
     */
    public JMenuItem getConnectDotsMenuItem() {
        return connectDotsMenuItem;
    }
    /**
     * @return Returns the gridLinesMenuItem.
     */
    public JMenuItem getGridLinesMenuItem() {
        return gridLinesMenuItem;
    }
    /**
     * @return
     */
//    protected JMenuItem getTransprcyMenuItem() {
//        return transprcyMenuItem;
//    }

    /**
     * @return
     */
    public JMenuItem getBigDotsMenuItem() {
        return bigDotsMenuItem;
    }
//
//    /**
//     * @return
//     */
//    public JMenuItem getTransprcyFactorMenuItem() {
//        return transprcyFactorMenuItem;
//    }

    /**
     * @return
     */
    public TransparencyAction getTransparencyAction() {
        return transparencyAction;
    }

    /**
     * @return
     */
    public AbstractManager getManager() {
        return manager;
    }

}