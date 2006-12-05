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

import org.tsho.dmc2.core.InternalErrorException;
import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;
import org.tsho.jidmclib.idmc;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class LuaLyapExp {
    
    //the private fields below serve to pass/collect data for Lyapunov vs. time plot 
    private double[] t;//array with one entry
    private double[] result;
    private double[] y;
    private double[] Q;
    private double[] parameters;
    private Model model;
    
    public LuaLyapExp(Model model, VariableDoubles parameters, VariableDoubles initialPoint){
        this.model=model;
        int dim=model.getNVar();
        t=new double[1];
        t[0]=0;
        result=new double[dim];
        for (int i=0;i<dim;i++) result[i]=0;
        this.parameters=VariableDoubles.toArray(parameters);
        y=VariableDoubles.toArray(initialPoint);
        Q=new double[dim*dim];
        for (int i=0;i<dim;i++)
            for (int j=0;j<dim;j++){
                if (i==j) 
                    Q[i*dim+j]=1;
                else
                    Q[i*dim+j]=0;
            }
    }
    
    private static void evaluateODEStep(org.tsho.jidmclib.Model m, double[] parameters, double[] result,
            double[] t, double[] y, double[] Q, double step) throws LuaErrorException {
    	idmc.idmc_lexp_ode_step(m, parameters, result, Q, y, t, step);
    }
   
    
    private static void evaluateODE(org.tsho.jidmclib.Model m, double[] parameters,
        double[] startPoint, double[] result, double time,double step) throws LuaErrorException {
    	idmc.idmc_lexp_ode(m, parameters, startPoint, result, time, step);
    }

    private static void evaluate(org.tsho.jidmclib.Model m,
            final double[] parameters, final double[] startPoint,
            final double[] result, final int iterations)
            throws LuaErrorException {
    	idmc.idmc_lexp(m,parameters, startPoint, result, iterations);
    }

    static double[] evaluate(
            final Model model, final VariableDoubles parameters,
            final VariableDoubles initialPoint, final int iterations)
            throws ModelException {
        double[] result = new double[model.getNVar()];
        try {
        	evaluate((LuaModel)model, VariableDoubles.toArray(parameters),
        			VariableDoubles.toArray(initialPoint), result,
                    iterations);
        }
        // ignore errors
        catch (LuaErrorException e) {
            // throw new ModelException(e);
        }
        return result;
    }
    
    static double[] evaluateODE(
            Model model, VariableDoubles parameters,
            VariableDoubles initialPoint, double time, double step)
            throws ModelException {

        double[] result = new double[model.getNVar()];

        try {
            evaluateODE(
            		(LuaModel)model, VariableDoubles.toArray(parameters),
                    VariableDoubles.toArray(initialPoint), result,
                    time,step);
        }
        // ignore errors
        catch (LuaErrorException e) {
            // throw new ModelException(e);
        }

        return result;
    }
    
    //one has first to construct an instance of LuaLyapExp, supplying the model, 
    //the initial point and the parameters (as VariableDoubles-s).
    public double[] evaluateODEStep(double step)
            throws ModelException {

        if (!(model instanceof LuaModel)) {
            throw new InternalErrorException("Model is not a LuaModel.");
        }
      
        try {
            evaluateODEStep((LuaModel)model, parameters,
                    result,
                    t,y,Q,step);
        }
        // ignore errors
        catch (LuaErrorException e) {
            // throw new ModelException(e);
        }
        
        return result;
    }
    
    public double getTime(){
        return t[0];
    }
    
}
