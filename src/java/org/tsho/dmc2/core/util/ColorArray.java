/*
 * Written by Alexei Grigoriev
 *
 */

package org.tsho.dmc2.core.util;

import java.awt.Color;
import org.jfree.chart.plot.DefaultDrawingSupplier;
/**
 *
 * @author  altair
 */
public class ColorArray {
    
    Color [] color;
    DefaultDrawingSupplier supplier=new DefaultDrawingSupplier();
    
    /** Creates a new instance of Class */
    public ColorArray(int n) {
        
        
        color=new Color[n];
        for (int i=0;i<n;i++){
            if (i<35)
                color[i]=(Color) supplier.DEFAULT_PAINT_SEQUENCE[i];
            else
                color[i]=(Color) supplier.DEFAULT_PAINT_SEQUENCE[34];
        }
        
        /*
        int red=150;
        int green=100;
        int blue=150;
        Color c=new Color(red,green,blue);
        int m=(int) Math.round(Math.exp(Math.log(n)/3)+1);
        int step=(int) 100/m;
        color =new Color[n];
        int count=0;
        for (int i=0; i<m; i++){
            for (int j=0;j<m;j++){
                for (int k=0;k<m;k++){
                    c=new Color(red,green,blue);
                    if (count<n) {
                        color[count]=c;
                    }
                    count++;
                    red=red+step;
                }
                red=100;
                blue=blue+step;
            }
            blue=100;
            green=green+step;
        }
         */
    }
    
    public Color getColor(int i){
        return color[i];
    }
    
}
