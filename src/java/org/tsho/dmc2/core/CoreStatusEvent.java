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
package org.tsho.dmc2.core;

import java.util.EventObject;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CoreStatusEvent extends EventObject {

    public static final int NONE        = 0x0000;
    public static final int STARTED     = 0x0001;
    public static final int FINISHED    = 0x0002;
    public static final int INTERRUPTED = 0x0004;
    //public static final int ERROR       = 0x08;
    public static final int PERCENT     = 0x0010;
    public static final int COUNT       = 0x0020;
    public static final int STRING      = 0x0040;
    public static final int REDRAW      = 0x0080;
    public static final int REPAINT     = 0x0100;

    private int type;
    int percent;
    long count;
    private String statusString;

    /**
     * @param source
     */
//    public CoreStatusEvent(Object source, int type, int status) {
//        super(source);
//        this.type = type;
//        this.status = status;
//    }

    public CoreStatusEvent(Object source) {
        super(source);
    }

    /**
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * @param i
     */
    public void setType(int i) {
        type = i;
    }

    /**
     * @return
     */
    public String getStatusString() {
        return statusString;
    }

    /**
     * @param string
     */
    public void setStatusString(String string) {
        type = NONE;
        statusString = string;
    }

    /**
     * @return
     */
    public int getPercent() {
        return percent;
    }

    /**
     * @param i
     */
    public void setPercent(int i) {
        type = NONE;
        percent = i;
    }

    /**
     * @return
     */
    public long getCount() {
        return count;
    }

    /**
     * @param i
     */
    public void setCount(long i) {
        type = NONE;
        count = i;
    }
}
