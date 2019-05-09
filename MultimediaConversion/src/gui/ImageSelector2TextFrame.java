package gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import conversion.ImageToTextExtractor;
import dialogutils.GenericFileChooser;
import frameutils.GuiUtils;
import media.ImageGeneralWorker;
import resources.IconsPathConfigurator;
import net.miginfocom.swing.MigLayout;
import net.sourceforge.tess4j.TesseractException;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.JScrollPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.Image;

public class ImageSelector2TextFrame extends JFrame{
	
	// GraphicPreferencesFrame logger
	static Logger logger = Logger.getLogger(ImageSelector2TextFrame.class); 
	
	private static final String PARSED_DEFAULT_TEXT = "Click \"Extract text\" to preview text";
	
	// flag to signal active
	public static boolean active = false;
	
	public int WIDTH = 880;
	public int HEIGHT = GuiUtils.getScreenSize().height-200;
	
	private JPanel imageContainerPanel;
	private JTextArea txtAreaParsedText;
	private JScrollPane textPreviewScrollPane;
	private JLabel labelLoadedImage;
	private JButton btnLoadImage;
	private JButton btnParseImage;
	private JButton btnInjectSelected;
	
	public JFrame mainFrameParent;
	public JFrame thisFrame;
	public JTextArea outputTEXTarea;
	
	private File selectedImageSource;
	public ImageToTextExtractor imageExtractor;
	
	public ImageSelector2TextFrame(JFrame parentRootFrame, JTextArea outputTxtArea, String title) {
		getContentPane().setBackground(Color.LIGHT_GRAY);
		logger.info(this.getClass().getName() + " Starting...");
		active = true;
		
		imageExtractor = new ImageToTextExtractor();
		mainFrameParent = parentRootFrame;
		thisFrame = this;
		outputTEXTarea = outputTxtArea;

		this.setAlwaysOnTop(true);
		this.setTitle(title);
		GuiUtils.setFrameIcon(IconsPathConfigurator.MAIN_APP_ICON, this);
		GuiUtils.centerFrame(this, WIDTH, HEIGHT);
		GuiUtils.moveFrame(this, GuiUtils.getScreenSize().width/4, 0);
		getContentPane().setLayout(new MigLayout("", "[130px][130px][grow][grow][350px]", "[grow][300px][35px]"));
		
		imageContainerPanel = new JPanel();
		imageContainerPanel.setBackground(Color.BLACK);
		imageContainerPanel.setBorder(new LineBorder(new Color(0, 0, 0), 3, true));
		getContentPane().add(imageContainerPanel, "cell 0 0 5 1,grow");
		imageContainerPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		labelLoadedImage = new JLabel("");
		imageContainerPanel.add(labelLoadedImage, "cell 0 0");
		
		textPreviewScrollPane = new JScrollPane();
		textPreviewScrollPane.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		textPreviewScrollPane.setBackground(Color.WHITE);
		getContentPane().add(textPreviewScrollPane, "cell 0 1 5 1,grow");
		
		txtAreaParsedText = new JTextArea();
		txtAreaParsedText.setWrapStyleWord(true);
		txtAreaParsedText.setMargin(new Insets(15, 15, 15, 15));
		txtAreaParsedText.setText(PARSED_DEFAULT_TEXT);
		textPreviewScrollPane.setViewportView(txtAreaParsedText);
		
		btnLoadImage = new JButton("Load Image");
		btnLoadImage.setBackground(new Color(230, 230, 250));
		btnLoadImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnLoadImage.setIcon(new ImageIcon(IconsPathConfigurator.ICON_LOAD));
		btnLoadImage.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		getContentPane().add(btnLoadImage, "cell 0 2,grow");
		
		btnParseImage = new JButton("Extract text");
		btnParseImage.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		btnParseImage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnParseImage.setIcon(new ImageIcon(IconsPathConfigurator.ICON_EXPORT_LOG));
		btnParseImage.setBackground(new Color(230, 230, 250));
		getContentPane().add(btnParseImage, "cell 1 2,grow");
		
		btnInjectSelected = new JButton("Inject Selected");
		btnInjectSelected.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
		btnInjectSelected.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnInjectSelected.setIcon(new ImageIcon(IconsPathConfigurator.ICON_CHECKMARK));
		btnInjectSelected.setBackground(new Color(176, 196, 222));
		getContentPane().add(btnInjectSelected, "cell 4 2,alignx right,growy");
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					
				logger.debug("Closing "+getClass().getName());
				active = false;
	        	dispose();
		    }
		});
		
		addHandlers();
		
		logger.debug(this.getClass().getName() + "### started");
	}
	
	private void addHandlers() {
		
		// LOAD AND DISPLAY IMAGE
		btnLoadImage.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final File selected = GenericFileChooser.fileImgRead(thisFrame);
				if(selected != null && selected.exists()) {
					final Cursor backup = imageContainerPanel.getCursor();
					imageContainerPanel.setCursor(GuiUtils.CURSOR_WAIT);
					
					Runnable runner = new Runnable() {
						public void run() {
							try {
								displayImage(selected);
								selectedImageSource = selected;
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(thisFrame, "An error occurred, cannot load image :(",
										"IO Error", JOptionPane.ERROR_MESSAGE);
								logger.error(e);
							} finally {

								imageContainerPanel.setCursor(backup);
							}
						}
					};
					SwingUtilities.invokeLater(runner);
				}
			}
		});
		
		// PARSE CURRENT IMAGE TO TEXT
		btnParseImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				imageContainerPanel.setCursor(GuiUtils.CURSOR_WAIT);
				txtAreaParsedText.setCursor(GuiUtils.CURSOR_WAIT);
				btnParseImage.setEnabled(false);
				
				SwingWorker worker = new SwingWorker<Boolean, Void>() {
					@Override
				    public Boolean doInBackground() {
						boolean result = true;
						try {
							txtAreaParsedText.setText(imageExtractor.readTextFromImage(selectedImageSource));
						} catch (TesseractException e1) {
							logger.error(e1);
							result = false;
							JOptionPane.showMessageDialog(thisFrame, "An error occurred parsing the image :(","Parse Error", JOptionPane.ERROR_MESSAGE);
						} 
						imageContainerPanel.setCursor(GuiUtils.CURSOR_DEFAULT);
						txtAreaParsedText.setCursor(GuiUtils.CURSOR_TEXT);
						btnParseImage.setEnabled(true);
						return result;
				    }
				};
				worker.execute();
			}
		});
		
		// INJECT TEXT SELECTED
		btnInjectSelected.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				String parsedText = txtAreaParsedText.getText();
				String outOldTextRef = outputTEXTarea.getText();
				if(parsedText.equals(PARSED_DEFAULT_TEXT)) {
					return;
				}
				
				boolean useAllText = txtAreaParsedText.getSelectedText() == null;
				
				// backup selected index
				int selectionStart = outputTEXTarea.getSelectionStart();
				int selectionEnd = outputTEXTarea.getSelectionEnd();
				String prefix = outOldTextRef.substring(0, selectionStart);
				String postfix = outOldTextRef.substring(selectionEnd);
				
				outputTEXTarea.setText(prefix);
				if(useAllText) {
					outputTEXTarea.append(txtAreaParsedText.getText());
				}else {
					outputTEXTarea.append(txtAreaParsedText.getSelectedText());
				}
				
				// reset selection using 
				outputTEXTarea.append(postfix);
				outputTEXTarea.setSelectionStart(selectionStart);
				outputTEXTarea.setSelectionEnd(selectionStart + parsedText.length());
			}
		});
	}
	
	private void displayImage(File sourceFile) throws IOException {
		Image sourceFileImage = ImageIO.read(sourceFile);
		Image image = ImageGeneralWorker.scaleImage(sourceFileImage, imageContainerPanel.getWidth(), imageContainerPanel.getHeight(), Image.SCALE_REPLICATE);
		labelLoadedImage.setIcon(new ImageIcon(image));
	}
	
}
