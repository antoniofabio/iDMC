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
package org.tsho.dmc2.ui.bifurcation;

import org.tsho.dmc2.managers.BifurcationManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;


class BifurcationFrameSM extends ComponentStateMachine {

    private final BifurcationSMItf frame;

    BifurcationFrameSM(final BifurcationSMItf frame) {
        super("BifurcationSM", BifState.init);

        this.frame = frame;

        setUp(table);
    }

    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            return ((BifurcationManager) frame.getManager()).doRendering(false);
        }
    };

    private final Condition renderRedraw = new Condition() {
        public boolean condition(final Input i) {
            return ((BifurcationManager) frame.getManager()).doRendering(true);
        }
    };

    private final Condition showErrorDialog = new Condition() {
        public boolean condition(final Input i) {
            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
            return true;
        }
    };

    private final Transition init = new Transition() {
        public void transition(final Input i) {
            addNoRunItem(frame.getClearAction());
            
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getClearAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
        }
    };

    private final Transition stopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
            frame.getClearAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
        }
    };

    private final Transition reset = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getClearAction().setEnabled(true);
            frame.getRedrawAction().setEnabled(true);
        }
    };

    private final Transition managerClear = new Transition() {
        public void transition(final Input i) {
            frame.getManager().clear();

            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getClearAction().setEnabled(false);
            frame.getRedrawAction().setEnabled(false);
        }
    };

    // note that we don't wait for thread to really finish...
    private final Transition cleanup = new Transition() {
        public void transition(final Input i) {
            frame.getManager().stopRendering();
        }
    };

//    private final Transition showErrorDialog = new Transition() {
//        public void transition(final Input i) {
//            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
//        }
//    };


    private final Object[][][] table = new Object[][][]
    {
// init
        {
            {BifState.init},
            {Input.go,                  null,         init,            BifState.ready},
            {Input.go,                  null,         null,            BifState.ready},
            {UserActionInput.close,     null,         cleanup,         BifState.fini} // fast user...
        },
//
        {
            {BifState.ready},

            {UserActionInput.start,    renderStart,  stopOnly,         BifState.running},
            {UserActionInput.start,    null,         null,             BifState.ready},
            {UserActionInput.redraw,   renderRedraw, stopOnly,         BifState.running},
            {UserActionInput.clear,    null,         managerClear,     BifState.clearing},
            {UserActionInput.close,    null,         cleanup,          BifState.fini},

            {ManagerInput.start,       null,         stopOnly,         BifState.running}, // user zoomed
            {ManagerInput.end,         null,         reset,            BifState.ready},

            {ManagerError.class,       showErrorDialog, reset,  BifState.ready}
        },
        {
            {BifState.running},

            {ManagerInput.start,       null,          null,            BifState.running},
            {ManagerInput.end,         null,          reset,           BifState.ready},
            {UserActionInput.close,    null,          cleanup,         BifState.fini},

            {ManagerError.class,       showErrorDialog, reset,  BifState.ready}
        },
        {
            {BifState.clearing},

            {ManagerInput.start,       null,          null,            BifState.clearing},
            {ManagerInput.end,         null,          null,            BifState.ready},
            {UserActionInput.close,    null,          cleanup,         BifState.fini},

            {ManagerError.class,       showErrorDialog, reset,  BifState.ready}
        },
// finish
        {
            {BifState.fini}, // final state
            {ManagerInput.class,       null,          null,            BifState.fini},
            {UserActionInput.class,    null,          null,            BifState.fini}
        },
    };
}


final class BifState extends State {
    BifState(final String name) {
        super(name);
    }

    public static final
            BifState init = new BifState("init"); // initial state
    public static final
            BifState ready = new BifState("ready");
    public static final
            BifState running = new BifState("running");
    public static final
            BifState clearing = new BifState("clearing");
    public static final
            BifState fini = new BifState("fini"); // final state
}

