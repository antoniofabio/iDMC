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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.tsho.jidmclib.idmc;
import org.tsho.dmc2.core.MapStepper;
import org.tsho.dmc2.core.ODEStepper;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.VariableItems;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>. 
 * Modified by Antonio Di Narzo <antonio.dinarzo@studio.unibo.it>
 */
public final class Lua {
        /**
     *idmclib common return codes
     */
    static final int OK = idmc.IDMC_OK;
    static final int EMEM = idmc.IDMC_EMEM;
    static final int ELUASYNTAX = idmc.IDMC_ELUASYNTAX;
    static final int ERUN = idmc.IDMC_ERUN;
    static final int EMODEL = idmc.IDMC_EMODEL;
    static final int EERROR = idmc.IDMC_EERROR;
    static final int EMATH = idmc.IDMC_EMATH;
    static final int EINT = idmc.IDMC_EINT;

    private Lua() {
    }

    /**
     * Checks for the presence of the key in the defaults section
     * number index. If the table is no present returns null; if the field
     * is present returns its value; if not returns ""
     */
    public static String checkDefaults(
            Model model, String section, int index, String key) {
        return "";
    }

    /**
     * Loads the defaults section number index.
     * If present returns a VariableItems key/value (string/string);
     * if not returns null
     */
    public static VariableItems loadDefaults(
            Model model, String section, int index) {
        return null;
    }

    /**
     *This is the model object factory. Depending on model features, specific 
     * LuaModel subclasses are instantiated
     */
    public static Model newModel(final File f)
            throws IOException, ModelException { // FileNotFoundException
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line;
        StringBuffer tmp = new StringBuffer();
        while ((line = br.readLine()) != null) {
            tmp.append(line);
            tmp.append("\n");
        }
        String buffer = tmp.toString();
        Model model = new LuaModel(f.getName(), buffer);
        boolean hasInverse, hasJacobian;
        hasInverse = model.hasInverse();
        hasJacobian = model.hasJacobian();
        int type = model.getType();

        if (type == LuaModel.TYPE_DISC) {
            if (hasInverse && hasJacobian) {
                return new LuaInvertibleDifferentiableMap(f.getName(), buffer);
            }
            else if (hasInverse) {
                return new LuaInvertibleMap(f.getName(), buffer);
            }
            else if (hasJacobian) {
                return new LuaDifferentiableMap(f.getName(), buffer);
            }
            else {
                return new LuaSimpleMap(f.getName(), buffer);
            }
        }
        else if (type == LuaModel.TYPE_CONT) {
            return new LuaODE(f.getName(), buffer);
        }
        else {
            throw new Error("Invalid model type.");
        }
    }

    public static MapStepper newIterator(Model model, 
    		double pars[], double vars[]) {
        return new LuaIterator((LuaSimpleMap) model, pars, vars);
    }

    public static ODEStepper newODEStepper(Model model, double[] parValues, 
    		double[] varValues, double step_size, 
    		int step_function_code) {
        return new LuaODEStepper((LuaModel) model, 
        		parValues, varValues, step_size, 
        		step_function_code);
    }

    public static String getStepFunctionName(int index) {
        return LuaStepIntegrator.stepFunctionName(index);
    }

    public static String getStepFunctionDescription(int index) {
        return LuaStepIntegrator.stepFunctionDescription(index);
    }

    public static double[] evaluateLyapunovExponents(
            final Model model, final VariableDoubles parameters,
            final VariableDoubles initialPoint, final int iterations)
            throws ModelException {
        double result[] = new double[model.getNVar()];
        int ans = idmc.idmc_lexp((org.tsho.jidmclib.Model) model,
                VariableDoubles.toArray(parameters), 
                VariableDoubles.toArray(initialPoint), 
                result, iterations);
        if(ans!=OK)
            throw new ModelException("error computing lyapunov exponents");
        return result;
    }
    
    public static double[] evaluateLyapunovExponentsODE(
            Model model, VariableDoubles parameters,
            VariableDoubles initialPoint, double time, double step)
            throws ModelException {        
        double result[] = new double[model.getNVar()];
        int ans = idmc.idmc_lexp_ode((org.tsho.jidmclib.Model) model, 
                VariableDoubles.toArray(parameters),
                VariableDoubles.toArray(initialPoint),
                result, time, step);
        if(ans!=OK)
            throw new ModelException("error in computing lyapunov exponents");
        return result;
    }

    /*
     * @throws ModelException
     * @throws AlgorithmFailedException
     */
    public static double[] findCycles(
            final Model model, final VariableDoubles parameters,
            final int power, final VariableDoubles startPoint,
            final double epsilon, final int maxIterations, double []eigvals) {

        double[] result;

        result = LuaCycles.findCycles(
                            model, parameters,
                            power, startPoint, epsilon, maxIterations, eigvals);
        return result;
    }
}
