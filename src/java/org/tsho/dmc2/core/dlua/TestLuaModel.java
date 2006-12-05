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
import java.io.IOException;

import junit.framework.TestCase;

import org.tsho.dmc2.core.model.Model;
import org.tsho.dmc2.core.model.ModelException;


public class TestLuaModel extends TestCase {

    public TestLuaModel(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
    }
    
    public void setUp() throws Exception {
    	super.setUp();
        String nativeLibName = org.tsho.dmc2.DmcDue.getNativeLibName();
        System.out.println("native lib name: " + nativeLibName);
        System.load(nativeLibName);
    }

    public void testModel() {
        Model model=null;
        File file = new File("models/simple.lua");
        try {
        	model = Lua.newModel(file);
        }
        catch (ModelException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
        	e.printStackTrace();
        	throw e;
        }
        System.out.println("name: " + model.getName());
        System.out.println("number of parameters: "+ model.getNPar());
        System.out.println("number of variables: " + model.getNVar());
        System.out.println("description: " + model.getDescription());
    }
    
    public void testLuaModel() {
        LuaModel model=null;
        File file = new File("models/simple.lua");
        try {
        	model = (LuaModel) Lua.newModel(file);
        }
        catch (ModelException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
        	e.printStackTrace();
        	throw e;
        }
        assertFalse(model.getHas_inverse()!=0);
        double [] result = new double[2];
        model.f(new double[] {1}, new double[] {0.5, 0.5}, result);
        assertTrue(0.25==result[0]);
        assertTrue(0.25==result[1]);
    }

}
