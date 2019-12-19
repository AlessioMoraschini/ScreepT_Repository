package gUI.panels.subpanels.texteditor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import frameutils.utils.GuiUtils;
import gUI.panels.TextEditor;
import gUI.utils.texteditor.TextEditorUtils;
import gUI.utils.tooltip.TextEditorTooltips;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import om.TabTextEditorObj;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class TextEditorHeaderPanel extends JPanel{
	private static final long serialVersionUID = -457587326299200369L;
	
	public TextEditor parent;
	public TextEditorUtils parentUtils;
	public INItializer currentConfiguration;
	
	private JLabel lblVoiceSpeed;
	public JSlider sliderSpokenSpeed;
	public JCheckBox checkBoxInlineTabs;
	public JCheckBox chkFitmantainwords;
	public JCheckBox chkCutWordsTo;
	public JLabel lblTextEditor;
	private JButton btnNextHistory;
	private JButton btnPrevHistory;
	private JButton btnShowhideFooter;
	private JButton btnShowhideToolkit;


	public TextEditorHeaderPanel(TextEditor parent) {
		
		this.parent = parent;
		this.parentUtils = parent.textEditorUtils;
		this.currentConfiguration = parent.currentConfiguration;
		
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("insets 6, fill, hidemode 3",
				"[160px,left][][][grow][:180px:300px,grow,trailing][20px:24px:24px][20px:24px:24px][20px:24px:24px][10px:10px:10px][][][][][][]",
				"[:0px:55px, grow]"));
		setForeground(SystemColor.menu);
		setBorder(new LineBorder(Color.BLACK, 1));
		setBackground(new Color(30,30,30));
		
		lblVoiceSpeed = new JLabel("Voice rate");
		lblVoiceSpeed.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVoiceSpeed.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		lblVoiceSpeed.setForeground(SystemColor.control);
		add(lblVoiceSpeed, "cell 1 0,alignx right,aligny center");
		
		sliderSpokenSpeed = new JSlider();
		sliderSpokenSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		sliderSpokenSpeed.setValue(currentConfiguration.getTextEditorOpt().getVoiceRate());
		sliderSpokenSpeed.setMaximum(400);
		add(sliderSpokenSpeed, "cell 2 0,growy");
		
		btnPrevHistory = new JButton("");
		btnPrevHistory.setCursor(GuiUtils.CURSOR_HAND);
		btnPrevHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_PREV));
		btnPrevHistory.setToolTipText(TextEditorTooltips.tooltipHystoryPrev);
		GuiUtils.makeTransparentJButton(btnPrevHistory);
		add(btnPrevHistory, "cell 6 0,alignx center,growy");
		
		btnNextHistory = new JButton("");
		btnNextHistory.setCursor(GuiUtils.CURSOR_HAND);
		btnNextHistory.setIcon(new ImageIcon(IconsPathConfigurator.ICON_NEXT));
		btnNextHistory.setToolTipText(TextEditorTooltips.tooltipHystoryNext);
		GuiUtils.makeTransparentJButton(btnNextHistory);
		add(btnNextHistory, "cell 7 0,alignx center,growy");
		
		checkBoxInlineTabs = new JCheckBox("Inline Tabs  ");
		checkBoxInlineTabs.setOpaque(false);
		checkBoxInlineTabs.setSelected(true);
		checkBoxInlineTabs.setToolTipText(TextEditorTooltips.tooltipInlineTabs);
		checkBoxInlineTabs.setCursor(GuiUtils.CURSOR_HAND);
		checkBoxInlineTabs.setForeground(SystemColor.control);
		checkBoxInlineTabs.setBackground(Color.DARK_GRAY);
		checkBoxInlineTabs.setIconTextGap(1);
		checkBoxInlineTabs.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(checkBoxInlineTabs, "cell 9 0,alignx center,aligny center");
		
		chkCutWordsTo = new JCheckBox("Adapt Text Width  ");
		chkCutWordsTo.setOpaque(false);
		chkCutWordsTo.setCursor(GuiUtils.CURSOR_HAND);
		chkCutWordsTo.setToolTipText(TextEditorTooltips.tooltipCutWordsTo);
		chkCutWordsTo.setSelected(false);
		chkCutWordsTo.setForeground(SystemColor.control);
		chkCutWordsTo.setBackground(Color.DARK_GRAY);
		chkCutWordsTo.setIconTextGap(1);
		chkCutWordsTo.setFont(new Font("SansSerif", Font.BOLD, 12));
		add(chkCutWordsTo, "cell 10 0,alignx center,aligny center");
		
		lblTextEditor = new JLabel("Text Editor");
		lblTextEditor.setHorizontalAlignment(SwingConstants.CENTER);
		lblTextEditor.setForeground(SystemColor.text);
		lblTextEditor.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTextEditor.setBackground(SystemColor.activeCaptionBorder);
		add(lblTextEditor, "cell 0 0,alignx left");
		
		chkFitmantainwords = new JCheckBox("Don't cut words");
		chkFitmantainwords.setOpaque(false);
		chkFitmantainwords.setCursor(GuiUtils.CURSOR_HAND);
		chkFitmantainwords.setToolTipText(TextEditorTooltips.tooltipFitMaintainWords);
		chkFitmantainwords.setForeground(SystemColor.control);
		chkFitmantainwords.setBackground(Color.DARK_GRAY);
		chkFitmantainwords.setIconTextGap(1);
		chkFitmantainwords.setFont(new Font("SansSerif", Font.BOLD, 12));
		chkFitmantainwords.setEnabled(false);
		add(chkFitmantainwords, "cell 11 0,alignx right,aligny center");
		
		btnShowhideToolkit = new JButton("Toggle functions panel");
		btnShowhideToolkit.setCursor(GuiUtils.CURSOR_HAND);
		btnShowhideToolkit.setIcon(IconsPathConfigurator.IMG_ICON_VISIBLE);
		btnShowhideToolkit.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(Font.PLAIN));
		btnShowhideToolkit.setForeground(Color.WHITE);
		btnShowhideToolkit.setBackground(Color.DARK_GRAY);
		add(btnShowhideToolkit, "cell 13 0,growy");
		
		btnShowhideFooter = new JButton("Toggle Footer");
		btnShowhideFooter.setCursor(GuiUtils.CURSOR_HAND);
		btnShowhideFooter.setIcon(IconsPathConfigurator.IMG_ICON_INVISIBLE);
		btnShowhideFooter.setFont(GeneralConfig.BUTTON_DEFAULT_FONT.deriveFont(Font.PLAIN));
		btnShowhideFooter.setForeground(Color.WHITE);
		btnShowhideFooter.setBackground(Color.DARK_GRAY);
		add(btnShowhideFooter, "cell 14 0,growy");
		
		addHandlers();
	}
	
	private void addHandlers() {

		// SET SPEAKER'S VOICE SPEED
		sliderSpokenSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int rate = sliderSpokenSpeed.getValue();
				currentConfiguration.getTextEditorOpt().setVoiceRate(rate);
				if (parent.subHeaderPanel.objSpeaker != null) {
					parent.subHeaderPanel.objSpeaker.voice.setDurationStretch((rate));
				}
			}
		});
		

		// ADAPT TO WIDTH MAINTAINING WORDS
		chkFitmantainwords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkFitmantainwords.isSelected()) {
					parent.getCurrentTabEditor().loadedTabObj.textArea.setWrapStyleWord(true);
				} else {
					parent.getCurrentTabEditor().loadedTabObj.textArea.setWrapStyleWord(false);
				}
			}
		});
		
		// CHANGE TAB VISUALIZATION MODE
		checkBoxInlineTabs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInlinedCheck();
			}
		});
		
		// ADAPT TO WIDTH
		if (chkCutWordsTo.isSelected()) {
			parent.getCurrentTabEditor().loadedTabObj.textArea.setLineWrap(true);
			chkFitmantainwords.setEnabled(true);
		}
		chkCutWordsTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TabTextEditorObj loadedTabObj = parent.getCurrentTabEditor().loadedTabObj;

				if (chkCutWordsTo.isSelected()) {
					loadedTabObj.textArea.setLineWrap(true);
					chkFitmantainwords.setEnabled(true);
					currentConfiguration.getTextEditorOpt().breaklineActive = true;
				} else {
					loadedTabObj.textArea.setLineWrap(false);
					loadedTabObj.textArea.setWrapStyleWord(false);
					chkFitmantainwords.setEnabled(false);
					currentConfiguration.getTextEditorOpt().breaklineActive = false;
				}
			}
		});
		
		// PREV IN HISTORY
		btnPrevHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToPreviousInHistory();
			}
		});

		// NEXT IN HISTORY
		btnNextHistory.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.getCurrentTabEditor().goToNextInHistory();
			}
		});
		
		btnShowhideToolkit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parentUtils.hideOrShowHeaderPanelLevel2();
			}
		});
		
		btnShowhideFooter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parentUtils.hideOrShowFooterPanel();
			}
		});
		
	}
	
	public void updateSubpanelVisibilityIconFromConfig() {
		if (currentConfiguration.getTextEditorOpt().subHeaderVisible) {
			btnShowhideToolkit.setIcon(IconsPathConfigurator.IMG_ICON_VISIBLE);
		} else {
			btnShowhideToolkit.setIcon(IconsPathConfigurator.IMG_ICON_INVISIBLE);
		}
	}

	public void updateFooterVisibilityIconFromConfig() {
		if (currentConfiguration.getTextEditorOpt().footerVisible) {
			btnShowhideFooter.setIcon(IconsPathConfigurator.IMG_ICON_VISIBLE);
		} else {
			btnShowhideFooter.setIcon(IconsPathConfigurator.IMG_ICON_INVISIBLE);
		}
	}
	
	public void doInlinedCheck() {
		if (checkBoxInlineTabs.isSelected()) {
			currentConfiguration.getTextEditorOpt().inlinedTabs = true;
			parent.getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		} else {
			currentConfiguration.getTextEditorOpt().inlinedTabs = false;
			parent.getCurrentTabEditor().mainTabList.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		}
		currentConfiguration.writeSingleValue(INItializer.TEXT_EDITOR_OPT, "inlineTabs", currentConfiguration.getTextEditorOpt().inlinedTabs);
	}
	
}
