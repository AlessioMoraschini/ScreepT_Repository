package guiTEST;

import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.SystemColor;
import javax.swing.UIManager;

import crypt.aes.AesCipher;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class CryptoStegTest_Gui_V2_0 extends JDialog{

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	private JFrame frame;
	private JTextField AesKey;
	private boolean selectS,selectE;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CryptoStegTest_Gui_V2_0 window = new CryptoStegTest_Gui_V2_0();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CryptoStegTest_Gui_V2_0() {
		initialize();
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		selectE=false;
		selectS=false;
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
		frame.getContentPane().setForeground(Color.DARK_GRAY);
		frame.setBounds(100, 100, 1142, 728);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		
		JTextPane source = new JTextPane();
		source.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if(selectS==false) {
					source.selectAll();
					selectS=true;
				}
				else {
					source.select(0,0);
					selectS=false;
				}
					
							
			}
		});
		source.setToolTipText("\r\n");
		source.setForeground(new Color(51, 51, 51));
		source.setFont(new Font("Monospaced", Font.PLAIN, 28));
		source.setBounds(755, 133, 945, 230);
		source.setBackground(SystemColor.info);
		frame.getContentPane().add(source);
		
		JScrollPane scrollPaneSource = new JScrollPane(source);
		scrollPaneSource.setBounds(134, 46, 945, 248);
		frame.getContentPane().add(scrollPaneSource);
		
		JLabel keyAlert = new JLabel("");
		keyAlert.setHorizontalAlignment(SwingConstants.LEFT);
		keyAlert.setBounds(144, 380, 935, 23);
		frame.getContentPane().add(keyAlert);
		
		JTextPane encoded = new JTextPane();
		encoded.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				if(selectE==false) {
					encoded.selectAll();
					selectE=true;
				}
				else {
					encoded.select(0,0);
					selectE=false;
				}			
			}
		});
		encoded.setBackground(UIManager.getColor("Viewport.background"));
		encoded.setFont(new Font("Monospaced", Font.PLAIN, 28));
		encoded.setBounds(202, 434, 596, 164);
		frame.getContentPane().add(encoded);
		
		JScrollPane scrollPane_encoded = new JScrollPane(encoded);
		scrollPane_encoded.setBounds(134, 410, 945, 248);
		frame.getContentPane().add(scrollPane_encoded);
		
		JLabel lblProcessed = new JLabel("Processed");
		lblProcessed.setFont(new Font("Dialog", Font.BOLD, 16));
		lblProcessed.setHorizontalAlignment(SwingConstants.CENTER);
		lblProcessed.setBounds(15, 509, 104, 40);
		frame.getContentPane().add(lblProcessed);
		
		JLabel lblSource = new JLabel("Source");
		lblSource.setFont(new Font("Dialog", Font.BOLD, 16));
		lblSource.setHorizontalAlignment(SwingConstants.CENTER);
		lblSource.setBounds(15, 137, 104, 40);
		frame.getContentPane().add(lblSource);
		
		AesKey = new JTextField();
		AesKey.setBackground(SystemColor.inactiveCaptionBorder);
		AesKey.setFont(new Font("Tahoma", Font.PLAIN, 32));
		AesKey.setHorizontalAlignment(SwingConstants.CENTER);
		AesKey.setBounds(134, 304, 548, 70);
		frame.getContentPane().add(AesKey);
		AesKey.setColumns(10);
		
		JButton btnEcrypt = new JButton("encrypt");
		btnEcrypt.setBackground(SystemColor.inactiveCaption);
		btnEcrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				keyAlert.setText("");
				AesKey.setBackground(SystemColor.inactiveCaptionBorder);
				
				try {
					AesCipher crypter=new AesCipher(AesKey.getText());
					encoded.setText(crypter.encrypt(source.getText()));
//					AesKey.setText(crypter.getDispKey());
				}catch(Exception e) {encoded.setText("Chiave sbagliata...Riprova!");}
								
			}
		});
		
		
		btnEcrypt.setBounds(705, 304, 123, 72);
		frame.getContentPane().add(btnEcrypt);
		
		//fine descrizione jbutton codifica
		
		//inizio descrizion button decodifica
		JButton btnNewButton = new JButton("decrypt");
		btnNewButton.setBackground(SystemColor.inactiveCaption);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
							
				keyAlert.setText("");
				AesKey.setBackground(SystemColor.inactiveCaptionBorder);
				
				try {
					AesCipher decrypter=new AesCipher(AesKey.getText());
					encoded.setText(decrypter.decrypt(source.getText()));
//					AesKey.setText(decrypter.getDispKey());
				}catch(Exception e) {encoded.setText("Chiave sbagliata...Riprova!");}
				
			}
		});
		
		btnNewButton.setBounds(850, 304, 123, 72);
		frame.getContentPane().add(btnNewButton);
		
		JLabel lblText = new JLabel("Text");
		lblText.setFont(new Font("Dialog", Font.BOLD, 16));
		lblText.setHorizontalAlignment(SwingConstants.CENTER);
		lblText.setBounds(15, 548, 104, 20);
		frame.getContentPane().add(lblText);
		
		JLabel lblText_1 = new JLabel("Text");
		lblText_1.setFont(new Font("Dialog", Font.BOLD, 16));
		lblText_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblText_1.setBounds(15, 177, 104, 20);
		frame.getContentPane().add(lblText_1);
		
		JLabel lblAesKey = new JLabel("AES KEY");
		lblAesKey.setForeground(SystemColor.inactiveCaptionBorder);
		lblAesKey.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblAesKey.setHorizontalAlignment(SwingConstants.CENTER);
		lblAesKey.setBounds(15, 295, 104, 47);
		frame.getContentPane().add(lblAesKey);
		
		JLabel lblCrypter = new JLabel("Crypter 1.2 - by Alessio");
		lblCrypter.setHorizontalAlignment(SwingConstants.CENTER);
		lblCrypter.setFont(new Font("Tahoma", Font.BOLD, 28));
		lblCrypter.setBounds(248, 6, 714, 34);
		frame.getContentPane().add(lblCrypter);
		
		JButton Check = new JButton("Check");
		Check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String actkey=AesKey.getText();
                if(actkey.length()==16) {
					AesKey.setBackground(Color.green);
					keyAlert.setText("Oddemooo! Chiave a 16 caratteri! :D");
				}
						
                if(actkey.length()<16) {
                	
				    AesKey.setBackground(Color.orange);					    
				    keyAlert.setText("We, tirchio, potresti arrivare a 16 caratteri, ne hai usati "+AesKey.getText().length()+" ;)");
				}
				if(actkey.length()>16) {
				    AesKey.setBackground(Color.yellow); 	
				    keyAlert.setText("Ella maddonna, calmo con quella chiave! "+AesKey.getText().length()+" caratteri sono troppi! Te ne bastano 16 per stare al sicuro ;)");
				}
			}
		});
		Check.setBackground(SystemColor.controlHighlight);
		Check.setForeground(new Color(51, 51, 51));
		Check.setBounds(27, 340, 78, 26);
		frame.getContentPane().add(Check);
		
		JButton btnClearAll = new JButton("Clear");
		btnClearAll.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				btnClearAll.setBackground(new Color(254, 230, 190));
			}
			@Override
			public void focusLost(FocusEvent e) {
				btnClearAll.setBackground(new Color(255, 235, 205));
			}
		});
		btnClearAll.setBackground(new Color(255, 235, 205));
		btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				encoded.setText("");
				source.setText("");
				AesKey.setText("");
				keyAlert.setText("");
				AesKey.setBackground(SystemColor.inactiveCaptionBorder);
				
				
			}
		});
		btnClearAll.setBounds(996, 304, 78, 70);
		frame.getContentPane().add(btnClearAll);
		
		
        // fine desccrizione button decodifica
	}
	
	public static void check(JTextField aesKey) {
		String actkey=aesKey.getText();
        if(actkey.length()==16) {
        	aesKey.setBackground(Color.green);
			aesKey.setToolTipText("WOW! 16 char is perfect! :D");
		}		
        if(actkey.length()<16) {     	
        	aesKey.setBackground(Color.orange);					    
        	aesKey.setToolTipText("You could reach 16 chars to have best secured password ;)");
		}
		if(actkey.length()>16) {
			aesKey.setBackground(Color.yellow); 	
			aesKey.setToolTipText("More that 16 chars are overkill, only first 16 are actually used :)");
		}
	}
}
