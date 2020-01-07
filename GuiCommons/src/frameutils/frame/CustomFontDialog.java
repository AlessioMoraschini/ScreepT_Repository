package frameutils.frame;

import javax.swing.*;

import org.drjekyll.fontchooser.FontChooser;
import org.drjekyll.fontchooser.FontDialog;
import org.drjekyll.fontchooser.util.ResourceBundleUtil;

import frameutils.utils.GuiUtilsExtended;
import guistyle.GUIStyelSettingsExtended;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

/**
 * A Customization of the original one by Christos Bohoris. 
 *
 * @author Alessio Moraschini
 */
public class CustomFontDialog  extends FontDialog {
	private static final long serialVersionUID = -2535872755943674977L;

		public static CustomFontDialog getDialog(Component component) {
	    	 CustomFontDialog dialog = new CustomFontDialog((Frame) null, "Select Font", true);
	         dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	         dialog.setSelectedFont(component.getFont());
	         dialog.setVisible(true);
	         GuiUtilsExtended.centerComponent(dialog, 600, 450);
	         if (!dialog.isCancelSelected()) {
	             component.setFont(dialog.getSelectedFont());
	         }
	         
	         GUIStyelSettingsExtended.applyNimbusSize(0, dialog);
	         
	         return dialog;
	     }

	     private final FontChooser chooser = new FontChooser();
	     private final JButton cancelButton = new JButton();
	     private final JButton okButton = new JButton();
	     private final ResourceBundle bundle = ResourceBundle.getBundle("FontDialog");
	     private final ResourceBundleUtil resourceBundleUtil = new ResourceBundleUtil(bundle);

	     public CustomFontDialog() {
	         initDialog();
	     }

	     public CustomFontDialog(Frame owner) {
	         super(owner);
	         initDialog();
	     }

	     public CustomFontDialog(Frame owner, boolean modal) {
	         super(owner, modal);
	         initDialog();
	     }

	     public CustomFontDialog(Frame owner, String title) {
	         super(owner, title);
	         initDialog();
	     }

	     public CustomFontDialog(Frame owner, String title, boolean modal) {
	         super(owner, title, modal);
	         initDialog();
	     }

	     public CustomFontDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
	         super(owner, title, modal, gc);
	         initDialog();
	     }

	     public CustomFontDialog(Dialog owner) {
	         super(owner);
	         initDialog();
	     }

	     public CustomFontDialog(Dialog owner, boolean modal) {
	         super(owner, modal);
	         initDialog();
	     }

	     public CustomFontDialog(Dialog owner, String title) {
	         super(owner, title);
	         initDialog();
	     }

	     public CustomFontDialog(Dialog owner, String title, boolean modal) {
	         super(owner, title, modal);
	         initDialog();
	     }

	     public CustomFontDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
	         super(owner, title, modal, gc);
	         initDialog();
	     }

	     public CustomFontDialog(Window owner) {
	         super(owner);
	         initDialog();
	     }

	     public CustomFontDialog(Window owner, ModalityType modalityType) {
	         super(owner, modalityType);
	         initDialog();
	     }

	     public CustomFontDialog(Window owner, String title) {
	         super(owner, title);
	         initDialog();
	     }

	     public CustomFontDialog(Window owner, String title, ModalityType modalityType) {
	         super(owner, title, modalityType);
	         initDialog();
	     }

	     public CustomFontDialog(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
	         super(owner, title, modalityType, gc);
	         initDialog();
	     }

	     private boolean cancelSelected;

	     private void initDialog() {
	         initComponents();
	         getRootPane().setDefaultButton(okButton);

	         cancelButton.addActionListener(event -> cancelSelected = true);
	         addWindowListener(new WindowAdapter() {
	             @Override
	             public void windowClosing(WindowEvent e) {
	                 cancelSelected = true;
	             }
	         });
	     }

	     private void initComponents() {

	         JPanel chooserPanel = new JPanel();
	         chooserPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 11));
	         chooserPanel.setLayout(new BorderLayout(0, 12));
	         chooserPanel.add(chooser);
	         add(chooserPanel);

	         JPanel controlPanel = new JPanel();
	         controlPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 6, 6));
	         controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
	         add(controlPanel, BorderLayout.PAGE_END);

	         okButton.setMnemonic(resourceBundleUtil.getFirstChar("action.ok.mnemonic"));
	         okButton.setText(bundle.getString("action.ok"));
	         okButton.addActionListener(event -> {
	             dispose();
	         });
	         controlPanel.add(okButton);

	         cancelButton.setMnemonic(resourceBundleUtil.getFirstChar("action.cancel.mnemonic"));
	         cancelButton.setText(bundle.getString("action.cancel"));
	         cancelButton.addActionListener(event -> {
	             cancelSelected = true;
	             dispose();
	         });
	         controlPanel.add(cancelButton);

	         pack();
	     }

	     public Font getSelectedFont() {
	         return chooser.getSelectedFont();
	     }

	     public void setSelectedFont(Font font) {
	         chooser.setSelectedFont(font);
	     }

	     public boolean isCancelSelected() {
	         return cancelSelected;
	     }
	     
	     public static void setDefaultLookAndFeelDecorated(boolean value) {
	    	 FontDialog.setDefaultLookAndFeelDecorated(value);
	     }
	 }
