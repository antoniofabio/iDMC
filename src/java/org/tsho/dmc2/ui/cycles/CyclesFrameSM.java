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
package org.tsho.dmc2.ui.cycles;

import org.tsho.dmc2.managers.CyclesManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;

/*
 * The State Machine
 */
final class CyclesFrameSM extends ComponentStateMachine {

    private final CyclesSMItf frame;

    CyclesFrameSM(final CyclesSMItf frame) {
        super("CyclesSM", CyclesState.init);

        this.frame = frame;

        setUp(table);
    }

    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            if (!((CyclesManager) frame.getManager()).doRendering()) {
                frame.showInvalidDataDialog(frame.getManager().getErrorMessage());
                return false;
            }
            return true;
        }
    };

    private final Transition init = new Transition() {
        public void transition(final Input i) {
            frame.getStopAction().setEnabled(false);
        }
    };

    private final Transition stopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
            frame.getClearAction().setEnabled(false);
        }
    };

    private final Transition reset = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getClearAction().setEnabled(true);
        }
    };

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

    private final Transition showErrorDialog = new Transition() {
        public void transition(final Input i) {
            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
        }
    };


    private final Object[][][] table = new Object[][][]
    {
// init
        {
            {CyclesState.init},
            {Input.go,                 null,         init,             CyclesState.ready},
            {Input.go,                 null,         null,             CyclesState.ready},
            {UserActionInput.close,    null,         cleanup,          CyclesState.fini} // fast user...
        },
//
        {
            {CyclesState.ready},

            {UserActionInput.start,    renderStart,  stopOnly,         CyclesState.running},
            {UserActionInput.start,    null,         null,             CyclesState.ready},
            {UserActionInput.clear,    null,         managerClear,     CyclesState.clearing},
            {UserActionInput.close,    null,         cleanup,          CyclesState.fini},

            {ManagerInput.start,       null,         stopOnly,         CyclesState.running}, // user zoomed
            {ManagerInput.end,         null,         reset,            CyclesState.ready},

            {ManagerError.class,       null,         showErrorDialog,  CyclesState.ready}
        },
        {
            {CyclesState.running},

            {ManagerInput.start,       null,         null,             CyclesState.running},
            {ManagerInput.end,         null,         reset,            CyclesState.ready},
            {UserActionInput.close,    null,         cleanup,          CyclesState.fini}
        },
        {
            {CyclesState.clearing},

            {ManagerInput.start,       null,         null,             CyclesState.clearing},
            {ManagerInput.end,         null,         reset,            CyclesState.ready},
            {UserActionInput.close,    null,         cleanup,          CyclesState.fini}
        },
// finish
        {
            {CyclesState.fini}, // final state
            {ManagerInput.class,       null,          null,             CyclesState.fini},
            {UserActionInput.class,    null,          null,             CyclesState.fini}
        },
    };
}


final class CyclesState extends State {
    CyclesState(final String name) {
        super(name);
    }

    public static final
            CyclesState init = new CyclesState("init"); // initial state

    public static final
            CyclesState ready = new CyclesState("ready");
    public static final
            CyclesState running = new CyclesState("running");
    public static final
            CyclesState clearing = new CyclesState("clearing");


    public static final CyclesState fini = new CyclesState("fini"); // final state
}

final class ManagerError extends Input {
    String message;

    ManagerError(final String message) {
        super("ManagerError");

        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}




