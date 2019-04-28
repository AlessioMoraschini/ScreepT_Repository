package frameutils.html;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ButtonUtils.ButtonDefaults;
import media.Sounds;
import net.miginfocom.swing.MigLayout;
import resources.IconsPathConfigurator;
import resources.SoundsConfigurator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class HeaderPanelSearch extends JPanel{
	
	private JButton btnGoBack;
	private JButton btnGoNext;
	private JButton btnReload;
	private JButton btnOpenInBrowser;
	
	public HeaderPanelSearch headerPaneThis;
	private JavaFxPanelHtml panelFxHtml;
	private JButton btnIndex;
	private JButton btnFind;
	private JTextField textFieldSearch;
	
	public HeaderPanelSearch(JavaFxPanelHtml panel) {
		
		panelFxHtml = panel;
		headerPaneThis = this;
		
		setBorder(new LineBorder(Color.white, 2));
		setBackground(Color.gray);
		setLayout(new MigLayout("", "[50px][50px][50px][50px][50px][grow][50px]", "[30px,grow]"));
		
		btnGoBack = new JButton("Back");
		btnGoBack.setOpaque(true);
		btnGoBack.setInheritsPopupMenu(true);
		btnGoBack.setBackground(Color.WHITE);
		btnGoBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGoBack.setIcon(new ImageIcon(IconsPathConfigurator.ICON_UNDO));
		add(btnGoBack, "cell 0 0,growx,aligny center");
		
		btnGoNext = new JButton("Forward");
		btnGoNext.setOpaque(true);
		btnGoNext.setBackground(Color.WHITE);
		btnGoNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnGoNext.setIcon(new ImageIcon(IconsPathConfigurator.ICON_REDO));
		add(btnGoNext, "cell 1 0,growx,aligny center");
		
		btnReload = new JButton("Reload");
		btnReload.setOpaque(true);
		btnReload.setBackground(Color.WHITE);
		btnReload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReload.setIcon(new ImageIcon(IconsPathConfigurator.ICON_RELOAD_CONF));
		add(btnReload, "cell 2 0,growx,aligny center");
		
		btnOpenInBrowser = new JButton("View in Web Browser");
		btnOpenInBrowser.setOpaque(true);
		btnOpenInBrowser.setBackground(Color.WHITE);
		btnOpenInBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(btnOpenInBrowser, "cell 4 0,growx,aligny center");
		
		btnIndex = new JButton("Index");
		btnIndex.setOpaque(true);
		btnIndex.setBackground(Color.WHITE);
		btnIndex.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		add(btnIndex, "cell 3 0,growx,aligny center");
		
		textFieldSearch = new JTextField();
		textFieldSearch.setHorizontalAlignment(SwingConstants.CENTER);
		add(textFieldSearch, "cell 5 0,growx,aligny center");
		textFieldSearch.setColumns(10);
		
		btnFind = new JButton("Find");
		btnFind.setOpaque(true);
		btnFind.setBackground(Color.WHITE);
		btnFind.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFind.setIcon(new ImageIcon(IconsPathConfigurator.ICON_SEARCH));
		add(btnFind, "cell 6 0,growx,aligny center");
		
		addHandlersFx();
	}
	
	private void addHandlersFx(){
		
		// BACK IN HISTORY
		btnGoBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelFxHtml.goBack();
			}
		});
		
		// FORWARD IN HISTORY
		btnGoNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelFxHtml.goNext();
			}
		});
		
		// RELOAD PAGE
		btnReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelFxHtml.reload();
			}
		});
		
		// GOTO INDEX
		btnIndex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panelFxHtml.goToIndex();
			}
		});
		
		// OPEN IN BROWSER
		btnOpenInBrowser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!panelFxHtml.openInBrowser()) {
					JOptionPane.showMessageDialog(panelFxHtml, "Cannot open in default browser, some error occurred :(", "Error opening in browser", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// SEARCH BUTTON
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!textFieldSearch.getText().equals("")) {
					panelFxHtml.findTextNHighLight(textFieldSearch.getText());
				}else {
					Sounds.playSound(SoundsConfigurator.CLICK_1, SoundsConfigurator.DEFAULT_EFFECTS_VOLUME);
				}
			}
		});
		
		textFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				panelFxHtml.deselectAll();
			}
			public void insertUpdate(DocumentEvent e) {
				panelFxHtml.deselectAll();
			}
			public void changedUpdate(DocumentEvent e) {
				panelFxHtml.deselectAll();
			}
		});
		
		ButtonDefaults.setDefaultOnFocus(btnFind, headerPaneThis);
		ButtonDefaults.setDefaultOnFocus(btnGoBack, headerPaneThis);
		ButtonDefaults.setDefaultOnFocus(btnGoNext, headerPaneThis);
		ButtonDefaults.setDefaultOnFocus(btnOpenInBrowser, headerPaneThis);
		ButtonDefaults.setDefaultOnFocus(btnIndex, headerPaneThis);
		ButtonDefaults.setDefaultOnFocus(btnReload, headerPaneThis);
	}
	
}
