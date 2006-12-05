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
package org.tsho.dmc2.core.dlua;

import org.tsho.dmc2.core.model.DifferentiableMap;
import org.tsho.dmc2.core.model.ModelException;

public class LuaDifferentiableMap
        extends LuaSimpleMap implements DifferentiableMap {

    public LuaDifferentiableMap(final String name, final String buffer) {
        super(name, buffer);
    }

    public void evaluateJacobian(
            final double[] par, final double[] var, final double[] result)
            throws ModelException {
        try {
            int status = model.Jf(par, var, result);
            if(status<0)
            	throw new LuaErrorException("Error evaluating model jacobian");
        }
        catch (LuaErrorException e) {
            throw new ModelException(e.getMessage(), e);
        }
    }
}
