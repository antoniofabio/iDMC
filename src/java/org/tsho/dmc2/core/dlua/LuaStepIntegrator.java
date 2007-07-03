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

import org.tsho.jidmclib.CTrajectory;
import org.tsho.jidmclib.Model;
import org.tsho.jidmclib.SWIGTYPE_p_double;
import org.tsho.jidmclib.SWIGTYPE_p_gsl_odeiv_step_type;
import org.tsho.jidmclib.idmc;

class LuaStepIntegrator {
	static final SWIGTYPE_p_gsl_odeiv_step_type[] step_funs = 
		new SWIGTYPE_p_gsl_odeiv_step_type[] {
		idmc.getGsl_odeiv_step_rk2(),
		idmc.getGsl_odeiv_step_gear1(),
		idmc.getGsl_odeiv_step_gear2(),
		idmc.getGsl_odeiv_step_rk2(),
		idmc.getGsl_odeiv_step_rk2imp(),
		idmc.getGsl_odeiv_step_rk4(),
		idmc.getGsl_odeiv_step_rk4imp(),
		idmc.getGsl_odeiv_step_rk8pd(),
		idmc.getGsl_odeiv_step_rkck(),
		idmc.getGsl_odeiv_step_rkf45(),
		null
	};
	static final String[] step_names = new String[] {
		"rk2",
		"gear1",
		"gear2",
		"rk2",
		"rk2imp",
		"rk4",
		"rk4imp",
		"rk8pd",
		"rkck",
		"rkf45",
		null
	};
	static final String[] step_descs = step_names; 
    CTrajectory ctraj=null;
    LuaModel model = null;
    double point[];
    double parameters[];
    int npars, nvars;
    
    LuaStepIntegrator(LuaModel m, double[] parValues, 
    		double[] varValues, double step_size, 
    		int step_function_code) {
    	ctraj = new CTrajectory(m, parValues, varValues, step_size,
    			step_funs[step_function_code]);
    	model=m;
    	npars=m.getPar_len();
    	nvars=m.getVar_len();
    	parameters= new double[npars];
    	point = new double[nvars];
    }
    
    public static String stepFunctionName(int id) {
    	return step_names[id];
    }

    public static String stepFunctionDescription(int id) {
    	return step_descs[id];
    }
    
    public void step() {
        ctraj.dostep();
    }
    
    public static double[] p_doubleToArray(
    		SWIGTYPE_p_double pin, int size) {
    	double ans[] = new double[size];
    	p_doubleToArray(pin, ans);
    	return ans;
    } 
    
    public static void p_doubleToArray(
    		SWIGTYPE_p_double pin, double ans[], int size) {
    	for(int i=0; i<size; i++)
    		ans[i] = idmc.doubleArray_getitem(pin,i);
    }
    
    public static void p_doubleToArray(
    		SWIGTYPE_p_double pin, double ans[]) {
    	p_doubleToArray(pin, ans, ans.length);
    }
    
    public double[] getParameters() {
    	return p_doubleToArray(ctraj.getPar(), npars);
    }

    public double[] getPoint() {
    	return p_doubleToArray(ctraj.getVar(), nvars); 
    }
    
    public void getPoint(double ans[]) {
    	p_doubleToArray(ctraj.getVar(), ans, nvars);
    }
    
    public Model getModel() {
    	return model;
    }
    
    public void setStep(double val, int step_code){
    	ctraj = new CTrajectory(ctraj.getModel(), 
    			getParameters(),
    			getPoint(),
    			val,
    			step_funs[step_code]);
    }
    
    public void setParameters(double pars[]) {
    	ctraj = new CTrajectory(ctraj.getModel(), 
    			pars,
    			getPoint(),
    			ctraj.getStep_size(),
    			ctraj.getStep_function_code());
    }
    
    public void setInitialValue(double vars[]) {
    	ctraj = new CTrajectory(getModel(), 
    			getParameters(),
    			vars,
    			ctraj.getStep_size(),
    			ctraj.getStep_function_code());
    }

}
