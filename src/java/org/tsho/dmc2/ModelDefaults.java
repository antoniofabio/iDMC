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
package org.tsho.dmc2;

/**
 * @author tsho
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface ModelDefaults {
    
    // TODO range NOT bounds
    
    /* general */
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String DESCRIPTION_KEY = "description";
    public static final String NAME_KEY = "name";
    public static final String TRANSIENTS_KEY = "transients";
    public static final String ITERATIONS_KEY = "iterations";
    public static final String PLOT_TYPE_KEY = "plot_type";

    /* "sections */
    public static final String SCATTER_SECTION = "scatter";
    public static final String BIFURCATION_SECTION = "bifurcation";
    public static final String MANIFOLDS_SECTION = "manifolds";

    /* scatter specific */
    public static final String SCATTER_PLOT_TYPE_NORMAL_VALUE = "normal";
    public static final String SCATTER_PLOT_TYPE_TIME_VALUE = "time";
    public static final String SCATTER_PLOT_TYPE_SHIFTED_VALUE = "shifted";

    public static final String SCATTER_SHIFT_VALUE_KEY = "shift_value";
    
    public static final String SCATTER_AUTOBOUNDS_ITERATIONS_KEY = "autobounds_iterations";

    public static final String SCATTER_BOUNDS_KEY = "bounds";
    public static final String SCATTER_BOUNDS_AUTO_VALUE = "auto";
    public static final String SCATTER_BOUNDS_MANUAL_VALUE = "manual";
    public static final String SCATTER_AUTO_BOUNDS_HMIN_KEY = "bounds_hmin";
    public static final String SCATTER_AUTO_BOUNDS_HMAX_KEY = "bounds_hmax";
    public static final String SCATTER_AUTO_BOUNDS_VMIN_KEY = "bounds_vmin";
    public static final String SCATTER_AUTO_BOUNDS_VMAX_KEY = "bounds_vmax";


    /* bifurcation specific */
    public static final String BIFUR_PLOT_TYPE_SINGLE_VALUE = "single";
    public static final String BIFUR_PLOT_TYPE_DOUBLE_VALUE = "double";

    public static final String BIFUR_HOR_PARAMETER = "horizontal_parameter";
    public static final String BIFUR_HPAR_MIN_KEY = "hor_parameter_min";
    public static final String BIFUR_HPAR_MAX_KEY = "hor_parameter_max";
    
    public static final String BIFUR_VER_PARAMETER = "vertical_parameter";
    public static final String BIFUR_VPAR_MIN_KEY = "ver_parameter_min";
    public static final String BIFUR_VPAR_MAX_KEY = "ver_parameter_max";

    public static final String BIFUR_VER_VARIABLE = "vertical_variable";
    public static final String BIFUR_VAR_MIN_KEY = "ver_variable_min";
    public static final String BIFUR_VAR_MAX_KEY = "ver_variable_max";

    public static final String BIFUR_EPSILON = "epsilon";
    public static final String BIFUR_INFINITY = "infinity";
    public static final String BIFUR_PERIOD = "period";

    public static final String BIFUR_BOUNDS_FIXED_INITIAL_KEY = "fixed_initial_point";



    /* kludge keys */
    public static final String TRANSPARENCY_KEY = "transparency";
}
