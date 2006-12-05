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
package org.tsho.dmc2.ui.manifolds;

import java.util.ArrayList;

import org.tsho.dmc2.managers.ManifoldsManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;


class ManifoldsFrameSM extends ComponentStateMachine {

    private final ManifoldsSMItf frame;

    /*
     * Components to be disabled while the painting thread
     * is running.
     */
    private final ArrayList disableOnRunItems;

    ManifoldsFrameSM(final ManifoldsSMItf frame) {
        super("ManifoldsSM", ManifoldsState.init);

        this.frame = frame;
        //sensibleItems = new ArrayList();
        disableOnRunItems = new ArrayList();

        setUp(table);
    }

    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            return ((ManifoldsManager) frame.getManager()).doRendering(false);
//            if (!((ManifoldsManager) frame.getManager()).doRendering(false)) {
//                frame.showInvalidDataDialog(frame.getManager().getErrorMessage());
//                return false;
//            }
//            return true;
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
        }
    };

    private final Transition stopOnly = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(false);
            setNoRunItemsEnabled(false);

            frame.getStartAction().setEnabled(false);
            frame.getStopAction().setEnabled(true);
            frame.getClearAction().setEnabled(false);
        }
    };

    private final Transition reset = new Transition() {
        public void transition(final Input i) {
            setSensibleItemsEnabled(true);
            setNoRunItemsEnabled(true);

            frame.getStartAction().setEnabled(true);
            frame.getStopAction().setEnabled(false);
            frame.getClearAction().setEnabled(true);
        }
    };

    private final Transition lock = new Transition() {
        public void transition(final Input i) {
            setNoRunItemsEnabled(true);
            //setComponentListEnabled(disableOnRunItems, true);

            frame.getStartAction().setEnabled(false);
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

    private final Object[][][] table = new Object[][][]
    {
// init
        {
            {ManifoldsState.init},
            {Input.go,                  null,         init,             ManifoldsState.ready},
            {Input.go,                  null,         null,             ManifoldsState.ready},
            {UserActionInput.close,    null,         cleanup,          ManifoldsState.fini} // fast user...
        },
//
        {
            {ManifoldsState.ready},

            {UserActionInput.start,    renderStart,  stopOnly,         ManifoldsState.running},
            {UserActionInput.start,    null,         null,             ManifoldsState.ready},
            {UserActionInput.clear,    null,         managerClear,     ManifoldsState.clearing},
            {UserActionInput.close,    null,         cleanup,          ManifoldsState.fini},

            {ManagerInput.start,       null,            stopOnly,      ManifoldsState.running}, // user zoomed
            {ManagerInput.end,         null,            reset,         ManifoldsState.ready},

            {ManagerError.class,       showErrorDialog, reset,         ManifoldsState.ready}
        },
        {
            {ManifoldsState.running},

            {ManagerInput.start,   null,          null,            ManifoldsState.running},
            {ManagerInput.end,  null,          reset,            ManifoldsState.ready},
            {UserActionInput.close,    null,          cleanup,         ManifoldsState.fini},
            
            {ManagerError.class,       showErrorDialog, reset,         ManifoldsState.ready}
        },
        {
            {ManifoldsState.clearing},

            {ManagerInput.start,   null,          null,            ManifoldsState.clearing},
            {ManagerInput.end,  null,          reset,           ManifoldsState.ready},
            {UserActionInput.close,    null,          cleanup,         ManifoldsState.fini},
            
            {ManagerError.class,       showErrorDialog, reset,         ManifoldsState.ready}
        },
// finish
        {
            {ManifoldsState.fini}, // final state
            {ManagerInput.class,       null,            null,           ManifoldsState.fini},
            {UserActionInput.class,   null,            null,           ManifoldsState.fini}
        },
    };
}


final class ManifoldsState extends State {
    ManifoldsState(final String name) {
        super(name);
    }

    public static final
            ManifoldsState init = new ManifoldsState("init"); // initial state
    public static final
            ManifoldsState ready = new ManifoldsState("ready");
    public static final
            ManifoldsState running = new ManifoldsState("running");
    public static final
            ManifoldsState clearing = new ManifoldsState("clearing");
    public static final
            ManifoldsState fini = new ManifoldsState("fini"); // final state
}
