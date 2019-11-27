package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

class JTableButtonRenderer extends JButton implements TableCellRenderer {
	private static final long serialVersionUID = 6493027142302342323L;

	public JTableButtonRenderer() {
		setOpaque(false);
		setForeground(Color.BLACK);
        setBackground(Color.LIGHT_GRAY);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setHorizontalAlignment( JLabel.CENTER );
	}

	@Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
		
		String pluginName = (String)table.getValueAt(row, 0);
		
        setForeground(Color.BLACK);
        setBackground(Color.LIGHT_GRAY);
        setText("Install " + pluginName);
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		});
        
		return this;
    }
}
