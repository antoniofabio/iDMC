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
package org.tsho.dmc2.ui.basinslice;

import org.tsho.dmc2.managers.BasinSliceManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;

/*
 * The State Machine
 */
final class BasinSliceFrameSM extends ComponentStateMachine {

    private final BasinSliceSMItf frame;

    BasinSliceFrameSM(final BasinSliceSMItf frame) {
        super("BasinSliceSM", BasinState.init);

        this.frame = frame;

        setUp(table);
    }

    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            if (!((BasinSliceManager) frame.getManager()).doRendering()) {
                frame.showInvalidDataDialog(frame.getManager().getErrorMessage());
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
            frame.getColorSettingsAction().setEnabled(false);
            frame.getClearAction().setEnabled(false);
        }
    };

    private final Transition reset = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getColorSettingsAction().setEnabled(true);
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

    private final Object[][][] table = new Object[][][]
    {
// init
        {
            {BasinState.init},
            {Input.go,                 null,         init,             BasinState.ready},
            {Input.go,                 null,         null,             BasinState.ready},
            {UserActionInput.close,    null,         cleanup,          BasinState.fini} // fast user...
        },
//
        {
            {BasinState.ready},

            {UserActionInput.start,    renderStart,  stopOnly,         BasinState.running},
            {UserActionInput.start,    null,         null,             BasinState.ready},
            {UserActionInput.clear,    null,         managerClear,     BasinState.clearing},
            {UserActionInput.close,    null,         cleanup,          BasinState.fini},

            {ManagerInput.start,       null,         stopOnly,         BasinState.running}, // user zoomed
            {ManagerInput.end,         null,         reset,            BasinState.ready},

            {ManagerError.class,       showErrorDialog,        reset,  BasinState.ready}
        },
        {
            {BasinState.running},

            {ManagerInput.start,       null,         null,             BasinState.running},
            {ManagerInput.end,         null,         reset,            BasinState.ready},
            {UserActionInput.close,    null,         cleanup,          BasinState.fini},

            {ManagerError.class,       showErrorDialog,        reset,  BasinState.ready}
        },
        {
            {BasinState.clearing},

            {ManagerInput.start,       null,         null,             BasinState.clearing},
            {ManagerInput.end,         null,         reset,            BasinState.ready},
            {UserActionInput.close,    null,         cleanup,          BasinState.fini},

            {ManagerError.class,       showErrorDialog,        reset,  BasinState.ready}
        },
// finish
        {
            {BasinState.fini}, // final state
            {ManagerInput.class,       null,          null,             BasinState.fini},
            {UserActionInput.class,    null,          null,             BasinState.fini}
        },
    };
}


final class BasinState extends State {
    BasinState(final String name) {
        super(name);
    }

    public static final
            BasinState init = new BasinState("init"); // initial state

    public static final
            BasinState ready = new BasinState("ready");
    public static final
            BasinState running = new BasinState("running");
    public static final
            BasinState clearing = new BasinState("clearing");


    public static final BasinState fini = new BasinState("fini"); // final state
}
