package tabbedpaneutils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import variousutils.HandleString;

public class TabUtils {
	
	public static final int TAB_ICON_ORDER = 0;
	public static final int TAB_TITLE_ORDER = 1;
	public static final int TAB_BUTTON_CLOSE_ORDER = 2;
	
	public static final int MAX_TAB_LABELENGHT = 120;
	
	/**
	 * Tab style = title + icon + tooltip + close button (action listener correlated)
	 * @param title
	 * @param tooltip
	 * @param iconPath
	 * @return
	 */
	public static JPanel getCustomClosableTab(String title, String tooltip, String iconPath, ActionListener tabCloseListener) {
		JPanel pnlTab = new JPanel();
		pnlTab.setOpaque(false);
		pnlTab.setToolTipText(tooltip);

		// tab title
		JLabel lblTitle = new JLabel();
		lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblTitle.setText(HandleString.fitToMaxLentgh(title, MAX_TAB_LABELENGHT));
		
		// image label
		JLabel imageLbl = new JLabel();
		imageLbl.setIcon(new ImageIcon(iconPath));
		imageLbl.setPreferredSize(new Dimension(15, 20));
		imageLbl.setMaximumSize(new Dimension(25, 20));
		
		pnlTab.setLayout(new GridBagLayout());
//		pnlTab.setLayout(new MigLayout("", "[16.00px][25.00px:35.00px,grow][15.00px]", "[16.00px,grow]"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 5;
		gbc.ipady = 0;
		gbc.weightx = 2;

		// add elements to pnlTab : order must be same as "int TAB_XXX_ORDER" defined at start of this class
		pnlTab.add(imageLbl, gbc); // 1 - ICON
		gbc.gridx++;
		gbc.ipadx = 4;
		gbc.weightx = 4;
		pnlTab.add(lblTitle, gbc); // 2 - TEXT TITLE
		gbc.gridx++;
		gbc.ipadx = 9;
		gbc.weightx = 3;
		
		// button close
		JButton btnClose = new JButton();
		btnClose = applyStyleToTabCloseBtn(btnClose);
		Dimension btnDimension = new Dimension(16, 22) ;
		btnClose.setFont(new Font("SansSerif", Font.PLAIN, 10));
		btnClose.setMinimumSize(btnDimension);
		btnClose.setPreferredSize(btnDimension);
		btnClose.setMaximumSize(btnDimension);
		btnClose.setVerticalTextPosition(SwingConstants.TOP);
		btnClose.setHorizontalTextPosition(SwingConstants.CENTER);
		btnClose.setText("x");
		btnClose.addActionListener(tabCloseListener);

		pnlTab.add(btnClose, gbc); // 3 - CLOSE BUTTON
		

		return pnlTab;
	}
	
	/**
	 * Tab style = title + icon + tooltip
	 * @param title
	 * @param tooltip
	 * @param iconPath
	 * @return
	 */
	public static JPanel getCustomTab(String title, String tooltip, String iconPath) {
		JPanel pnlTab = new JPanel(new GridBagLayout());
		pnlTab.setOpaque(true);
		pnlTab.setPreferredSize(new Dimension(100,25));
		pnlTab.setToolTipText(tooltip);

		// tab title
		JLabel lblTitle = new JLabel(title);
		
		// image label
		JLabel imageLbl = new JLabel();
		imageLbl.setIcon(new ImageIcon(iconPath));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 4;
		gbc.ipady = 0;
		gbc.weightx = 4;

		// add elements to pnlTab : order must be same as "int TAB_XXX_ORDER" defined at start of this class
		pnlTab.add(imageLbl, gbc); // 1 - ICON
		gbc.gridx++;
		pnlTab.add(lblTitle, gbc); // 2 - TEXT TITLE

		return pnlTab;
	}
	
	
	public static JButton applyStyleToTabCloseBtn(JButton btnClose) {
		Color background = btnClose.getBackground();
		Color foreground = new Color(210,210,210);
		
		btnClose.setFont(new Font("SansSerif", Font.BOLD, 9));
		btnClose.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		btnClose.setMargin(new Insets(4, 4, 4, 4));
		btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnClose.setBackground(background);
		btnClose.setForeground(foreground);
		btnClose.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
				btnClose.setForeground(foreground);
				btnClose.setBackground(background);
				btnClose.setText("x");
			}
			public void mouseEntered(MouseEvent e) {
				btnClose.setForeground(Color.WHITE);
				btnClose.setBackground(background.brighter());
				btnClose.setText("X");
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
		return btnClose;
	}
}
