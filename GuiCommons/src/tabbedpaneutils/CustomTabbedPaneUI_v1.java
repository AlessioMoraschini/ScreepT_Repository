package tabbedpaneutils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTabbedPane;

public class CustomTabbedPaneUI_v1 extends javax.swing.plaf.basic.BasicTabbedPaneUI {

	public static final Color DEFAULT_FILLING = Color.lightGray;
	public static final Color DEFAULT_DARK_FILLING = DEFAULT_FILLING.darker();
	public static final Color DEFAULT_BORDER = Color.DARK_GRAY;
	
	private static final int xGap = -2;
	private static final int yGap = -2;
	private static final int wPad = 5;
	private static final int hPad = 2;
	private static final int arch = 8;
	
	public Color fillingColor;
	public Color deSelfillingColor;
	public Color borderColor;
	public JTabbedPane mainTabPane;
	
	public CustomTabbedPaneUI_v1(Color filling, Color deselectedFilling, Color border, JTabbedPane mainPaneTabbed) {
		super();
		fillingColor = (filling != null)? filling : DEFAULT_FILLING;
		deSelfillingColor = (deselectedFilling != null)? deselectedFilling : DEFAULT_DARK_FILLING;
		borderColor = (border != null)? border : DEFAULT_BORDER;
		mainTabPane = mainPaneTabbed;
	}
	
    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, 
               int tabIndex, Rectangle iconRect, Rectangle textRect) {
        Color savedColor = g.getColor();
        if(mainTabPane.getSelectedIndex() == tabIndex) {
        	g.setColor(fillingColor);
        }else{
        	g.setColor(deSelfillingColor);
        }
        g.fillRoundRect(rects[tabIndex].x+xGap, rects[tabIndex].y+yGap, 
               rects[tabIndex].width+wPad, rects[tabIndex].height+hPad, arch, arch);
        g.setColor(borderColor);
        g.drawRoundRect(rects[tabIndex].x+xGap, rects[tabIndex].y+yGap, 
               rects[tabIndex].width+wPad, rects[tabIndex].height+hPad, arch, arch);
        g.setColor(savedColor);
    }
 }
