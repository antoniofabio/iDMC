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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.tsho.dmc2.ui.AbstractPlotFrame;

/**
 * This adds visibility support by actions
 * 
 */
// TODO is there a way to do the same thing in a simpler way??
public class DmcMenuItem extends JMenuItem {

    public DmcMenuItem(final DmcAction a) {
        super(a);
    }

    protected void configurePropertiesFromAction(final Action a) {
        super.configurePropertiesFromAction(a);

        setVisible(((DmcAction) a).isVisible());
    }

    protected PropertyChangeListener createActionPropertyChangeListener(
            final Action a) {

        return new MyPropertyChangeListener(
                super.createActionPropertyChangeListener(a));
    }

    final class MyPropertyChangeListener implements PropertyChangeListener {
        PropertyChangeListener superListener;
        private MyPropertyChangeListener(PropertyChangeListener l) {
            superListener = l;
        }

        public void propertyChange(PropertyChangeEvent evt) {

            if (evt.getPropertyName().equals(
                    DmcAction.VISIBILITY_KEY)) {
               DmcMenuItem.this.setVisible(((Boolean) evt.getNewValue()).booleanValue());
            }
            superListener.propertyChange(evt);
        }
    }
}
