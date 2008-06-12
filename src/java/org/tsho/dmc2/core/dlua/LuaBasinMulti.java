/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tsho.dmc2.core.dlua;

import org.tsho.jidmclib.*;

/**
 *
 * @author antonio
 */
public class LuaBasinMulti {
    private BasinMulti bs;
    
    public LuaBasinMulti(LuaModel m, double[] par,
            double xmin, double xmax, int xres,
            double ymin, double ymax, int yres,
            double eps, int limit, int iter,
            int ntries, int xvar, int yvar,
            double[] var) {
        bs = new BasinMulti((org.tsho.jidmclib.Model) m,
			as_C_array(par), xmin, xmax, xres, ymin, ymax, yres,
			eps, limit, iter, ntries, xvar, yvar,
			as_C_array(var));
    }
    
    public int step() {
        return bs.step();
    }
    
    public boolean finished() {
        return (bs.finished()!=0);
    }
    
    public int findNextAttractor() {
        return bs.find_next_attractor();
    }
    
    public void findAttractors() {
        for(int i=0; i<bs.getNtries(); i++) {
            findNextAttractor();
        }
    }
    
    public int getNAttractors() {
        return bs.getNAttractors();
    }

    static SWIGTYPE_p_double as_C_array(double[] x) {
            SWIGTYPE_p_double ans = idmc.new_doubleArray(x.length);
            for(int i=0; i<x.length; i++) {
                    idmc.doubleArray_setitem(ans, i, x[i]);
            }
            return ans;
    }

    static double[] as_Java_array(SWIGTYPE_p_double x, int size) {
            double ans[] = new double[size];
            for(int i=0; i<ans.length; i++) {
                    ans[i] = idmc.doubleArray_getitem(x, i);
            }
            return ans;
    }
}
