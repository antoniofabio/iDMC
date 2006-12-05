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


public class UserActionInput extends Input {
    public static final
        UserActionInput start = new UserActionInput("start");
    public static final
        UserActionInput stop = new UserActionInput("stop");
    public static final
        UserActionInput clear = new UserActionInput("clear");
    public static final
        UserActionInput reset = new UserActionInput("reset");
    public static final
        UserActionInput close = new UserActionInput("close");
    public static final
        UserActionInput continua = new UserActionInput("continua");
    public static final
        UserActionInput redraw = new UserActionInput("redraw");
    public static final
        UserActionInput add = new UserActionInput("add");

    public UserActionInput(final String name) {
        super(name);
    }
}
