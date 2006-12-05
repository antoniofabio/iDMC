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

import org.tsho.dmc2.core.VariableDoubles;
import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;

public class LuaModel extends org.tsho.jidmclib.Model implements Model {
	public org.tsho.jidmclib.Model model=null;
    static final int TYPE_CONT = 1;
    static final int TYPE_DISC = 2;
    
    protected final String name;
    
    LuaModel(final String name, final String buffer)
    throws ModelException {
    	super(buffer, buffer.length());
        this.name = name;
        model = this;
    }
    
    public int getType() {
    	int ans = super.getType();
    	if(ans=='D')
    		return TYPE_DISC;
    	else if(ans == 'C')
    		return TYPE_CONT;
    	else return 0;
    }
    
    public String[] getVarNames() {
    	String ans[] = new String[getVar_len()];
    	for(int i=0; i<ans.length; i++)
    		ans[i] = getVar(i);
    	return ans;
    }
    
    public String[] getParNames() {
    	String ans[] = new String[getPar_len()];
    	for(int i=0; i<ans.length; i++)
    		ans[i] = getPar(i);
    	return ans;
    }
        
    public VariableDoubles getVariables() {
        return new VariableDoubles(getVarNames());
    }
    
    public VariableDoubles getParameters() {
        return new VariableDoubles(getParNames());
    }
    
    public String getModelText() {
    	String buffer=getBuffer();
    	int pos=buffer.indexOf("--@@",0);
        if (pos>=0)
            return buffer.substring(pos+5,buffer.length());
        else
            return buffer;
    }
    
    public String getDescription() {
    	return getDesc();
    }
    
    public int getNVar() {
    	return getVar_len();
    }
    
    public int getNPar() {
    	return getPar_len();
    }
    
    public boolean hasJacobian() {
    	return getHas_jacobian()!=0;
    }

    public boolean hasInverse() {
    	return getHas_inverse()!=0;
    }
}
