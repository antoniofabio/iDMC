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

/**
 *  Exception thrown when the data available on the UI is not valid.
 *  Such as in a needed field left blank or a negative variation count, etc.
 *  
 *  @author Daniele Pizzoni <auouo@tin.it>
 */
public class InvalidData extends Exception {

	/**
	 * 
	 */
	public InvalidData() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
     * @param message
     */
    public InvalidData(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidData(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public InvalidData(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
