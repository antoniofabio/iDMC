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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jfree.data.Range;
import org.tsho.dmc2.ui.InvalidData;


public class GetInt extends JTextField {
    protected int value;
    
    protected boolean valid;
    protected boolean outOfRange;
    protected boolean ignoreValid;
    
    protected String name = "field";
    protected Range validRange;
    
    public static final String INVALID_FIELD_MESSAGE = "empty or not valid";
    public static final String INVALID_RANGE_FIELD_MESSAGE = "out of range";

	public GetInt(int len) {
		super(len);
		valid = false;
        outOfRange = false;
        ignoreValid = false;
        

		this.getDocument().addDocumentListener(new MyDocumentListener());
        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                selectAll();
            }
        });		
	}

    public GetInt(String name, int len, Range range) {
        this(len);
        
        this.name = name;
        validRange = range;
    }

    public GetInt(String name, int len) {
        this(len);
        
        this.name = name;

    }

    public void syncText() {
        if (valid)
            setText(Integer.toString(value));
        else
            setText("");
    }

    public void syncValue() {
        String s = getText();
        try {
            value = Integer.valueOf(s).intValue();
            valid = true;
        } catch (NumberFormatException e) {
            value = 0;
            valid = false;
        }
    }

    public int getValue() throws InvalidData {
        if (!valid && !ignoreValid) {
            if (outOfRange) {
                String message = name
                       + ": "
                       + INVALID_RANGE_FIELD_MESSAGE 
                       + " ( " 
                       + (int)validRange.getLowerBound() 
                       + " ... "
                       + (int)validRange.getUpperBound()
                       + " )";
                throw new InvalidData(message);
            }
            else {
                throw new InvalidData(name + ": " + INVALID_FIELD_MESSAGE);
            }
        }

        return value;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValue(int value) {
        this.value = value;
        setText(Integer.toString(value)); 
    }

    private class MyDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        public void removeUpdate(DocumentEvent e) {
           update(e);
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void update(DocumentEvent e) {
            Document d = (Document) e.getDocument();
            String s = null;
            try {
                s = d.getText(0,d.getLength());
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
            if (s != null && s.length() > 0) {
                try {
                    value = Integer.valueOf(s).intValue();
                    valid = true;
                } catch(NumberFormatException e2) {
                    value = 0;
                    valid = false;
                    outOfRange = false;
                }

                if (valid != false
                    && validRange != null && !validRange.contains(value)) {

                    value = 0;
                    valid = false;
                    outOfRange = true;
                }

                setForeground(valid ? Color.black : Color.red);
            }
            if (s.length() == 0) {
                value = 0;
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

    public String getName() {
        return name;
    }

}

