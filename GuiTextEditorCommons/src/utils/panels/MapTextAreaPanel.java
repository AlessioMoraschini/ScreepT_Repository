package utils.panels;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;

public class MapTextAreaPanel extends ParentPanel{

	public static MapTextAreaPanel singletonInstance;
	
	private RSyntaxTextArea txtAreaMiniMap;
	private JScrollPane currentScrollPane;
	private ITextAreaDialogs dialogManager;
	
	public MapTextAreaPanel(ITextAreaDialogs dialogManager) {
		this.dialogManager = dialogManager;
		this.currentScrollPane = new JScrollPane();
		this.txtAreaMiniMap = new RSyntaxTextArea();
		
		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"fill, insets 1, hidemode 2", "[200px,grow]", "[270px,grow]"));
		
		add(currentScrollPane, "cell 0 0,grow");
		currentScrollPane.setViewportView(txtAreaMiniMap);
		currentScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		txtAreaMiniMap.setBorder(new EmptyBorder(0,0,0,0));
		setBorder(new EmptyBorder(0,0,0,0));
		currentScrollPane.setBackground(Color.black);
		
		txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
		txtAreaMiniMap.setEditable(false);
		
		txtAreaMiniMap.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				syncReferenceCaretPosition(dialogManager.getTargetTextArea());
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}
	
	public void sync() {
		RSyntaxTextArea reference = dialogManager.getTargetTextArea();
		if (reference != null && txtAreaMiniMap != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Get diyamic reference from interface taken from main editor utils class
					txtAreaMiniMap.setBackground(reference.getBackground());
					RsyntaxUtils.setBackground(txtAreaMiniMap, reference.getBackground());
					txtAreaMiniMap.setForeground(reference.getForeground());
					RsyntaxUtils.setForeground(txtAreaMiniMap, reference.getCaretColor());
					// sync Text and caret position
					txtAreaMiniMap.setText(reference.getText());
					syncCaretPosition(reference);
					txtAreaMiniMap.setCurrentLineHighlightColor(txtAreaMiniMap.getBackground().brighter());
					txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
					dialogManager.loadThemeIntoTarget(txtAreaMiniMap);
					currentScrollPane.setBackground(Color.black);
				}
			});
		}
		
	}
	
	/**
	 * Refresh caret position in minimap textArea
	 */
	public void syncCaretPosition(RSyntaxTextArea reference) {
		if (reference != null) {
			int caretPos = reference.getCaretPosition();
			reference.requestFocus();
			txtAreaMiniMap.setCaretPosition(0);
			txtAreaMiniMap.setCaretPosition(caretPos);
		}
	}
	
	/**
	 * Refresh caret position in target main Text Area
	 */
	public void syncReferenceCaretPosition(RSyntaxTextArea reference) {
		if (reference != null) {
			int caretPos = txtAreaMiniMap.getCaretPosition();
			reference.requestFocus();
			reference.setCaretPosition(0);
			reference.setCaretPosition(caretPos);
		}
	}
	
	public RSyntaxTextArea getTxtAreaMiniMap() {
		return txtAreaMiniMap;
	}

	public void setTxtAreaMiniMap(RSyntaxTextArea txtAreaMiniMap) {
		this.txtAreaMiniMap = txtAreaMiniMap;
	}

	public JScrollPane getCurrentScrollPane() {
		return currentScrollPane;
	}

	public void setCurrentScrollPane(JScrollPane currentScrollPane) {
		this.currentScrollPane = currentScrollPane;
	}

	/**
	 * Get a shared instance of text area, useful if used only one at once
	 */
	public static MapTextAreaPanel getSingletonLazy(ITextAreaDialogs dialogManager) {
		singletonInstance = (singletonInstance == null)? new MapTextAreaPanel(dialogManager) : singletonInstance;
		return singletonInstance;
	}

}
