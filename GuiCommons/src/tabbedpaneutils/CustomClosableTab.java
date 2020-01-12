/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package tabbedpaneutils;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;

public class CustomClosableTab extends JPanel{
	
	JPanel pnlTab;
	JLabel lblTitle;
	JLabel imageLbl;
	JButton btnClose;
	
	public CustomClosableTab(String title, String tooltip, String iconPath, ActionListener tabCloseListener) {
//		pnlTab = new JPanel();
//		pnlTab.setOpaque(false);
//		pnlTab.setPreferredSize(new Dimension(130,25));
		setToolTipText(tooltip);
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[25.00px,grow]", "[16.00px, grow]"));
//		add(pnlTab, "cell 0 0,alignx left,aligny top");

		// tab title
		lblTitle = new JLabel();
		lblTitle.setText(title);
		
		// image label
		imageLbl = new JLabel();
		imageLbl.setIcon(new ImageIcon(iconPath));
		imageLbl.setPreferredSize(new Dimension(15, 20));
		imageLbl.setMaximumSize(new Dimension(15, 20));
		
		// button close
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[16.00px][25.00px:35.00px,grow][15.00px]", "[16.00px,grow]"));
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.gridx = 0;
//		gbc.gridy = 0;
//		gbc.ipadx = 2;
//		gbc.ipady = 0;
//		gbc.weightx = 1;

		// add elements to pnlTab : order must be same as "int TAB_XXX_ORDER" defined at start of this class
		add(imageLbl, "cell 0 0 1 1,alignx left,aligny top"); // 1 - ICON
//		gbc.gridx++;
		add(lblTitle, "cell 1 0 1 1,alignx center,aligny top"); // 2 - TEXT TITLE
//		gbc.gridx++;
//		gbc.weightx = 4;
		btnClose = new JButton();
		
		btnClose = TabUtils.applyStyleToTabCloseBtn(btnClose);
		Dimension btnDimension = new Dimension(20, 20) ;
		btnClose.setMinimumSize(btnDimension);
		btnClose.setPreferredSize(btnDimension);
		btnClose.setMaximumSize(btnDimension);
		btnClose.setText("X");
		btnClose.addActionListener(tabCloseListener);

		add(btnClose, "cell 2 0 1 1,alignx right,aligny top"); // 3 - CLOSE BUTTON
		
	}
}
