package gUI.utils.filedialog;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.border.LineBorder;
import java.awt.SystemColor;
import net.miginfocom.swing.MigLayout;
import uTILS.HandleString;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import gUI.om.Templates_Enum;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TemplatesChoosePanel extends JFrame{

	public TemplatesChoosePanel(JTextArea output, JComboBox templatesCombo) {
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[30px,grow][100px,grow][100px,grow][30px,grow]", "[42px][42px,grow]"));
		
		JPanel panel = new JPanel();
		panel.setForeground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.GRAY, 4));
		panel.setBackground(Color.BLACK);
		getContentPane().add(panel, "cell 0 0 4 1,growx,aligny top");
		panel.setLayout(new MigLayout("", "[][][][][][]", "[]"));
		
		JLabel lblTemplates = new JLabel("Templates");
		lblTemplates.setHorizontalAlignment(SwingConstants.CENTER);
		lblTemplates.setForeground(Color.LIGHT_GRAY);
		lblTemplates.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblTemplates.setBackground(SystemColor.activeCaptionBorder);
		panel.add(lblTemplates, "cell 5 0,alignx center");
		
		JComboBox comboTemplateSelector = new JComboBox();
		comboTemplateSelector.setModel(new DefaultComboBoxModel(Templates_Enum.values()));
		getContentPane().add(comboTemplateSelector, "cell 0 1 2 1,alignx center");
		
		JButton btnInject = new JButton("Inject template");
		btnInject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedTemplate = "\n\n"+((Templates_Enum)comboTemplateSelector.getSelectedItem()).value + "\n\n";
				String extTemplate = ((Templates_Enum)comboTemplateSelector.getSelectedItem()).type_ext;
				String modified = HandleString.injectStringInSelection(output.getText(), selectedTemplate, output.getSelectionStart(), output.getSelectionEnd());
				output.setText(modified);
			}
		});
		btnInject.setBackground(new Color(169, 169, 169));
		getContentPane().add(btnInject, "cell 2 1 2 1,alignx center");
		
	}
	
}
