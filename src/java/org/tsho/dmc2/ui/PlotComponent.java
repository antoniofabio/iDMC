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
package org.tsho.dmc2.ui;

import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.tsho.dmc2.core.util.DataObject;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface PlotComponent {

    JPanel getPanel();

    JMenu getOptionsMenu();
    JMenu getCommandMenu();
    JMenu getPlotMenu();
    JMenu getSamplesMenu();

    JToolBar getStatusBar();
    JToolBar getToolBar();

    void saveImageAs() throws IOException;
    void saveDataAs() throws IOException;
    DataObject getDataobject();

    void dispose();
}
