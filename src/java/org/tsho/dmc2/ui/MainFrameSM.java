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

import javax.swing.Action;

import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.SimpleMap;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.ui.MainFrame.SaveDataAction;
import org.tsho.dmc2.ui.absorbingArea.AbsorbingAreaComponent;
import org.tsho.dmc2.ui.basin.BasinComponent;
import org.tsho.dmc2.ui.basinslice.BasinSliceComponent;
import org.tsho.dmc2.ui.bifurcation.BifurcationComponent;
import org.tsho.dmc2.ui.coweb.CowebComponent;
import org.tsho.dmc2.ui.cycles.CyclesComponent;
import org.tsho.dmc2.ui.lyapunov.LyapunovComponent;
import org.tsho.dmc2.ui.manifolds.ManifoldsComponent;
import org.tsho.dmc2.ui.trajectory.TrajectoryComponent;


final class MainFrameSM extends ComponentStateMachine {

    /*
     * States
     */
    private static final State initialS = new State("initialS"); // initial state
    private static final State noModelS = new State("noModelS");
    private static final State noPlotS = new State("noPlotS"); // only info tab
    private static final State infoTabS = new State("infoTabS"); // info tab selected
    private static final State plotTabS = new State("plotTabS"); // plot tab selected
    private static final State finalS = new State("finalS"); // final state


    private MainFrame frame;

    MainFrameSM(final MainFrame frame) {
        super("MainFrameSM", initialS);

        this.frame = frame;
        setUp(table);
    }

    private final Condition closeModel = new Condition() {
        public boolean condition(final Input i) {
            return frame.closeModel();
        }
    };

    private final Condition openModel = new Condition() {
        public boolean condition(final Input i) {
            return frame.openModel();
        }
    };

    private final Condition newModel = new Condition() {
        public boolean condition(final Input i) {
            return frame.newModel();
        }
    };

    /*
     * Transitions
     */
    private final Transition confNoModel = new Transition() {
        public void transition(final Input i) {

            frame.getOpenModelAction().putValue(Action.NAME, "Open model...");
            frame.getCloseModelAction().setEnabled(false);
            
            frame.getClosePlotTabAction().setEnabled(false);

            frame.getCowebAction().setEnabled(false);
            frame.getTrajectoryAction().setEnabled(false);
            frame.getCyclesACtion().setEnabled(false);
            frame.getBifurAction().setEnabled(false);
            frame.getBasinACtion().setEnabled(false);
            frame.getBasinSliceAction().setEnabled(false);
            frame.getLyapunovAction().setEnabled(false);
            frame.getManifoldsAction().setEnabled(false);
            frame.getAbsorbingAreaAction().setEnabled(false);
        }
    };

    private final Transition confNoPlot = new Transition() {
        public void transition(final Input i) {

            frame.getOpenModelAction().putValue(Action.NAME, "New model...");
            frame.getCloseModelAction().setEnabled(true);
            frame.getSaveDataAction().setEnabled(false);

            frame.getClosePlotTabAction().setEnabled(false);

            boolean discreteActions = false;
            if (i == InternalInput.contiModel) {
                discreteActions = false;
                frame.getBasinACtion().setEnabled(false);
            }
            else if (i == InternalInput.discrModel) {
                frame.getManifoldsAction().setEnabled(false);
                if (frame.getModel().getNVar()==2) {
                    frame.getBasinACtion().setEnabled(true);
                    frame.getBasinSliceAction().setEnabled(true);
                } else if (frame.getModel().getNVar()>2) {
                    frame.getBasinSliceAction().setEnabled(true);
                    frame.getBasinACtion().setEnabled(false);
                } else {
                    frame.getBasinSliceAction().setEnabled(false);
                    frame.getBasinACtion().setEnabled(false);
                }
                discreteActions = true;
            }
            else if (i == InternalInput.discr2DDiffModel) {
                frame.getManifoldsAction().setEnabled(true);
                frame.getBasinACtion().setEnabled(true);
                frame.getAbsorbingAreaAction().setEnabled(true);
                discreteActions = true;
            }

            if (i == InternalInput.discr1DModel) {
                frame.getCowebAction().setEnabled(true);
                discreteActions = true;
            }
            else {
                frame.getCowebAction().setEnabled(false);                
            }

            frame.getCyclesACtion().setEnabled(discreteActions);
            frame.getTrajectoryAction().setEnabled(true);
            frame.getBifurAction().setEnabled(true);
            //frame.getBasinACtion().setEnabled(discreteActions);
            frame.getLyapunovAction().setEnabled(true);
        }
    };

    private final Transition confInfoTab = new Transition() {
        public void transition(final Input i) {
            frame.getOpenModelAction().setEnabled(true);
            frame.getCloseModelAction().setEnabled(true);
            frame.getClosePlotTabAction().setEnabled(false);
            frame.getSaveAsAction().setEnabled(false);
            frame.getSaveDataAction().setEnabled(false);            
        }
    };

    private final Transition confPlotTab = new Transition() {
        public void transition(final Input i) {
              frame.getClosePlotTabAction().setEnabled(true);
              frame.getSaveAsAction().setEnabled(true);
              // TODO: implement a save data method for each plot type
              AbstractPlotComponent c = (AbstractPlotComponent) frame.getSelectedPlotComponent();
              if((c instanceof BasinComponent) || 
            		  (c instanceof TrajectoryComponent) ||
            		  (c instanceof LyapunovComponent))
            	  frame.getSaveDataAction().setEnabled(true);
              else
            	  frame.getSaveDataAction().setEnabled(false);
        }
    };


    private final Transition newPlot = new Transition() {
        public void transition(final Input i) {
            Model model = frame.duplicateModel();

            if (model == null) {
                return;
            }

            if (i == NewPlotInput.basin) {
                frame.newPlotComponent(new BasinComponent((SimpleMap) model,frame));
            }
            else if (i == NewPlotInput.basinslice) {
                frame.newPlotComponent(new BasinSliceComponent((SimpleMap) model,frame));
            }
            else if (i == NewPlotInput.cycles) {
                frame.newPlotComponent(new CyclesComponent((SimpleMap) model,frame));
            }
            else if (i == NewPlotInput.coweb) {
                frame.newPlotComponent(new CowebComponent((SimpleMap) model,frame));
            }
            else if (i == NewPlotInput.bifurcat) {
                frame.newPlotComponent(new BifurcationComponent(model,frame));
            }
            else if (i == NewPlotInput.discTraj) {
                frame.newPlotComponent(new TrajectoryComponent(model,frame));
            }
            else if (i == NewPlotInput.lyapunov) {
                frame.newPlotComponent(new LyapunovComponent(model,frame));
            }
            else if (i == NewPlotInput.manifolds) {
                frame.newPlotComponent(new ManifoldsComponent(model,frame));
            }
            else if (i == NewPlotInput.absorbingArea){
                frame.newPlotComponent(new AbsorbingAreaComponent((DifferentiableMap) model,frame));
            }
        }
    };

    private final Transition closeTab = new Transition() {
        public void transition(final Input i) {
            frame.closeCurrentTab();
        }
    };


    /*
     * Transition  matrix
     */
    private final Object[][][] table = new Object[][][]
    {

// initial state

        {
            {initialS},
            /*input 					condition	transition		new state*/
            {Input.go,                  null,      confNoModel,     noModelS},
            {UserActionInput.close,     null,      null,     finalS} // fast user... or this is executued by the awt thread mmhh...
        },

// no model loaded yet (or all closed)
        {
            {noModelS},
            
            {UserMenuInput.openModel,   openModel,    null,         noPlotS}, // sends contiModel or disrcModel or nothing
            {UserMenuInput.class,       null,         null,         noModelS},

            {InternalInput.contiModel,       null,         confNoPlot,   noPlotS},
            {InternalInput.discrModel,       null,         confNoPlot,   noPlotS},
            {InternalInput.discr2DDiffModel, null,         confNoPlot,   noPlotS},
            {InternalInput.discr1DModel,     null,         confNoPlot,   noPlotS},
            {InternalInput.infoTab,          null,         null,         noModelS},
        },

// model loaded but only info tab
        {
            {noPlotS},
            {UserMenuInput.closeModel,  closeModel,   confNoModel,  noModelS},

            {NewPlotInput.class,        null,         newPlot,      noPlotS},

            {UserMenuInput.openModel,   newModel,     null,         infoTabS},
            {UserMenuInput.class,       null,         null,         noPlotS},

            {InternalInput.infoTab,     null,         null,         noPlotS},
            {InternalInput.plotTab,     null,         confPlotTab,  plotTabS},
        },

        {
            {infoTabS},

            {NewPlotInput.class,        null,         newPlot,      plotTabS},

            {UserMenuInput.closeModel,  closeModel,   confNoModel,  noModelS},
            {UserMenuInput.openModel,   newModel,     null,         infoTabS},
            {UserMenuInput.class,       null,         null,         infoTabS},

            {InternalInput.noMorePlotTabs,  null,     null,         noPlotS},
            {InternalInput.infoTab,     null,         null,         infoTabS},
            {InternalInput.plotTab,     null,         confPlotTab,  plotTabS},

        },

        {
            {plotTabS},
            {UserMenuInput.closeTab,    null,         closeTab,     plotTabS},

            {UserMenuInput.closeModel,  closeModel,   confNoModel,  noModelS},
            {UserMenuInput.openModel,   newModel,     null,         plotTabS},
            {UserMenuInput.class,       null,         null,         plotTabS},

            {NewPlotInput.class,        null,         newPlot,      plotTabS},

            {InternalInput.noMorePlotTabs,  null,     null,         noPlotS},
            {InternalInput.infoTab,     null,         confInfoTab,  infoTabS},
            {InternalInput.plotTab,     null,         confPlotTab,         plotTabS},
        },

// finish
        {
            {finalS}, // final state
            {UserActionInput.class,     null,         null,         finalS}
        },
    };
}



final class UserMenuInput extends Input {

    UserMenuInput(final String name) {
        super(name);
    }

    static final UserMenuInput openModel = new UserMenuInput("openModel");
    static final UserMenuInput closeModel = new UserMenuInput("closeModel");

    static final UserMenuInput closeTab = new UserMenuInput("closeTab");
}

final class NewPlotInput extends Input {

    NewPlotInput(final String name) {
        super(name);
    }

    static final NewPlotInput absorbingArea = new NewPlotInput("absorbingArea");
    static final NewPlotInput discTraj = new NewPlotInput("discTraj");
    static final NewPlotInput cycles = new NewPlotInput("cycles");
    static final NewPlotInput coweb = new NewPlotInput("coweb");
    static final NewPlotInput bifurcat = new NewPlotInput("bifurcat");
    static final NewPlotInput basin = new NewPlotInput("basin");
    static final NewPlotInput basinslice = new NewPlotInput("basinslice");
    static final NewPlotInput lyapunov = new NewPlotInput("lyapunov");
    static final NewPlotInput manifolds = new NewPlotInput("manifolds");
}

final class InternalInput extends Input {

    InternalInput(final String name) {
        super(name);
    }

    static final InternalInput discrModel = new InternalInput("discrModel");
    static final InternalInput discr2DDiffModel = new InternalInput("discr2DDiffModel");
    static final InternalInput discr1DModel = new InternalInput("discr1DModel");
    static final InternalInput contiModel = new InternalInput("contiModel");
    static final InternalInput infoTab = new InternalInput("infoTab");
    static final InternalInput plotTab = new InternalInput("plotTab");

    static final InternalInput noMorePlotTabs = new InternalInput("noMorePlotTabs");
}


