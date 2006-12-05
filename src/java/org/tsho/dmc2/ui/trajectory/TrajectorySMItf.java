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
package org.tsho.dmc2.ui.trajectory;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JSlider;

import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.managers.AbstractManager;
import org.tsho.dmc2.ui.components.DmcAction;
import org.tsho.dmc2.ui.*;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface TrajectorySMItf {
    Action getStartAction();
    Action getStopAction();

    DmcAction getContinueAction();
    DmcAction getRedrawAction();
    DmcAction getResetAction();

    JMenuItem getNormalPlotMenuItem();
    JMenuItem getTimePlotMenuItem();
//    JMenuItem getShiftedPlotMenuItem();

    AbstractControlForm getControlForm();
    JSlider getSlider();
    Model getModel();

    AbstractManager getManager();
    void showInvalidDataDialog(String message);
}
