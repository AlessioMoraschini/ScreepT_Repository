package frameutils;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Cursor;

import net.miginfocom.swing.MigLayout;
import resources.SoundsConfigurator;

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

public class SearchFrame extends JFrame{

	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(SearchFrame.class);
	
	public static final String SEARCH_PLACEHOLDER = "Search word...";
		
	public JTextArea textSearchArea;
	private JTextField searchTextField;
	private JButton btnNext;
	private JButton btnPrev;
	
	private String textTemp;
	private boolean textChanged = true;
	private boolean end = false;
	private int indexStart = 0;
	
	public SearchFrame(boolean undecorated, boolean alwaysOnTop, JTextArea targetSearchArea) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		this.setUndecorated(undecorated);
		this.setAlwaysOnTop(alwaysOnTop);
		this.setTitle("Text Search");
		textSearchArea = targetSearchArea;
		textSearchArea.requestFocus();
		textSearchArea.setCaret(new DefaultCaret() {
			@Override
			public void setSelectionVisible(boolean visible) {
				super.setSelectionVisible(true);
			}
		});

		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[10px][100px,grow][100px,grow][100px,grow][100px][100px,grow][10px]", "[50px,grow]"));
		
		searchTextField = new JTextField();
		searchTextField.setMinimumSize(new Dimension(6, 35));
		searchTextField.setHorizontalAlignment(SwingConstants.CENTER);
		searchTextField.setText(SEARCH_PLACEHOLDER);
		searchTextField.setToolTipText("Search term");
		getContentPane().add(searchTextField, "cell 1 0 3 1,growx,aligny center");
		searchTextField.setColumns(10);
		
		btnPrev = new JButton("Prev");
		btnPrev.setMinimumSize(new Dimension(42, 35));
		btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnPrev.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnPrev.setBackground(new Color(119, 136, 153));
		getContentPane().add(btnPrev, "cell 4 0,growx,aligny center");
		
		btnNext = new JButton("Next");
		btnNext.setMinimumSize(new Dimension(42, 35));
		btnNext	.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnNext.setFont(new Font("SansSerif", Font.BOLD, 13));
		btnNext.setBackground(new Color(119, 136, 153));
		getContentPane().add(btnNext, "cell 5 0,growx,aligny center");
		
//		getRootPane().setDefaultButton(btnPrev);
		getRootPane().setDefaultButton(btnNext);
		
		adHandlersTextAreaSimple();
		
		logger.info(this.getClass().getName() + " Running");
	}
	
	private void adHandlersTextAreaSimple() {
		
		// FIND PREV OCCURRENCE
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchTxtArea(searchTextField.getText(), false);
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
				searchTxtArea(searchTextField.getText(), true);
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
		
		textSearchArea.getDocument().addDocumentListener(new DocumentListener() {
			
			public void removeUpdate(DocumentEvent e) {
				textChanged = true;
				indexStart = textSearchArea.getSelectionStart();
			}
			
			public void insertUpdate(DocumentEvent e) {
				textChanged = false;
				indexStart = textSearchArea.getSelectionStart();
			}
			
			public void changedUpdate(DocumentEvent e) {
				textChanged = true;
				indexStart = textSearchArea.getSelectionStart();
			}
		});
		
		searchTextField.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
				if(searchTextField.getText().equals(SEARCH_PLACEHOLDER)) {
					searchTextField.setText("");
				}else {
					searchTextField.setSelectionStart(0);
					searchTextField.setSelectionEnd(searchTextField.getText().length());
				}
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseClicked(MouseEvent e) {
			}
		});
		
		searchTextField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(searchTextField.getText().equals(SEARCH_PLACEHOLDER)) {
					searchTextField.setText("");
				}
			}
		});
		
	}
	
	// SEARCH FUNCTIONS //
	
	private void searchTxtArea(String match, boolean forward) {
		loadText();
		ifEndGoToStart(match, forward);

		int tempIndex = indexStart;
		if(forward) {
			tempIndex = textTemp.indexOf(match, indexStart+1);
		}else {
			tempIndex = textTemp.lastIndexOf(match, indexStart-1);
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
	}
	
	private void ifEndGoToStart(String match, boolean forward) {
		int totTxtAreaLength = textSearchArea.getText().length();
		if(forward && end) {
			indexStart = 0;
		}else if(!forward && end) {
			indexStart = totTxtAreaLength-1;
		}
	}
	
	private void loadText() {
		if (textChanged) {
			textTemp = textSearchArea.getText();
			textChanged = false;
		}
	}
	
}
