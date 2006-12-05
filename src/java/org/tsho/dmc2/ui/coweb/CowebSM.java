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
package org.tsho.dmc2.ui.coweb;

import org.tsho.dmc2.managers.CowebManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;


class CowebSM extends ComponentStateMachine {

    private final CowebSMItf frame;

    private static final State initS = new State("init"); // initial state

    private static final State shiftReadyS = new State("shiftReady");
    private static final State shiftRunningS = new State("shiftRunning");
//    private static final State shiftLockedS = new State("shiftLocked");
    private static final State shiftClearingS = new State("shiftClearing");

    private static final State cowebReadyS = new State("normalReady");
    private static final State cowebRunningS = new State("normalRunning");
//    private static final State cowebLockedS = new State("normalLocked");
    private static final State cowebClearingS = new State("normalClearing");

    private static final State finiS = new State("fini"); // final state


    CowebSM(final CowebSMItf frame) {
        super("ScatterSM", initS);
        this.frame = frame;

        setUp(table);
    }

    private final Condition showErrorDialog = new Condition() {
        public boolean condition(final Input i) {
            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
            return true;
        }
    };

    private final Condition render = new Condition() {
        public boolean condition(final Input i) {
            CowebManager manager = (CowebManager) frame.getManager();

            boolean ok = false;            
            if (i == UserActionInput.start) {
                ok = manager.doRendering(false); 
            }
//            else if (i == UserActionInput.continua) {
//                ok = manager.continueRendering();
//            }
            else if (i == UserActionInput.redraw) {
                ok = manager.doRendering(true);
            }
            else {
                //Modification: line below commented
                //assert (false);
            }

            if (!ok) {
                frame.showInvalidDataDialog(
                            frame.getManager().getErrorMessage());
                return false;
            }
            return true;
        }
    };

    private final Transition initMachine = new Transition() {
        public void transition(final Input i) {

            addSensibleItem(frame.getControlForm());
            addSensibleItem(frame.getCowebPlotMenuItem());

//            frame.getCowebPlotMenuItem().setSelected(true);
            ((CowebControlForm2)frame.getControlForm()).setPlotType(CowebControlForm2.TYPE_SHIFTED);
            
            //frame.getControlForm().setAutomaticBounds(false);

            initShiftMode.transition(null);
            shiftResetted.transition(null);
        }
    };

    /*
     * Shift mode
     */
    private final Transition initShiftMode = new Transition() {
        public void transition(final Input i) {
        	((CowebControlForm2)frame.getControlForm()).setPlotType(CowebControlForm2.TYPE_SHIFTED);

//            frame.getContinueAction().setVisible(true);
//            frame.getRedrawAction().setVisible(true);
//            frame.getResetAction().setVisible(true);

            frame.getSlider().setVisible(false);
        }
    };

    private final Transition shiftStopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
//            frame.getContinueAction().setEnabled(false);
//            frame.getRedrawAction().setEnabled(false);
//            frame.getResetAction().setEnabled(false);
        }
    };

//    private final Transition shiftLock = new Transition() {
//        public void transition(final Input i) {
////          frame.getControlForm().setIterationsEnabled(true);
//
//          setNoRunItemsEnabled(true);
//
//          frame.getStartAction().setEnabled(false);
//          frame.getStopAction().setEnabled(false);
////          frame.getContinueAction().setEnabled(true);
////          frame.getRedrawAction().setEnabled(true);
////          frame.getResetAction().setEnabled(true);
//        }
//    };

    private final Transition shiftResetted = new Transition() {
        public void transition(final Input i) {
          setSensibleItemsEnabled(true);
          setNoRunItemsEnabled(true);

          frame.getStartAction().setEnabled(true);
          frame.getStopAction().setEnabled(false);
//          frame.getContinueAction().setEnabled(false);
//          frame.getRedrawAction().setEnabled(false);
//          frame.getResetAction().setEnabled(false);
        }
    };



    /*
     * Coweb mode
     */
    private final Transition initCowebMode = new Transition() {
        public void transition(Input i) {
        	((CowebControlForm2)frame.getControlForm()).setPlotType(CowebControlForm2.TYPE_COWEB);

//            frame.getContinueAction().setVisible(false);
            //frame.getRedrawAction().setVisible(false);
//            frame.getResetAction().setVisible(false);
            
            frame.getSlider().setVisible(true);
            
            cowebResetted.transition(null);
        }
    };

    private final Transition cowebStopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
        }
    };

    // the same as resetted but with redraw enabled
//    private final Transition cowebLock = new Transition() {
//        public void transition(Input i) {
//            setSensibleItemsEnabled(true);
//            setNoRunItemsEnabled(true);
//
//            frame.getStartAction().setEnabled(true);
//            frame.getStopAction().setEnabled(false);
////            frame.getRedrawAction().setEnabled(true);
//        }
//    };

    private final Transition cowebResetted = new Transition() {
        public void transition(Input i) {
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
//            frame.getRedrawAction().setEnabled(false);
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
            {Input.go,                  null,           initMachine,     shiftReadyS},
            {UserActionInput.close,     null,           cleanup,         finiS} // fast user... or this is executued by the awt thread mmhh...
        },

// coweb
        {
            {cowebReadyS},
            {UserActionInput.start,    render,          cowebStopOnly,  cowebRunningS},
            {UserActionInput.start,     null,           null,            cowebReadyS},
            {UserActionInput.clear,     null,           managerClear,    cowebClearingS},
            {UserActionInput.close,     null,           cleanup,         finiS},
            {UserMenuInput.cowebMode,   null,           null,            cowebReadyS},
            {UserMenuInput.shiftMode,   null,           initShiftMode,   shiftReadyS},

            {ManagerInput.start,        null,           cowebStopOnly,   cowebRunningS}, // user zoomed
            {ManagerInput.end,          null,           null,            cowebReadyS}, // this arrives after an error
            
            {ManagerError.class,        showErrorDialog, cowebResetted, cowebReadyS}
        },
        {
            {cowebRunningS},
            {ManagerInput.start,        null,           null,            cowebRunningS},  // the thread sends an input we ignore
            {ManagerInput.end,          null,           cowebResetted,   cowebReadyS},
            {UserActionInput.close,     null,           cleanup,         finiS},
            
            {ManagerError.class,        showErrorDialog, cowebResetted, cowebReadyS}
        },
        {
            {cowebClearingS},
            {ManagerInput.start,       null,           null,            cowebClearingS},
            {ManagerInput.end,         null,           cowebResetted,  cowebReadyS},
            {UserActionInput.close,    null,           cleanup,         finiS}
        },
// shift
        {
            {shiftReadyS},
            {UserActionInput.start,     render,         shiftStopOnly,   shiftRunningS},
            {UserActionInput.start,     null,           null,            shiftReadyS},
            {UserActionInput.clear,     null,           managerClear,    shiftClearingS},
            {UserActionInput.close,     null,           cleanup,         finiS},
            {UserMenuInput.cowebMode,  null,            initCowebMode,  cowebReadyS},
            {UserMenuInput.shiftMode,   null,           null,            shiftReadyS},

            {ManagerInput.start,        null,           shiftStopOnly,   shiftRunningS}, // user zoomed
            {ManagerInput.end,          null,           null,            shiftReadyS}, // this arrives after an error
            
            {ManagerError.class,        showErrorDialog, shiftResetted, shiftReadyS}
        },
        {
            {shiftRunningS},
            {ManagerInput.start,        null,           null,            shiftRunningS},
            {ManagerInput.end,          null,           shiftResetted,   shiftReadyS},
            {UserActionInput.close,     null,           cleanup,         finiS},
            
            {ManagerError.class,        showErrorDialog, shiftResetted, shiftReadyS}
        },
        {
            {shiftClearingS},
            {ManagerInput.start,        null,           null,           shiftClearingS},
            {ManagerInput.end,          null,           shiftResetted,  shiftReadyS},
            {UserActionInput.close,     null,           cleanup,        finiS},
            
            {ManagerError.class,        showErrorDialog, shiftResetted, shiftReadyS}
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

    static final UserMenuInput shiftMode = new UserMenuInput("shiftMode");
    static final UserMenuInput cowebMode = new UserMenuInput("cowebMode");
}
