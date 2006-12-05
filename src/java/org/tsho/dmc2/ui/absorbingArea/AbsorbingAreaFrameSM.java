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
package org.tsho.dmc2.ui.absorbingArea;

import org.tsho.dmc2.managers.AbsorbingAreaManager;
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
final class AbsorbingAreaFrameSM extends ComponentStateMachine {
    
    private final AbsorbingAreaSMItf frame;
    
    AbsorbingAreaFrameSM(final AbsorbingAreaSMItf frame) {
        super("AbsorbingAreaSM", AbsorbingAreaState.init);
        
        this.frame = frame;
        
        setUp(table);
    }
    
    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            if (!((AbsorbingAreaManager) frame.getManager()).doRendering()) {
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
    
    
    
    private final Object[][][] table = new Object[][][] {
        // init
        {
            {AbsorbingAreaState.init},
            {Input.go,                 null,         init,             AbsorbingAreaState.ready},
            {Input.go,                 null,         null,             AbsorbingAreaState.ready},
            {UserActionInput.close,    null,         cleanup,          AbsorbingAreaState.fini} // fast user...
        },
        //
        {
            {AbsorbingAreaState.ready},
            
            {UserActionInput.start,    renderStart,  stopOnly,         AbsorbingAreaState.drawingCriticalLocus},
            {UserActionInput.start,    null,         null,             AbsorbingAreaState.ready},
            {UserActionInput.clear,    null,         managerClear,     AbsorbingAreaState.clearing},
            {UserActionInput.close,    null,         cleanup,          AbsorbingAreaState.fini},
            
            {ManagerInput.start,       null,         stopOnly,         AbsorbingAreaState.drawingCriticalLocus}, // user zoomed
            {ManagerInput.end,         null,         reset,            AbsorbingAreaState.ready},
            
            {ManagerError.class,       showErrorDialog,        reset,  AbsorbingAreaState.ready}
        }, {
            {AbsorbingAreaState.drawingCriticalLocus},
            
            {ManagerInput.start,       null,         null,             AbsorbingAreaState.drawingCriticalLocus},
            {ManagerInput.end,         null,         reset,            AbsorbingAreaState.ready},
            {UserActionInput.close,    null,         cleanup,          AbsorbingAreaState.fini},
            
            {ManagerError.class,       showErrorDialog,        reset,  AbsorbingAreaState.ready}
        }, {
            {AbsorbingAreaState.clearing},
            
            {ManagerInput.start,       null,         null,             AbsorbingAreaState.clearing},
            {ManagerInput.end,         null,         reset,            AbsorbingAreaState.ready},
            {UserActionInput.close,    null,         cleanup,          AbsorbingAreaState.fini},
            
            {ManagerError.class,       showErrorDialog,        reset,  AbsorbingAreaState.ready}
        },
        // finish
        {
            {AbsorbingAreaState.fini}, // final state
            {ManagerInput.class,       null,          null,             AbsorbingAreaState.fini},
            {UserActionInput.class,    null,          null,             AbsorbingAreaState.fini}
        },
    };
}


final class AbsorbingAreaState extends State {
    AbsorbingAreaState(final String name) {
        super(name);
    }
    
    public static final
    AbsorbingAreaState init = new AbsorbingAreaState("init"); // initial state
    public static final
    AbsorbingAreaState ready = new AbsorbingAreaState("ready");
    public static final
    AbsorbingAreaState drawingCriticalLocus = new AbsorbingAreaState("drawingCriticalLocus");
    public static final
    AbsorbingAreaState clearing = new AbsorbingAreaState("clearing");
    /*public static final
            AbsorbingAreaState choosingSegments = new AbsorbingAreaState("choosingSegments");
    public static final
            AbsorbingAreaState drawingAttractor = new AbsorbingAreaState("drawingAttractor");
    public static final
            AbsorbingAreaState iteratingCriticalLocus = new AbsorbingAreaState("iteratingCriticalLocus");
    public static final
            AbsorbingAreaState iteratingChosenSegments = new AbsorbingAreaState("iteratingChosenSegments");
     */
    public static final
            AbsorbingAreaState fini = new AbsorbingAreaState("fini"); // final state
}
