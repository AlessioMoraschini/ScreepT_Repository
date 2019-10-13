package utils.panels;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import guistyle.GUIStyelSettings;
import net.miginfocom.swing.MigLayout;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import resources.GeneralConfig;
import utils.RsyntaxUtils;
import utils.dialog.ITextAreaDialogs;
import utils.panels.arch.IParentPanel;
import utils.panels.arch.ParentPanel;

public class MapTextAreaPanel extends ParentPanel implements IParentPanel{

	public static MapTextAreaPanel singletonInstance;
	
	private RSyntaxTextArea txtAreaMiniMap;
	private RSyntaxTextArea targetTemp;
	
	private int startTemp = 0;
	private Integer endTemp = null;
	
	public MapTextAreaPanel(final ITextAreaDialogs dialogManager) {
		this.txtAreaMiniMap = new RSyntaxTextArea();
		
		this.currentScrollPane = new JScrollPane();
		setLayout(new MigLayout("fill, insets 1, hidemode 2", "[200px,grow]", "[270px,grow]"));
		
		setBorder(new EmptyBorder(0,0,0,0));

		add(currentScrollPane, "cell 0 0,grow");
		this.dialogManager = dialogManager;
		
		add(currentScrollPane, "cell 0 0,grow");
		currentScrollPane.setViewportView(txtAreaMiniMap);
		currentScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		currentScrollPane.setBackground(Color.black);
		currentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		currentScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		currentScrollPane.getVerticalScrollBar().setUnitIncrement(GeneralConfig.FAST_WHEEL_SPEED);

		txtAreaMiniMap.setBorder(new EmptyBorder(0,0,0,0));
		txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
		txtAreaMiniMap.setAntiAliasingEnabled(true);
		txtAreaMiniMap.setEditable(false);
		GUIStyelSettings.applyNimbusSize(0);
		
		txtAreaMiniMap.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						syncReferenceCaretPosition(startTemp, endTemp);
						GUIStyelSettings.applyNimbusSize(0);
					}
				});
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				// Refresh map content
				String sel = txtAreaMiniMap.getSelectedText();
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if(sel ==  null || sel.equals("")) {
							startTemp = txtAreaMiniMap.getCaretPosition();
						}else {
							startTemp = txtAreaMiniMap.getSelectionStart();
							endTemp = txtAreaMiniMap.getSelectionEnd();
						}

						targetTemp = dialogManager.getTargetTextArea();
						synchronized (targetTemp) {
							txtAreaMiniMap.setText(targetTemp.getText());
						}
						
						syncCaretPosition(startTemp, endTemp);
					}
				});

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
					txtAreaMiniMap.setCurrentLineHighlightColor(txtAreaMiniMap.getBackground().brighter());
					txtAreaMiniMap.setFont(GeneralConfig.TXT_EDITOR_MINIMAP_FONT);
					dialogManager.loadThemeIntoTarget(txtAreaMiniMap);
					currentScrollPane.setBackground(Color.black);

					safeSyncCaretFromTxtReference(reference);
				}
			});
		}
		
	}
	
	public void safeSyncCaretToTxtReference() {
		String sel = txtAreaMiniMap.getSelectedText();
		if(sel ==  null || sel.equals("")) {
			syncReferenceCaretPosition(txtAreaMiniMap.getCaretPosition(), null);
		}else {
			syncReferenceCaretPosition(txtAreaMiniMap.getSelectionStart(), txtAreaMiniMap.getSelectionEnd());
		}
	}

	public void safeSyncCaretFromTxtReference(JTextArea txtArea) {
		String sel = txtArea.getSelectedText();
		if(sel ==  null || sel.equals("")) {
			syncCaretPosition(txtArea.getCaretPosition(), null);
		}else {
			syncCaretPosition(txtArea.getSelectionStart(), txtArea.getSelectionEnd());
		}
	}
	
	/**
	 * Refresh caret position in minimap textArea (if end is null then it will be setted as caret position)
	 */
	public void syncCaretPosition(int start, Integer end) {
		try {
			
			if (end == null) {
				txtAreaMiniMap.setCaretPosition(0);
				txtAreaMiniMap.setCaretPosition(start);
			}else {
				txtAreaMiniMap.setSelectionStart(start);
				txtAreaMiniMap.setSelectionEnd(end);
			}
		} catch (Exception e) {
			logger.warn("ERROR setting caret position into textArea miniMap");
		}
	}
	
	/**
	 * Refresh caret position in target main Text Area
	 */
	public void syncReferenceCaretPosition(int start, Integer end) {
		JTextArea reference = dialogManager.getTargetTextArea();
		try {
			
			if (end == null) {
				reference.setCaretPosition(0);
				reference.setCaretPosition(start);
			}else {
				reference.setSelectionStart(start);
				reference.setSelectionEnd(end);
			}
		} catch (Exception e) {
			logger.warn("ERROR setting caret position into textArea miniMap");
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
