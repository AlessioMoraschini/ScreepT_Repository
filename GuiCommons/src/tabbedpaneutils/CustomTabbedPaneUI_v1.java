package tabbedpaneutils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class CustomTabbedPaneUI_v1 extends javax.swing.plaf.basic.BasicTabbedPaneUI {

	public static final Color DEFAULT_FILLING = Color.lightGray;
	public static final Color DEFAULT_DARK_FILLING = DEFAULT_FILLING.darker();
	public static final Color DEFAULT_BORDER = Color.DARK_GRAY;
	public static final Color ACTIVE_BTN_CLOSE_COL = new Color(180, 10, 0);
	public static final Color INACTIVE_BTN_CLOSE_COL = new Color(40, 0, 0);
	
	public static final int THICKNESS_BORDER = 2;
	
	private static final int xGap = -1;
	private static final int yGap = -2;
	private static final int wPad = 2;
	private static final int hPad = 4;
	private static final int arch = 10;
	
	public Color fillingColor;
	public Color deSelfillingColor;
	public Color borderColor;
	public JTabbedPane mainTabPane;
	public int buttonOrdinalPos;
	
	/**
	 * Use this if there is close button
	 */
	public CustomTabbedPaneUI_v1(Color filling, Color deselectedFilling, Color border, JTabbedPane mainPaneTabbed, int closeBtnPos) {
		super();
		buttonOrdinalPos = closeBtnPos;
		fillingColor = (filling != null)? filling : DEFAULT_FILLING;
		deSelfillingColor = (deselectedFilling != null)? deselectedFilling : DEFAULT_DARK_FILLING;
		borderColor = (border != null)? border : DEFAULT_BORDER;
		mainTabPane = mainPaneTabbed;
	}
	
	/**
	 * Use this if there is no close button
	 */
	public CustomTabbedPaneUI_v1(Color filling, Color deselectedFilling, Color border, JTabbedPane mainPaneTabbed) {
		super();
		buttonOrdinalPos = Integer.MAX_VALUE;
		fillingColor = (filling != null)? filling : DEFAULT_FILLING;
		deSelfillingColor = (deselectedFilling != null)? deselectedFilling : DEFAULT_DARK_FILLING;
		borderColor = (border != null)? border : DEFAULT_BORDER;
		mainTabPane = mainPaneTabbed;
	}
	
    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, 
               int tabIndex, Rectangle iconRect, Rectangle textRect) {
    	int selectedIndex = mainTabPane.getSelectedIndex();
    	Color savedColor = g.getColor();
    	Graphics2D g2d = (Graphics2D)g;
        int x = rects[tabIndex].x+xGap;
        int y = rects[tabIndex].y+yGap;
        
        JButton currentButton = getCloseButton(tabIndex, buttonOrdinalPos);
        if (buttonOrdinalPos != Integer.MAX_VALUE && currentButton != null) {
			if (selectedIndex == tabIndex) {
				g.setColor(fillingColor);
				currentButton.setBackground(ACTIVE_BTN_CLOSE_COL);
			} else {
				g.setColor(deSelfillingColor);
				currentButton.setBackground(INACTIVE_BTN_CLOSE_COL);
			} 
		}
		g.fillRoundRect(x, y, rects[tabIndex].width+wPad, rects[tabIndex].height+hPad, arch, arch);
        
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(THICKNESS_BORDER));
        g2d.drawRoundRect(x, y, rects[tabIndex].width+wPad, rects[tabIndex].height+hPad, arch, arch);
        g2d.setColor(savedColor);
        
    }
    
    public JButton getCloseButton(int index, int buttonOrdinalPosition) {
    	JButton buttonCloseTab = null; 
    	try {
			buttonCloseTab = (JButton)(getPanelFromTabHeader(index)).getComponent(buttonOrdinalPosition);
		} catch (Exception e) {
		}
    	return buttonCloseTab;
    }
    
    public JPanel getPanelFromTabHeader(int index) {
		JPanel tabPanel = null;
		try {
			tabPanel = (JPanel) mainTabPane.getTabComponentAt(index);
		} catch (Exception e) {
		}
		return tabPanel;
	}
 }
