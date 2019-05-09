package frameutils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Cursor;

import net.miginfocom.swing.MigLayout;
import resources.SoundsConfigurator;
import textareautil.SelectionDto;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import media.Sounds;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.awt.Font;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class SearchFrame extends JFrame{

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(SearchFrame.class);
	
	public static final String SEARCH_PLACEHOLDER = "Search word...";
	public static final String REPLACE_PLACEHOLDER = "Replace selected";
		
	public JTextArea textSearchArea;
	public JTextField searchTextField;
	private JTextField txtReplaceSelection;
	private JButton btnChangeText;
	private JButton btnNext;
	private JButton btnPrev;
	private JFrame thisFrame;
	private JCheckBox chckbxCaseSensitive;
	private JTabbedPane tabbedPane;
	private JPanel tabSearch;
	
	private String textTemp;
	private boolean textChanged = true;
	private boolean end = false;
	private int indexStart = 0;
	
	public volatile boolean active = false;
	
	public SearchFrame(boolean undecorated, boolean alwaysOnTop, JTextArea targetSearchArea) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		thisFrame = this;
		active = true;
		
		this.setUndecorated(undecorated);
		this.setAlwaysOnTop(alwaysOnTop);
		this.setTitle("Text Search");
		getContentPane().setBackground(Color.GRAY);
			
		tabSearch = new JPanel();
		tabSearch.setLayout(new MigLayout("", "[10px][100px,grow][100px,grow][100px,grow][100px][100px][120px,grow][10px]", "[50px,grow][50px]"));
		tabSearch.setBackground(Color.DARK_GRAY);
		getContentPane().add(tabSearch);
		
		textSearchArea = targetSearchArea;
		textSearchArea.requestFocus();
		textSearchArea.setCaret(new DefaultCaret() {
			@Override
			public void setSelectionVisible(boolean visible) {
				super.setSelectionVisible(true);
			}
		});

		searchTextField = new JTextField();
		searchTextField.setMinimumSize(new Dimension(6, 35));
		searchTextField.setHorizontalAlignment(SwingConstants.CENTER);
		searchTextField.setText(SEARCH_PLACEHOLDER);
		searchTextField.setToolTipText("Search term");
		tabSearch.add(searchTextField, "cell 1 0 3 1,growx,aligny center");
		searchTextField.setColumns(10);
		
		chckbxCaseSensitive = new JCheckBox("Case Sensitive");
		chckbxCaseSensitive.setFont(new Font("SansSerif", Font.PLAIN, 14));
		chckbxCaseSensitive.setForeground(Color.WHITE);
		tabSearch.add(chckbxCaseSensitive, "cell 4 0 2 1,alignx center");
		
		txtReplaceSelection = new JTextField();
		txtReplaceSelection.setToolTipText("Replace Selected text");
		txtReplaceSelection.setText(REPLACE_PLACEHOLDER);
		txtReplaceSelection.setMinimumSize(new Dimension(6, 35));
		txtReplaceSelection.setHorizontalAlignment(SwingConstants.CENTER);
		txtReplaceSelection.setColumns(10);
		tabSearch.add(txtReplaceSelection, "cell 1 1 3 1,growx");
		
		btnChangeText = new JButton("Change text");
		btnChangeText.setToolTipText("Replace selected text");
		btnChangeText.setMinimumSize(new Dimension(42, 35));
		btnChangeText.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnChangeText.setBackground(new Color(70, 130, 180));
		tabSearch.add(btnChangeText, "cell 4 1 2 1,growx");
		
		btnNext = new JButton("Next");
		btnNext.setMinimumSize(new Dimension(42, 35));
		btnNext	.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnNext.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnNext.setBackground(new Color(119, 136, 153));
		tabSearch.add(btnNext, "cell 6 0,growx,aligny center");
		
		getRootPane().setDefaultButton(btnNext);
		
		btnPrev = new JButton("Prev");
		btnPrev.setMinimumSize(new Dimension(42, 35));
		btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnPrev.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnPrev.setBackground(new Color(119, 136, 153));
		tabSearch.add(btnPrev, "cell 6 1,growx,aligny center");

		adHandlersTextAreaSimple();
		
		logger.info(this.getClass().getName() + " Running");
	}
	
	private void adHandlersTextAreaSimple() {
		
		// window on close operations
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            active = false;
		        	logger.debug("Closing "+getClass().getName());
		        	dispose();
		    }
		});
		
		this.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
			}
			public void focusGained(FocusEvent e) {
				textChanged = true;
				textSearchArea.revalidate();
				loadText();
			}
		});
		
		// FIND PREV OCCURRENCE
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTxtArea(searchTextField.getText(), false, chckbxCaseSensitive.isSelected());
			}
		});
		
		btnPrev.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				getRootPane().setDefaultButton(btnPrev);
			}
		});
		
		// FIND NEXT OCCURRENCE
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTxtArea(searchTextField.getText(), true, chckbxCaseSensitive.isSelected());
			}
		});
		
		btnNext.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				getRootPane().setDefaultButton(btnNext);
			}
		});
		
		btnNext.requestFocus();
		
		// REPLACE TEXT SELECTED
		btnChangeText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceTextSelected();
			}
		});
		
		btnChangeText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				getRootPane().setDefaultButton(btnChangeText);
			}
		});
		
		textSearchArea.getDocument().addDocumentListener(new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				mainTextAreaChanged();
			}
			
			public void insertUpdate(DocumentEvent e) {
				mainTextAreaChanged();
			}
			
			public void changedUpdate(DocumentEvent e) {
				mainTextAreaChanged();
			}
		});
		
		thisFrame.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
				searchTextField.setSelectionStart(0);
			}
			
			public void focusGained(FocusEvent e) {
				String selected = textSearchArea.getSelectedText();
				if(!searchTextField.getText().equals(selected) && thisFrame.hasFocus()) {
					searchTextField.setText(selected);
				}
			}
		});
		
		addPlacehoderActions(searchTextField, SEARCH_PLACEHOLDER);
		
		addPlacehoderActions(txtReplaceSelection, REPLACE_PLACEHOLDER);
		
	}
	
	private void mainTextAreaChanged() {
		textChanged = true;
		indexStart = textSearchArea.getSelectionStart();
	}
	
	private void replaceTextSelected() {
		SelectionDto selectionTxtArea = new SelectionDto(textSearchArea);
		
		int selStart = selectionTxtArea.first;
		String output = selectionTxtArea.prefix.concat(txtReplaceSelection.getText()).concat(selectionTxtArea.end);
		textSearchArea.setText("");
		textSearchArea.append(output);
		
		textSearchArea.requestFocus();
		textSearchArea.setSelectionStart(selStart);
		textSearchArea.setSelectionEnd(selStart + txtReplaceSelection.getText().length());
	}
	
	public static void addPlacehoderActions(JTextField field, String placeholder) {
		// Search text
		field.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
				if(field.getText().equals(placeholder)) {
					field.setText("");
				}else {
					field.setSelectionStart(0);
					field.setSelectionEnd(field.getText().length());
				}
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		field.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(field.getText().equals(placeholder)) {
					field.setText("");
				}
			}
		});
		
		
	}
	
	// SEARCH FUNCTIONS //
	
	private void searchTxtArea(String match, boolean forward, boolean caseSensitive) {
		loadText();
		if(!caseSensitive) {
			textTemp = textTemp.toLowerCase();
			match = match.toLowerCase();
			textChanged = true;
		}
		
		int tempIndex = indexStart;
		if(forward) {
			tempIndex = textTemp.indexOf(match, indexStart);
		}else {
			tempIndex = textTemp.lastIndexOf(match, indexStart);
		}
		// if found update reference, else use 0
		indexStart = (tempIndex >= 0)? tempIndex : textSearchArea.getSelectionStart();
		
		if (tempIndex >= 0) {
			textSearchArea.setSelectionStart(indexStart);
			textSearchArea.setSelectionEnd(indexStart + match.length());
			end = false;
			Sounds.playSound(SoundsConfigurator.CLICK_2, null);
		}else {
			end = true;
			Sounds.playSound(SoundsConfigurator.CLICK_1, null);
		}
		
		ifEndGoToStart(match, forward);
	}
	
	private void ifEndGoToStart(String match, boolean forward) {
		int totTxtAreaLength = textSearchArea.getText().length();
		if(forward && end) {
			indexStart = 0;
		}else if(!forward && end) {
			indexStart = totTxtAreaLength;
		}else {
			if(forward) {
				indexStart++;
			}else if(!forward) {
				indexStart--;
			}
		}
	}
	
	private void loadText() {
		if (textChanged) {
			textTemp = textSearchArea.getText();
			textChanged = false;
		}
	}
	
}
