package org.tsho.dmc2.ui.basin;
import javax.swing.*;
import javax.swing.table.*;

import org.tsho.dmc2.core.chart.ColorSettings;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.factories.ButtonBarFactory;

import java.awt.event.*;
import java.awt.Color;

/**
 * Basins of attraction colors settings dialog
 * */
public class ColorSettingsDialog {
	private JDialog dialog;
	private JButton bttnCEmpty, bttnCInfinity;
	private JButton bttnOk, bttnSave, bttnReset, bttnCancel;
	private JButton bttnAdd, bttnEdit, bttnRemove;
	private JTable tabPairs;
	private ColorSettings initialColorSettings, colorSettings, colorAnswer;
	
	public static void main(String[] args) {
		DefaultListCellRenderer dflr = new DefaultListCellRenderer();
		dflr.getGraphics();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	
        }
		ColorSettingsDialog m = new ColorSettingsDialog(new ColorSettings(ColorSettingsDialog.class));
		m.show();
	}
	
	public ColorSettingsDialog(ColorSettings cs) {
		colorSettings = (ColorSettings) cs.clone();
		initialColorSettings = (ColorSettings) colorSettings.clone();
		colorAnswer = initialColorSettings;
	}
	
	public JPanel buildPanel() {
		initComponents();
        FormLayout layout = new FormLayout(
        		"6dlu, l:90dlu, 3dlu, l:max(30dlu;p):g, 6dlu", //cols
        		"6dlu, p, 3dlu, p, 6dlu, p, 3dlu, " +
        		"p, 3dlu, p, 3dlu, p, 3dlu, 30dlu, " + //table view
        		"3dlu, p, 3dlu"); //bottom buttons bar
        // Create a builder that assists in adding components to the container. 
        // Wrap the panel with a standardized border.
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        // Obtain a reusable constraints object to place components in the grid.
        CellConstraints cc = new CellConstraints();

        // Fill the grid with components;
        builder.addLabel("Empty box", cc.xy(2, 2));
        builder.add(bttnCEmpty, cc.xy(4, 2));
        builder.addLabel("Basin of infinity", cc.xy(2, 4));
        builder.add(bttnCInfinity, cc.xy(4, 4));
        builder.addSeparator("Attractor/Basin pairs", cc.xywh(2, 6, 3, 1, "f, b"));
        builder.add(new JScrollPane(tabPairs), cc.xywh(2, 8, 1, 7));
        builder.add(bttnAdd, cc.xy(4, 8, "l, b"));
        builder.add(bttnEdit, cc.xy(4, 10, "l, b"));
        builder.add(bttnRemove, cc.xy(4, 12, "l, b"));
        JPanel bttns = ButtonBarFactory.buildLeftAlignedBar(new JButton[]{
        		bttnOk, bttnSave, bttnReset, bttnCancel,
        		});
        builder.add(bttns, cc.xywh(2, 16, 3, 1, "c, b"));
		return builder.getPanel();
	}
	

	/**
	 * Creates form controls. Should be called only once
	 * */
	private void createComponents() {
    	bttnOk = new JButton("Ok");
    	bttnOk.setToolTipText("retain current settings");
    	bttnOk.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			colorAnswer = colorSettings;
    			dialog.dispose();
    		}
    	});
    	bttnSave = new JButton("Save");
    	bttnSave.setToolTipText("save current user settings for future reuse");
    	bttnSave.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			colorSettings.save();
    		}
    	});    	
    	bttnReset = new JButton("Reset");
    	bttnReset.setToolTipText("reset color settings to application defaults");
    	bttnReset.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			colorSettings.reset();
    			initialColorSettings = (ColorSettings) colorSettings.clone();
    			colorAnswer = initialColorSettings;
    			loadSettingsOnForm();
    			tabPairs.revalidate();
    		}
    	});    	
    	bttnCancel = new JButton("Cancel");
    	bttnCancel.setToolTipText("discards current settings");
    	bttnCancel.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			dialog.dispose();
    		}
    	});
    	
    	bttnCEmpty = new JButton("select color");
    	bttnCEmpty.setToolTipText("select empty box color");
    	bttnCEmpty.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			JButton source = (JButton) ae.getSource();
    			Color c = JColorChooser.showDialog(
                        source, "Empty box color",
                        source.getForeground());
    			if(c!=null) {
    				source.setForeground(c);
    				colorSettings.setEmpty(c.getRGB());
    			}
    		}
    	});
    	bttnCInfinity = new JButton("select color");
    	bttnCInfinity.setToolTipText("select basin of infinity color");
    	bttnCInfinity.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			JButton source = (JButton) ae.getSource();
    			Color c = JColorChooser.showDialog(
                        source, "Basin of infinity color",
                        source.getForeground());
    			if(c!=null) {
    				source.setForeground(c);
    				colorSettings.setInfinity(c.getRGB());
    			}
    		}
    	});
    	createTable();
	}
	
	private void createTable() {
        final TableModel pairsModel = new AbstractTableModel() {
            public int getColumnCount() { 
            	return 2; 
            }
            public int getRowCount() { 
            	return colorSettings.getPairsVector().size();
            }
            public Object getValueAt(int row, int col) {
            	return new Color(colorSettings.getPair(row)[col]);
            }
            public String getColumnName(int column) {
            	return new String[] {"Attractor", "Basin"} [column];
            }
            public Class getColumnClass(int c) {
            	return Color.class;
            }
            public boolean isCellEditable(int row, int col) {
            	return false;
            }
            public void setValueAt(Object aValue, int row, int column) {
            	colorSettings.getPair(row)[column] = ((Color) aValue).getRGB();
            }
         };

		
    	bttnAdd = new JButton("Add");
    	bttnAdd.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			Color ca = Color.black;
    			Color cb = Color.white;
    			cb = JColorChooser.showDialog(dialog, "Chose basin color", cb);
    			if(cb!=null)
    				ca = JColorChooser.showDialog(dialog, "Chose attractor color", cb);
    			if(ca!=null) {
    				int[] pair = new int[] {ca.getRGB(), cb.getRGB()};
    				colorSettings.addPair(pair);
    				tabPairs.revalidate();
    			}
    		}
    	});
    	bttnEdit = new JButton("Edit");
    	bttnEdit.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			int r = tabPairs.getSelectedRow(),
    				c = tabPairs.getSelectedColumn();
    			if(r>=0 && c>=0) {
	    			Color color = (Color) tabPairs.getValueAt(r, c); 
	    			String title = "Choose " + ((c==1)? "basin":"attractor")+ " color";
	    			color = JColorChooser.showDialog(dialog, title, color);
	    			if(color!=null)
	    				colorSettings.getPair(r)[c] = color.getRGB();
	    			tabPairs.revalidate();
    			}
    		}
    	});
    	
    	bttnRemove = new JButton("Remove");
    	bttnRemove.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent ae) {
    			int r = tabPairs.getSelectedRow();
    			if(r>=0) {
    				colorSettings.removePair(r);
    				tabPairs.revalidate();
    			}
    		}
    	});

        DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {
    	    public void setValue(Object value) {
    	        setBackground((Color) value);
    	    }
        };
        tabPairs = new JTable(pairsModel);
        tabPairs.getColumn("Basin").setCellRenderer(colorRenderer);
        tabPairs.getColumn("Attractor").setCellRenderer(colorRenderer);
	}
	
    /**
     * Creates, intializes and configures the UI components.
     */
    private void initComponents() {
    	createComponents();
    	loadSettingsOnForm();
    }
    
    /**
     * Transfers settings from 'colorSettings' to dialog controls
     * */
    private void loadSettingsOnForm() {
    	bttnCEmpty.setForeground(new Color(colorSettings.getEmpty()));    	
    	bttnCInfinity.setForeground(new Color(colorSettings.getInfinity()));
    	tabPairs.invalidate();
    }
	

    /**
     * Show modal dialog
     * */
	public void show() {
		dialog = new JDialog();
		dialog.setResizable(false);
		dialog.setModal(true);
        dialog.setTitle("Basins/attractors colors");
        dialog.setContentPane(buildPanel());
        dialog.pack();
        dialog.setVisible(true);
	}
	
	/**
	 * Get current color settings
	 * */
	public ColorSettings getColorSettings() {
		return colorAnswer;
	}

}
