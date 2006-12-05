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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.tsho.dmc2.AlgorithmFailedException;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.jidmclib.idmc;

/**
 * @author tsho
 *
 */
final class LuaCycles {

    private LuaCycles() {
        super();
    }
    
    /**
     * Tries to find the cycles of period power of the model using
     * the gsl_multiroot_fsolver_hybrids of the GNU gsl library.
     * ("...a modified version of Powell's Hybrid method as
     * implemented in the HYBRJ algorithm in MINPACK")
     *
     * @param model the model
     * @param parameters parameters for the model
     * @param power cycle order
     * @param startPoint starting approximation of cycle
     * @param epsilon approximation needed
     * @param maxIterations max number of steps
	 * @param eigvals eigenvalues modula
     * @return
     *
     * @throws LuaErrorException on general error (bad model, etc.)
     * @throws AlgorithmFailedException if the algorithm cannot converge
     *          after the steps specified
     */
    static double[] findCycles(
            final Model model, final VariableDoubles parameters,
            final int power, final VariableDoubles startPoint,
			final double epsilon, final int maxIterations, double []eigvals) {
        int found;
        double[] result = new double[model.getNVar()];

        found = idmc.idmc_cycles_find((org.tsho.jidmclib.Model) model,
                VariableDoubles.toArray(parameters),
                VariableDoubles.toArray(startPoint),
                power,
                epsilon,
                maxIterations,
                result,
				eigvals);

        if (found!=0) {
            throw new AlgorithmFailedException();
        }

        return result;
    }

    public static void main(final String[] args) {
        File f = new File("models/henon.lua");
        Model model = null;
        try {
            model = Lua.newModel(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ModelException e) {
            e.printStackTrace();
        }

        // henon2
        VariableDoubles par = model.getParameters();
        par.put("a", 1.3);
        par.put("b", 0.4);

        VariableDoubles init = model.getVariables();
        init.put("x", 0.00001);
        init.put("y", 0.0000001);

        double[] result = new double[2];
		double[] eigvals = new double[2];

        result = findCycles(model, par, 1, init, 1e-50, 100000, eigvals);

        for (int i = 0; i < model.getNVar(); i++) {
            System.out.println("cicle[" + i + "] = " + result[i]);
        }
    }
}
