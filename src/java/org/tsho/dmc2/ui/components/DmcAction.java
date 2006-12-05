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
package org.tsho.dmc2.ui.components;

import javax.swing.AbstractAction;


/**
 * An action with a visibility property. Use with DmcMenuItem & DmcButton 
 */
public abstract class DmcAction extends AbstractAction {
    public static final String VISIBILITY_KEY = "visible";

        public DmcAction() {
            super();

            putValue(VISIBILITY_KEY, new Boolean(true));
        }

    public void setVisible(final boolean visible) {

        Boolean oldValue = (Boolean) this.getValue(VISIBILITY_KEY);

        if (visible == oldValue.booleanValue()) {
            return;
        }

        Boolean b = new Boolean(visible);
        putValue(VISIBILITY_KEY, b);

        firePropertyChange(VISIBILITY_KEY, oldValue, b);
    }

    public boolean isVisible() {
        return ((Boolean) getValue(VISIBILITY_KEY)).booleanValue();
    }
 }
