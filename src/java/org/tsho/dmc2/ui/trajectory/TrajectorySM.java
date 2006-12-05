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
package org.tsho.dmc2.ui.trajectory;

import org.tsho.dmc2.managers.TrajectoryManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.core.chart.jfree.DmcChartPanel;
import org.tsho.dmc2.ui.AbstractPlotComponent;


class TrajectorySM extends ComponentStateMachine {

    private final TrajectorySMItf frame;

    private static final State initS = new State("init"); // initial state

    private static final State normalReadyS = new State("normalReady");
    private static final State normalRunningS = new State("normalRunning");
    private static final State normalLockedS = new State("normalLocked");
    private static final State normalClearingS = new State("normalClearing");

    private static final State timeReadyS = new State("timeReady");
    private static final State timeRunningS = new State("timeRunning");
    private static final State timeLockedS = new State("timeLocked");
    private static final State timeClearingS = new State("timeClearing");

    private static final State finiS = new State("fini"); // final state

    TrajectorySM(final TrajectorySMItf frame) {
        super("ScatterSM", initS);
        this.frame = frame;

        setUp(table);
    }

    /*
     * check for 1-dimension model
     */
    private final Condition singleDim = new Condition() {
        public boolean condition(final Input i) {
            return (frame.getModel().getNVar() == 1 ? true : false);
        }
    };

    private final Condition render = new Condition() {
        public boolean condition(final Input i) {
            TrajectoryManager manager = (TrajectoryManager) frame.getManager();

            boolean ok = false;
            if (i == UserActionInput.start) {
                ok = manager.doRendering(false); 
            }
            else if (i == UserActionInput.continua) {
                ok = manager.continueRendering();
            }
            else if (i == UserActionInput.redraw) {
                ok = manager.doRendering(true);
            }
            else {
               //Modification: line below commented
                // assert (false);
            }

            if (!ok) {
                frame.showInvalidDataDialog(
                            frame.getManager().getErrorMessage());
                return false;
            }
            return true;
        }
    };

    private final Condition showErrorDialog = new Condition() {
        public boolean condition(final Input i) {
            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
            return true;
        }
    };

    private final Transition initMachine = new Transition() {
        public void transition(final Input i) {

            addSensibleItem(frame.getTimePlotMenuItem());
            addSensibleItem(frame.getControlForm());

            if (getNextState() == normalReadyS) {
                addSensibleItem(frame.getNormalPlotMenuItem());

//                frame.getShiftedPlotMenuItem().setEnabled(false);
                frame.getContinueAction().setEnabled(false);

                frame.getNormalPlotMenuItem().setSelected(true);

                initNormalMode.transition(null);
                normalResetted.transition(null);

                return;
            }
            else if (getNextState() == timeReadyS) {
//                addSensibleItem(frame.getShiftedPlotMenuItem());

                frame.getNormalPlotMenuItem().setEnabled(false);
                frame.getTimePlotMenuItem().setSelected(true);

                initTimeMode.transition(null);
                timeReset.transition(null);

                return;
            }

            throw new Error();
        }
    };


    /*
     * Normal mode
     */
    private final Transition initNormalMode = new Transition() {
        public void transition(final Input i) {
            ((TrajectoryControlForm2)frame.getControlForm()).setTime(false);

            frame.getContinueAction().setVisible(true);
            frame.getRedrawAction().setVisible(true);
            frame.getResetAction().setVisible(true);

            frame.getSlider().setVisible(true);
        }
    };

    private final Transition normalStopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);
            
            DmcChartPanel chartPanel=(DmcChartPanel) ((AbstractPlotComponent) frame).getManager().getChartPanel();
            chartPanel.setCrosshairNotBlocked(false);
            
            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
            frame.getContinueAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
            frame.getResetAction().setEnabled(false);
        }
    };

    private final Transition normalLock = new Transition() {
        public void transition(final Input i) {
            DmcChartPanel chartPanel=(DmcChartPanel) ((AbstractPlotComponent) frame).getManager().getChartPanel();
            chartPanel.setCrosshairNotBlocked(true);
            
            ((TrajectoryControlForm2)frame.getControlForm()).setIterationsEnabled(true);

            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(false);
            frame.getContinueAction().setEnabled(true);
            frame.getRedrawAction().setEnabled(true);
            frame.getResetAction().setEnabled(true);
        }
    };

    private final Transition normalResetted = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getContinueAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
            frame.getResetAction().setEnabled(false);
        }
    };

    /*
     * Time mode
     */
    private final Transition initTimeMode = new Transition() {
        public void transition(final Input i) {
        	((TrajectoryControlForm2)frame.getControlForm()).setTime(true);

            frame.getContinueAction().setVisible(false);
            frame.getRedrawAction().setVisible(true);
            frame.getResetAction().setVisible(true);

            frame.getSlider().setVisible(true);
        }
    };

    private final Transition timeStopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);
            
            DmcChartPanel chartPanel=(DmcChartPanel) ((AbstractPlotComponent) frame).getManager().getChartPanel();
            chartPanel.setCrosshairNotBlocked(false);
            
            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
            frame.getRedrawAction().setEnabled(false);
            frame.getResetAction().setEnabled(false);
        }
    };

    private final Transition timeLock = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(true);
            
            DmcChartPanel chartPanel=(DmcChartPanel) ((AbstractPlotComponent) frame).getManager().getChartPanel();
            chartPanel.setCrosshairNotBlocked(true);
            
            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(true);
            frame.getResetAction().setEnabled(true);
        }
    };

    private final Transition timeReset = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
            frame.getResetAction().setEnabled(false);
        }
    };

    /*
     * Misc
     */
    private final Transition managerClear = new Transition() {
        public void transition(final Input i) {
            frame.getManager().clear();
        }
    };

    // note that we don't wait for thread to really finish...
    private final Transition cleanup = new Transition() {
        public void transition(final Input i) {
            frame.getManager().stopRendering();
        }
    };

    /*
     * Transition  matrix
     */
    private final Object[][][] table = new Object[][][]
    {
// init
        {
            {initS},
            {Input.go,                  singleDim,      initMachine,     timeReadyS},
            {Input.go,                  null,           initMachine,     normalReadyS},
            {UserActionInput.close,     null,           cleanup,         finiS} // fast user... or this is executued by the awt thread mmhh...
        },

// normal
        {
            {normalReadyS},
            {UserActionInput.start,    render,          normalStopOnly,  normalRunningS},
            {UserActionInput.start,     null,           null,            normalReadyS},
            {UserActionInput.clear,     null,           managerClear,    normalClearingS},
            {UserActionInput.close,     null,           cleanup,         finiS},
            {UserMenuInput.normalMode,  null,           null,            normalReadyS},
            {UserMenuInput.timeMode,    null,           initTimeMode,    timeReadyS},

            {ManagerInput.start,        null,           normalStopOnly,  normalRunningS}, // user zoomed

            {ManagerInput.end,          null,           null,  normalReadyS} // this arrives after an error
        },
        {
            {normalRunningS},
            {ManagerInput.start,        null,           null,            normalRunningS},  // the thread sends an input we ignore
            {ManagerInput.end,          null,           normalLock,      normalLockedS},
            {UserActionInput.close,     null,           cleanup,         finiS},

            {ManagerError.class,       showErrorDialog, normalResetted,  normalReadyS}
        },
        {
            {normalLockedS},
            {UserActionInput.continua, render,         normalStopOnly,  normalRunningS},
            {UserActionInput.continua, null,           null,            normalLockedS},
            {UserActionInput.redraw,   render,         normalStopOnly,  normalRunningS},
            {UserActionInput.redraw,   null,           null,            normalLockedS},
            {UserActionInput.reset,    null,           normalResetted,  normalReadyS},
            {UserActionInput.clear,    null,           managerClear,    normalClearingS},
            {UserActionInput.close,    null,           cleanup,         finiS},

            {ManagerInput.start,        null,          normalStopOnly,  normalRunningS}, // user zoomed
            {ManagerInput.end,          null,          normalLock,      normalLockedS},
        },
        {
            {normalClearingS},
            {ManagerInput.start,       null,           null,            normalClearingS},
            {ManagerInput.end,         null,           normalResetted,  normalReadyS},
            {UserActionInput.close,    null,           cleanup,         finiS}
        },
// time
        {
            {timeReadyS},
            {UserActionInput.start,     render,        timeStopOnly,      timeRunningS},
            {UserActionInput.start,     null,          null,              timeReadyS},
            {UserActionInput.clear,     null,          managerClear,      timeClearingS},
            {UserActionInput.close,     null,          cleanup,           finiS},
            {UserActionInput.redraw,    render,        timeStopOnly,      timeRunningS},
            {UserActionInput.redraw,    null,          null,              timeReadyS},

            {UserMenuInput.normalMode,  null,          initNormalMode,    normalReadyS},
            {UserMenuInput.timeMode,    null,          null,              timeReadyS},

            {ManagerInput.start,        null,          timeStopOnly,      timeRunningS}, // user zoomed

            {ManagerInput.end,          null,           null,            timeReadyS} // this arrives after an error
        },
        {
            {timeLockedS},
            {UserActionInput.redraw,    render,        timeStopOnly,    timeRunningS},
            {UserActionInput.redraw,    null,          null,            timeLockedS},
            {UserActionInput.reset,     null,          timeReset,       timeReadyS},
            {UserActionInput.clear,     null,          managerClear,    timeClearingS},
            {UserActionInput.close,     null,          cleanup,         finiS},

            {UserMenuInput.timeMode,    null,           initTimeMode,   timeReadyS},

            {ManagerInput.start,        null,           timeStopOnly,   timeRunningS}, // user zoomed
            {ManagerInput.end,          null,           timeLock,       timeLockedS},
        },
        {
            {timeRunningS},
            {ManagerInput.start,       null,           null,            timeRunningS},
            {ManagerInput.end,         null,           timeLock,        timeLockedS},
            {UserActionInput.close,    null,           cleanup,         finiS},
            {ManagerError.class,        showErrorDialog,timeReset,      timeReadyS}
        },
        {
            {timeClearingS},
            {ManagerInput.start,       null,           null,            timeClearingS},
            {ManagerInput.end,         null,           timeReset,       timeReadyS},
            {UserActionInput.close,    null,           cleanup,         finiS}
        },
// finish
        {
            {finiS}, // final state
            {ManagerInput.class,        null,           null,           finiS},
            {UserActionInput.class,    null,           null,           finiS}
        },
    };
}

final class UserMenuInput extends Input {

    UserMenuInput(final String name) {
        super(name);
    }

    static final UserMenuInput normalMode = new UserMenuInput("normalMode");
    static final UserMenuInput timeMode = new UserMenuInput("timeMode");
}
