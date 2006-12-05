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
package org.tsho.dmc2.ui.lyapunov;

import org.tsho.dmc2.managers.LyapunovManager;
import org.tsho.dmc2.sm.ComponentStateMachine;
import org.tsho.dmc2.sm.Condition;
import org.tsho.dmc2.sm.Input;
import org.tsho.dmc2.sm.ManagerError;
import org.tsho.dmc2.sm.ManagerInput;
import org.tsho.dmc2.sm.State;
import org.tsho.dmc2.sm.Transition;
import org.tsho.dmc2.sm.UserActionInput;
import org.tsho.dmc2.core.chart.LyapunovRenderer;

/*
 * The State Machine
 */
final class LyapunovFrameSM extends ComponentStateMachine {
    
    private final LyapunovSMItf frame;
    
    LyapunovFrameSM(final LyapunovSMItf frame) {
        super("LyapunovSM", LyapunovState.init);
        
        this.frame = frame;
        
        setUp(table);
        
        //? 21.7.2004 changed to false (from true)
        debug = false;
    }
    
    private final Condition renderStart = new Condition() {
        public boolean condition(final Input i) {
            return ((LyapunovManager) frame.getManager()).doRendering();
        }
    };
    
    private final Condition renderRedraw = new Condition() {
        public boolean condition(final Input i) {
            LyapunovManager manager=(LyapunovManager) frame.getManager();
            LyapunovRenderer renderer = manager.getRenderer();
            
            //to decomment after renderer is OK
            //renderer.setHoldLegend=true;
            //renderer.setRedrawExistingChart=true;
            renderer.setPass(renderer.getPass()+1);
            boolean b=true;
            if (renderer.getPass()==1)
                b=manager.doRendering();
            else
                renderer.setPass(0);
            
            return b;
        }
    };
    
    private final Condition showErrorDialog = new Condition() {
        public boolean condition(final Input i) {
            frame.showInvalidDataDialog(((ManagerError) i).getMessage());
            return true;
        }
    };
    
    private final Condition notAreaChart = new Condition() {
        public boolean condition(final Input i) {
            try{
                LyapunovManager manager=(LyapunovManager) frame.getManager();
                if (manager.getType()== LyapunovControlForm2.TYPE_PAR_SPACE)
                    return false;
                else
                    return true;
            }
            catch(Exception e){
                return true;//needed if the Lyapunov plot component is being closed while rendering
            }
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
            try{
                LyapunovManager manager=(LyapunovManager) frame.getManager();
                LyapunovRenderer renderer = manager.getRenderer();
                
                if (manager.getType()!=LyapunovControlForm2.TYPE_PAR_SPACE || renderer.getPass()==0){
                    setSensibleItemsEnabled(true);
                    
                    frame.getStartAction().setEnabled(true);
                    frame.getStopAction().setEnabled(false);
                    frame.getClearAction().setEnabled(true);
                }
            }
            catch(Exception e){
                //exception can be thrown while closing the Lyapunov plot component while rendering
            }
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
            {LyapunovState.init},
            {Input.go,                 null,         init,          LyapunovState.ready},
            {Input.go,                 null,         null,          LyapunovState.ready},
            {UserActionInput.close,    null,         cleanup,       LyapunovState.fini} // fast user...
        },
        //
        {
            {LyapunovState.ready},
            
            {UserActionInput.start,    renderStart,  stopOnly,         LyapunovState.running},
            {UserActionInput.start,    null,         null,             LyapunovState.ready},
            {UserActionInput.clear,    null,         managerClear,     LyapunovState.clearing},
            {UserActionInput.close,    null,         cleanup,          LyapunovState.fini},
            
            {ManagerInput.start,   null,         stopOnly,         LyapunovState.running}, // user zoomed
            {ManagerInput.end,  null,         reset,            LyapunovState.ready},
            
            {ManagerError.class,        showErrorDialog,         reset,  LyapunovState.ready}
        }, {
            {LyapunovState.running},
            
            {ManagerInput.start,   null,                 null,             LyapunovState.running},
            {ManagerInput.end,     notAreaChart,         reset,            LyapunovState.ready},
            {ManagerInput.end,     renderRedraw,         reset,            LyapunovState.ready},
            {ManagerInput.end,     null,                 reset,            LyapunovState.ready},
            {UserActionInput.close,    null,         cleanup,          LyapunovState.fini},
            
            {ManagerError.class,       showErrorDialog, reset,  LyapunovState.ready}
        }, {
            {LyapunovState.clearing},
            
            {ManagerInput.start,   null,         null,             LyapunovState.clearing},
            {ManagerInput.end,  null,         reset,            LyapunovState.ready},
            {UserActionInput.close,    null,         cleanup,          LyapunovState.fini},
            
            {ManagerError.class,       showErrorDialog, reset,  LyapunovState.ready}
        },
        // finish
        {
            {LyapunovState.fini}, // final state
            {ManagerInput.class,       null,          null,             LyapunovState.fini},
            {UserActionInput.class,   null,          null,             LyapunovState.fini}
        },
    };
}


final class LyapunovState extends State {
    LyapunovState(final String name) {
        super(name);
    }
    
    public static final
    LyapunovState init = new LyapunovState("init"); // initial state
    
    public static final
    LyapunovState ready = new LyapunovState("ready");
    public static final
    LyapunovState running = new LyapunovState("running");
    public static final
    LyapunovState clearing = new LyapunovState("clearing");
    
    public static final LyapunovState fini = new LyapunovState("fini"); // final state
}



