/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tsho.dmc2.core.dlua;

import java.util.Vector;
import org.tsho.jidmclib.*;

/**
 *
 * @author antonio
 */
public class LuaBasinMulti {
    private BasinMulti bs;
    attr_lst cattr;
    int dim;
    
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
        dim = var.length;
    }
    
    public int step() {
        return bs.step();
    }
    
    public boolean finished() {
        return (bs.finished()!=0);
    }
    
    public int getIndex() {
        return bs.getCurrId();
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
    
    public Vector getAttractors() {
        Vector ans = new Vector();
        cattr = bs.getAttr_head();
        attractor_pt tmp_apt;
        SWIGTYPE_p_double tmp_dpt;
        Vector tmp_vec;
        for(int i=0; i<idmc.idmc_attractor_list_length(cattr); i++) {
            tmp_vec = new Vector();
            tmp_apt = cattr.getHd();
            for(int j=0; j<idmc.idmc_attractor_length(cattr); j++) {
                tmp_dpt = tmp_apt.getX();
                tmp_vec.add(as_Java_array(tmp_dpt, dim));
                tmp_apt = tmp_apt.getNext();
            }
            ans.add(tmp_vec);
        }
        return ans;
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
