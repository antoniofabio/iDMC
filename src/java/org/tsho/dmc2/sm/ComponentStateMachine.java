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
package org.tsho.dmc2.sm;

import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JComponent;

public abstract class ComponentStateMachine extends StateMachine {

    // TODO the following ArrayLists don't make much point, they
    // are fixed.

    /*
     * "sensible": components to be disabled when
     * the drawing is "active", that is saying, tipically, from
     * when the user presses start to when he presses
     * reset or clear (or close).
     */
    private final ArrayList sensibleItems;
    private boolean sensiblesEnabled = true;

    /*
     * Components to be disabled while the painting thread
     * is running.
     */
    private final ArrayList noRunItems;
    private boolean noRunsEnabled = true;

    public ComponentStateMachine(final String name, final State initial) {
        super(name, initial);

        sensibleItems = new ArrayList();
        noRunItems = new ArrayList();
    }

    public void addSensibleItem(final Object item) {
        sensibleItems.add(item);
    }

    public void addNoRunItem(final Object item) {
        noRunItems.add(item);
    }

    public synchronized void addAndConfigureSensibleItem(final Object item) {
        enableItem(item, sensiblesEnabled);
        addSensibleItem(item);
    }

    /**
     *
     * @param item
     */    
    public synchronized void addAndConfigureNoRunItem(final Object item) {
        enableItem(item, noRunsEnabled);
        addNoRunItem(item);
    }

    public void removeSensibleItem(final Object item) {
        sensibleItems.remove(item);
    }

    public void removeNoRunItem(final Object item) {
        noRunItems.remove(item);
    }



    protected void setSensibleItemsEnabled(final boolean flag) {
        sensiblesEnabled = flag;
        setComponentListEnabled(sensibleItems, flag);
    }

    protected void setNoRunItemsEnabled(final boolean flag) {
        noRunsEnabled = flag;
        setComponentListEnabled(noRunItems, flag);
    }

    // TODO convert this to fixed sixe array
    // or use ArrayList.trimToSize
    /*
     * @deprecated
     */
    private static void setComponentListEnabled(
            final ArrayList list, final boolean flag) {

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            enableItem(obj, flag);
        }
    }

    private static void enableItem(final Object item, final boolean enabled) {
        if (item instanceof Action) {
            ((Action) item).setEnabled(enabled);
        }
        else {
            ((JComponent) item).setEnabled(enabled);
        }
    }
}

