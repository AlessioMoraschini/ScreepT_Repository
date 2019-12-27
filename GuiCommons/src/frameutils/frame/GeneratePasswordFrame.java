package frameutils.frame;

import java.awt.Dimension;

import javax.swing.JFrame;

import frameutils.frame.arch.ParentFrame;
import frameutils.frame.panels.ClipboardPanelLight;
import frameutils.utils.GuiUtils;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.apache.commons.lang3.RandomStringUtils;

public class GeneratePasswordFrame extends ParentFrame{
	private static final long serialVersionUID = 7066597918430250412L;
	
	public static GeneratePasswordFrame singletonInstance = null;

	private char[] possibleCharacters = 
			new String(
				"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789`!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
			).toCharArray();
	
	JFrame thisFrame;
	
	ClipboardPanelLight	payloadPanel;
	private JTextField textField;
	private JLabel lblLength;
	private JSpinner spinnerLength;
	private JButton btnGenerate;
	
	public GeneratePasswordFrame(JFrame parentFrame) {
		super();
		getContentPane().setBackground(new Color(10,10,10));
		
		logger.info(this.getClass().getName() + " - Starting...");
		
		setTitle("Generate Password - " + GeneralConfig.APPLICATION_NAME);
		
		this.setMinimumSize(new Dimension(650, 60));

		setVisible(true);
		setAlwaysOnTop(true);
		
		// SW window icon
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_KEYGEN_PSW, this);
		getContentPane().setLayout(new MigLayout("", "[::5px][grow][70px][60px][]", "[grow][:35px:40px][grow]"));
		
		textField = new JTextField();
		getContentPane().add(textField, "cell 1 1,grow");
		textField.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 17));
		textField.setColumns(10);
		textField.setBackground(Color.DARK_GRAY);
		textField.setForeground(Color.WHITE);
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		
		lblLength = new JLabel("Length");
		lblLength.setForeground(Color.WHITE);
		lblLength.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
		getContentPane().add(lblLength, "cell 2 1,alignx center");
		
		spinnerLength = new JSpinner();
		spinnerLength.setToolTipText("Best match to use in ScreepT is 16 characters");
		spinnerLength.setModel(new SpinnerNumberModel(16, 1, 64, 1));
		GuiUtils.setJspinnerColors(spinnerLength, Color.GRAY, new Color(10,10,10));
		spinnerLength.setAlignmentY(Component.TOP_ALIGNMENT);
		spinnerLength.setAlignmentX(Component.LEFT_ALIGNMENT);
		spinnerLength.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
		getContentPane().add(spinnerLength, "cell 3 1,grow");
		
		btnGenerate = new JButton("Generate");
		btnGenerate.setCursor(GuiUtils.CURSOR_HAND);
		btnGenerate.setBackground(Color.GRAY);
		btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(btnGenerate, "cell 4 1,grow");
		
		addHandlers();
		
		btnGenerate.doClick();
		
		logger.info(this.getClass().getName() + " - Started and ready!");
	}
	
	private void addHandlers() {
		
		// DEFAULT CLOSE ACTIONS 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	
		    	singletonInstance = null;
		    	
		    	dispose();
		    }
	    });
		
		btnGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String randomString = RandomStringUtils.random((int)spinnerLength.getValue(), possibleCharacters);
				textField.setText(randomString);
			}
		});
		
		textField.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				String selection = textField.getSelectedText();
				if (selection == null) {
					textField.selectAll();
				}
			}
		});
	}
	
	public void forceLentgh(int length) {
		spinnerLength.setValue(length);
		spinnerLength.setEnabled(false);
	}

	public void unforceLentgh(int length) {
		spinnerLength.setValue(length);
		spinnerLength.setEnabled(true);
	}
	
	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(650, 110);
	}
	
	public static GeneratePasswordFrame getInstance(JFrame parentFrame) {
		return singletonInstance == null ? new GeneratePasswordFrame(parentFrame) : singletonInstance;
	}
}
