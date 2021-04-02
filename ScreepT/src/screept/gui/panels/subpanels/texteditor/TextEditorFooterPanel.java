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
package screept.gui.panels.subpanels.texteditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import initializer.configs.impl.INItializer;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import screept.gui.panels.TextEditor;
import screept.gui.utils.texteditor.TextEditorUtils;
import various.common.light.gui.GuiUtils;

public class TextEditorFooterPanel extends JPanel{
	private static final long serialVersionUID = 6053593284206473287L;

	public TextEditor parent;
	public TextEditorUtils parentUtils;
	public INItializer currentConfiguration;

	public JRadioButton rdbtnShowCompletePath;
	public final static String[] radioButtonOptions = {"Path", "Name"};

	public JLabel lblCurrentFileLBL;
	public JLabel labelTotLines;
	public JLabel labelTotalChars;
	public JLabel labelSelectionLength;
	public JLabel labelCharset;
	public JLabel dynLblTotLines;
	public JLabel dynLblTotChars;
	public JLabel dynLblSelLength;
	public JLabel dynLabelCharset;
	public JLabel lblColumn;
	public JLabel dynLblColumn;
	public JLabel lblLine;
	public JLabel dynLblCurrLine;
	public JLabel labelCurrPos;
	public JLabel dynLblCurrPos;
	public JLabel dynLblSearchEsit;
	public JLabel lblAdvice;

	public JTextField lblCurrentFile;

	public TextEditorFooterPanel(TextEditor parent) {
		super();
		this.parent = parent;
		this.parentUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;

		setBorder(new LineBorder(new Color(0, 0, 0), 2));
		setBackground(Color.GRAY);
		setLayout(new MigLayout("fill,insets 3, hidemode 3",
				"[][50px,grow][50px][][:50px:120px][][:45px:90px][][:50px:120px][][:20px:120px][:60px:120px][][:60px:110px][][:20px:120px][][:50px:120px][grow][70px:n][]",
				"[fill]"));

		lblCurrentFileLBL = new JLabel(" Current file:");
		add(lblCurrentFileLBL, "cell 0 0,growy");
		lblCurrentFileLBL.setForeground(SystemColor.controlHighlight);
		lblCurrentFileLBL.setFont(GeneralConfig.BUTTON_DEFAULT_FONT);

		lblCurrentFile = new JTextField("\\*.txt");
		lblCurrentFile.setMinimumSize(new Dimension(40, 22));
		lblCurrentFile.setBackground(Color.DARK_GRAY);
		lblCurrentFile.setOpaque(true);
		lblCurrentFile.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblCurrentFile.setForeground(SystemColor.activeCaption);
		add(lblCurrentFile, "cell 1 0,growx");

		rdbtnShowCompletePath = new JRadioButton(radioButtonOptions[0]);
		rdbtnShowCompletePath.setOpaque(false);
		rdbtnShowCompletePath.setCursor(GuiUtils.CURSOR_HAND);
		rdbtnShowCompletePath.setFont(new Font("SansSerif", Font.PLAIN, 11));
		add(rdbtnShowCompletePath, "cell 2 0");
		rdbtnShowCompletePath.setSelected(true);
		rdbtnShowCompletePath.setForeground(Color.WHITE);

		labelCharset = new JLabel("Charset:");
		labelCharset.setForeground(SystemColor.controlHighlight);
		labelCharset.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(labelCharset, "cell 3 0");

		dynLabelCharset = new JLabel("");
		dynLabelCharset.setForeground(SystemColor.inactiveCaptionBorder);
		dynLabelCharset.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLabelCharset, "cell 4 0");

		labelTotLines = new JLabel("Lines:");
		labelTotLines.setForeground(SystemColor.controlHighlight);
		labelTotLines.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(labelTotLines, "cell 5 0,alignx right,aligny center");

		dynLblTotLines = new JLabel("");
		dynLblTotLines.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblTotLines.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblTotLines, "cell 6 0");

		labelTotalChars = new JLabel("Length:");
		labelTotalChars.setForeground(SystemColor.controlHighlight);
		labelTotalChars.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(labelTotalChars, "cell 7 0,alignx right");

		dynLblTotChars = new JLabel("");
		dynLblTotChars.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblTotChars.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblTotChars, "cell 8 0");

		lblLine = new JLabel("Sel. line:");
		lblLine.setForeground(SystemColor.controlHighlight);
		lblLine.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(lblLine, "cell 9 0");

		dynLblCurrLine = new JLabel("");
		dynLblCurrLine.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblCurrLine.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblCurrLine, "cell 10 0");

		labelCurrPos = new JLabel("Position:");
		labelCurrPos.setForeground(SystemColor.controlHighlight);
		labelCurrPos.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(labelCurrPos, "cell 11 0,alignx right");

		dynLblCurrPos = new JLabel("");
		dynLblCurrPos.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblCurrPos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblCurrPos, "cell 12 0");

		lblColumn = new JLabel("Sel. Column:");
		lblColumn.setForeground(SystemColor.controlHighlight);
		lblColumn.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(lblColumn, "cell 13 0");

		dynLblColumn = new JLabel("");
		dynLblColumn.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblColumn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblColumn, "cell 14 0");

		labelSelectionLength = new JLabel("Sel. length:");
		labelSelectionLength.setForeground(SystemColor.controlHighlight);
		labelSelectionLength.setFont(new Font("Segoe UI", Font.BOLD, 11));
		add(labelSelectionLength, "cell 15 0,alignx right");

		dynLblSelLength = new JLabel("");
		dynLblSelLength.setForeground(SystemColor.inactiveCaptionBorder);
		dynLblSelLength.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		add(dynLblSelLength, "cell 16 0");

		dynLblSearchEsit = new JLabel("");
		dynLblSearchEsit.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblSearchEsit.setForeground(new Color(255, 222, 173));
		dynLblSearchEsit.setFont(new Font("Segoe UI", Font.BOLD, 13));
		dynLblSearchEsit.setBackground(Color.WHITE);
		add(dynLblSearchEsit, "cell 17 0,alignx center");

		lblAdvice = new JLabel("");
		lblAdvice.setAlignmentX(0.9f);
		add(lblAdvice, "cell 18 0,alignx right,aligny center");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		lblAdvice.setForeground(Color.LIGHT_GRAY);
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdvice.setText("[Type: .txt]");

		addHandlers();

	}

	private void addHandlers() {
		// FOOTER UI RADIOBOX COMPLETE PATH (current file)
		rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
		rdbtnShowCompletePath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (rdbtnShowCompletePath.isSelected()) {
							rdbtnShowCompletePath.setForeground(Color.WHITE);
							rdbtnShowCompletePath.setText(radioButtonOptions[0]);
						} else {
							rdbtnShowCompletePath.setForeground(Color.LIGHT_GRAY);
							rdbtnShowCompletePath.setText(radioButtonOptions[1]);
						}
					}
				});
			}
		});
	}
}
