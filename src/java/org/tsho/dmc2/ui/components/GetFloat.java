/***************************************************************************
 * DMC, Dynamical Model Cruncher, simulates and analyzes low-dimensional
 * dynamical systems
 * Copyright (C) 2002   Alfredo Medio and Giampaolo Gallo
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * For a copy of the GNU General Public, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 
 * USA
 *
 * Contact:  Alfredo Medio   Medio@unive.it
 *
 * adapted by Daniele Pizzoni <auouo@tin.it>
 *
 ***************************************************************************/
package org.tsho.dmc2.ui.components;
import java.awt.Color;
import java.io.Serializable;

import javax.swing.JTextField;
import java.awt.event.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jfree.data.Range;
import org.tsho.dmc2.ui.InvalidData;


/**
 *  @author Giampaolo Gallo
 *  @author Daniele Pizzoni <auouo@tin.it>
 */
public final class GetFloat extends JTextField implements Serializable {
    protected double value;
    
    protected boolean valid;
    protected boolean outOfRange;
    protected boolean ignoreValid;
    
    protected Range validRange;

    protected String name = "field";

    public static final String INVALID_FIELD_MESSAGE = "empty or not valid";
    public static final String INVALID_RANGE_FIELD_MESSAGE = "out of range";

    public GetFloat() {
        super();
        init();
    }


    public GetFloat(int len) {
        super(len);
        init();
    }

    public GetFloat(String name, int len, Range range) {
        super(len);
        
        this.name = name;
        validRange = range;
        init();
    }

    public GetFloat(String name, int len) {
        super(len);
        
        this.name = name;
        init();
    }

    public GetFloat(int len, double initialValue) {
        super(len);
        init();
        try {
            this.getDocument().insertString(
                0,
                new String(Double.toString(initialValue)),
                null);
        }
        catch (BadLocationException e) {}
    }

    private void init() {
        valid = false;
        ignoreValid = false;
        outOfRange = false;

        this.getDocument().addDocumentListener(new MyDocumentListener());
        
        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                selectAll();
            }
        });
        //super.setBorder(BorderFactory.createLineBorder(null));        
    }

    public void syncText() {
        if (valid)
            setText(Double.toString(value));
        else
            setText("");
    }

    public void syncValue() {
        String s = getText();
        try {
            value = Double.valueOf(s).doubleValue();
            valid = true;
        }
        catch (NumberFormatException e) {
            value = 0.0;
            valid = false;
        }
    }
    
    public void setValue(double value) {
        this.value = value;
        setText(Double.toString(value)); 
    }

    public double getValue() throws InvalidData {
        if (!valid && !ignoreValid) {
            if (outOfRange) {
                String message = name
                       + ": "
                       + INVALID_RANGE_FIELD_MESSAGE 
                       + " (" 
                       + validRange.getLowerBound() 
                       + " < "
                       +  validRange.getUpperBound()
                       + ")";
                throw new InvalidData(message);
            }
            else {
                InvalidData e=new InvalidData(name+": "+INVALID_FIELD_MESSAGE);
		throw e;
            }
        }

        return value;
    }

    public boolean isValid() {
        return valid;
    }

    private class MyDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }
        public void changedUpdate(DocumentEvent e) {}

        private void update(DocumentEvent e) {
            Document d = (Document)e.getDocument();
            String s = null;
            try {
                s = d.getText(0, d.getLength());
            }
            catch (BadLocationException e1) {}

            if (s != null && s.length() > 0) {
                try {
                    value = Double.valueOf(s).doubleValue();
                    valid = true;
                }
                catch (NumberFormatException e2) {
                    value = 0.0;
                    valid = false;
                    outOfRange = false;
                }

                if (valid != false
                    && validRange != null && !validRange.contains(value)) {

                    value = 0.0;
                    valid = false;
                    outOfRange = true;
                }

                setForeground(valid ? Color.black : Color.red);
            }
            if (s.length() == 0) {
                value = 0.0;
                valid = false;
            }
        }
    }
	/**
	 * @return
	 */
	public boolean isIgnoreValid() {
		return ignoreValid;
	}

	/**
	 * @param b
	 */
	public void setIgnoreValid(boolean b) {
		ignoreValid = b;
	}
}
