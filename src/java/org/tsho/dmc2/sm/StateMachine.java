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
/*
 * Idea from Thinking in Patterns (with Java) by Bruce Eckel
 * http://www.BruceEckel.com
 */
package org.tsho.dmc2.sm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StateMachine {

    protected boolean debug = false;
    private State state;
    // this makes sense while calling transitions
    private State nextState;
    /**
     * Contains transitions map
     * */
    private Map map = new HashMap();

    private final String name;

    private StateMachine() {
        name = null;
    }

    public StateMachine(final String name, final State initial) {
        state = initial;
        this.name = name;
    }

    /**
     * Setup transition table. given idexes i,j,k, 
     * table[i][0][0] contains map keys.
     * */
    public void setUp(final Object[][][] table) {
        Object[][] subTable;
        Object st;

        for (int i = 0; i < table.length; i++) {
            subTable = table[i];
            st = subTable[0][0];
            List transList = new ArrayList();

            for (int j = 1; j < subTable.length; j++) {
                transList.add(subTable[j]);
            }
            map.put(st, transList);
        }
    }

    public synchronized void parseInput(final Input input) {
        Iterator iterator;
        
        /*Depending on current state, iterate trough transitions*/
        iterator = ((List) map.get(state)).iterator();

        if (debug) {
            System.out.println("StateMachine: " + name);
            System.out.println("got input : " + input.toString());
            System.out.println("     state: " + state.toString());
        }

        while (iterator.hasNext()) {
            Object[] transitions = (Object[]) iterator.next();

            if (input == transitions[0] || input.getClass() == transitions[0]) {

                if (transitions[1] != null) {


                    Condition c = (Condition) transitions[1];
                    if (!c.condition(input)) {
                        continue;
                    }
                }

                nextState = (State) transitions[3];

                if (debug) {
                    System.out.println("next state: " + nextState.toString());
                    System.out.println();
                }

                // TODO get rid of old Transition
                if (transitions[2] != null) {
                    ((Transition) transitions[2]).transition(input);
                }

                state = nextState;
                
                return;
            }
        }

        throw new RuntimeException(
                "StateMachine error - state: " + state + ", invalid input: " + input.toString());
    }


    public final State getNextState() {
        return nextState;
    }

    public final State getState() {
        return state;
    }
    
    //? 29.7.2004 quick and dirty solution to some bug
    public final void setState(State state){
        this.state=state;
    }

    protected void finalize() {
        //System.out.println("finalizing: " + getClass());
    }
   
}
