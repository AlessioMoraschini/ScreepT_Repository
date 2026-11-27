/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import screept.gui.panels.TextEditor;
import screept.gui.utils.texteditor.TextEditorUtils;
import various.common.light.gui.GuiUtils;

/**
 * Bottom bar of the text editor, always on one row: the current file (it takes the space that is left), then framed blocks
 * that group the information (file: charset, EOL, size on disk; text: lines, length; caret: line, column, position;
 * selection), then the messages. Names are darker than values, so a value stands out at a glance.
 */
public class TextEditorFooterPanel extends JPanel{
	private static final long serialVersionUID = 6053593284206473287L;

	/** Names are a softer gray than the values (the bar is gray) */
	private static final Color NAME_COLOR = new Color(185, 185, 185);
	private static final Color VALUE_COLOR = Color.WHITE;
	private static final Font NAME_FONT = new Font("Segoe UI", Font.PLAIN, 11);
	private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 12);

	public TextEditor parent;
	public TextEditorUtils parentUtils;
	public INItializer currentConfiguration;

	public JRadioButton rdbtnShowCompletePath;
	public final static String[] radioButtonOptions = {"Path", "Name"};

	public JLabel lblCurrentFileLBL;
	public JLabel labelTotLines;
	public JLabel labelTotalChars;
	public JLabel labelFileSize;
	public JLabel labelSelectionLength;
	public JLabel labelCharset;
	public JLabel dynLblTotLines;
	public JLabel dynLblTotChars;
	public JLabel dynLblFileSize;
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
	public JLabel dynLblEol;
	public JLabel lblEol;

	public TextEditorFooterPanel(TextEditor parent) {
		super();
		this.parent = parent;
		this.parentUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;

		setBorder(new LineBorder(new Color(0, 0, 0), 2));
		setBackground(Color.GRAY);
		setLayout(new MigLayout("fill,insets 3, hidemode 3, gapx 4", "[][40:200:,grow,fill][][][][][][grow][]", "[fill]"));

		lblCurrentFileLBL = new JLabel(" Current file:");
		add(lblCurrentFileLBL, "cell 0 0,growy");
		lblCurrentFileLBL.setForeground(NAME_COLOR);
		lblCurrentFileLBL.setFont(NAME_FONT);

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

		// block: file
		Block file = new Block();
		labelCharset = name("Charset:");
		dynLabelCharset = value(4);
		file.pair(labelCharset, dynLabelCharset);
		lblEol = name("EOL:");
		dynLblEol = value(4);
		file.pair(lblEol, dynLblEol);
		labelFileSize = name("Size:");
		dynLblFileSize = value(6);
		file.pair(labelFileSize, dynLblFileSize);
		add(file, "cell 3 0,growy");

		// block: text
		Block text = new Block();
		labelTotLines = name("Lines:");
		dynLblTotLines = value(7);
		text.pair(labelTotLines, dynLblTotLines);
		labelTotalChars = name("Length:");
		dynLblTotChars = value(8);
		text.pair(labelTotalChars, dynLblTotChars);
		add(text, "cell 4 0,growy");

		// block: caret
		Block caret = new Block();
		lblLine = name("Sel. line:");
		dynLblCurrLine = value(4);
		caret.pair(lblLine, dynLblCurrLine);
		lblColumn = name("Sel. Column:");
		dynLblColumn = value(3);
		caret.pair(lblColumn, dynLblColumn);
		labelCurrPos = name("Position:");
		dynLblCurrPos = value(7);
		caret.pair(labelCurrPos, dynLblCurrPos);
		add(caret, "cell 5 0,growy");

		// block: selection
		Block selection = new Block();
		labelSelectionLength = name("Sel. length:");
		dynLblSelLength = value(4);
		selection.pair(labelSelectionLength, dynLblSelLength);
		add(selection, "cell 6 0,growy");

		dynLblSearchEsit = new JLabel("");
		dynLblSearchEsit.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblSearchEsit.setForeground(new Color(255, 222, 173));
		dynLblSearchEsit.setFont(new Font("Segoe UI", Font.BOLD, 13));
		dynLblSearchEsit.setBackground(Color.WHITE);
		add(dynLblSearchEsit, "cell 7 0,alignx center");

		lblAdvice = new JLabel("");
		lblAdvice.setAlignmentX(0.9f);
		add(lblAdvice, "cell 8 0,alignx right,aligny center");
		lblAdvice.setBackground(SystemColor.info);
		lblAdvice.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		lblAdvice.setForeground(Color.LIGHT_GRAY);
		lblAdvice.setHorizontalAlignment(SwingConstants.CENTER);
		lblAdvice.setText("[Type: .txt]");

		addHandlers();
	}

	private static JLabel name(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(NAME_COLOR);
		label.setFont(NAME_FONT);
		return label;
	}

	/** Value label: never narrower than the given number of digits, so the bar does not jump while the numbers change */
	private static JLabel value(final int minChars) {
		JLabel label = new JLabel("") {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = Math.max(size.width, getFontMetrics(getFont()).charWidth('0') * minChars);
				return size;
			}
		};
		label.setForeground(VALUE_COLOR);
		label.setFont(VALUE_FONT);
		return label;
	}

	/** File size as people read it: 512 B, 1.5 KB, 12.3 MB, 2.0 GB (1 KB = 1024 bytes); empty for a negative size */
	public static String humanReadableSize(long bytes) {
		if (bytes < 0) {
			return "";
		}
		if (bytes < 1024) {
			return bytes + " B";
		}
		String[] units = { "KB", "MB", "GB", "TB" };
		double value = bytes;
		int unit = -1;
		while (value >= 1024 && unit < units.length - 1) {
			value /= 1024;
			unit++;
		}
		return String.format(java.util.Locale.ROOT, "%.1f %s", value, units[unit]);
	}

	private void addHandlers() {
		// click on the encoding: reopen the file with another one, or convert it
		java.awt.event.MouseAdapter encodingClick = new java.awt.event.MouseAdapter() {
			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
				parent.getCurrentTabEditor().fillEncodingMenu(menu, parent.getCurrentTabEditor().loadedTabObj);
				menu.show(e.getComponent(), 0, -menu.getPreferredSize().height);
			}
		};
		for (JLabel label : new JLabel[] { labelCharset, dynLabelCharset }) {
			label.setCursor(GuiUtils.CURSOR_HAND);
			label.setToolTipText(various.common.light.i18n.I18n.t("editor.click_to_change_encoding"));
			label.addMouseListener(encodingClick);
		}

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

	/** A group of name/value pairs on one row, on a slightly darker rounded background */
	private static final class Block extends JPanel {
		private static final long serialVersionUID = 1L;

		Block() {
			super(new MigLayout("insets 1 6 1 2, gap 0, hidemode 3", "", "[fill]"));
			setOpaque(false);
		}

		void pair(JLabel name, JLabel value) {
			add(name, "gapright 3,aligny center");
			add(value, "gapright 8,aligny center");
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(0, 0, 0, 55));
			g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
			g.dispose();
			super.paintComponent(graphics);
		}
	}
}
