package splitpanel;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import utility.reflection.ReflectionUtils;


public class SplitPaneUtils {

	public static final Color defaultBackgroundDivider = new Color(41,49,52).darker();
	
	public static void resetAllDIviderColors(final Object targetClassInstance, Color target) {
				// Reset all divider colors
				try {
					for (JSplitPane i : ReflectionUtils.retrieveFieldsByType(targetClassInstance, JSplitPane.class)) {
						SplitPaneUtils.setDividerColors(target, Color.BLACK, i);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
	}
	
	public static void setDividerColors(Color back, Color border, JSplitPane panel) {
		panel.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
	            return new BasicSplitPaneDivider(this) {
					private static final long serialVersionUID = -4392834503356984099L;

					public void setBorder(Border b) {
	                }
	
	                @Override
	                    public void paint(Graphics g) {
		            		if(back == null) {
		            			g.setColor(defaultBackgroundDivider);
		            		}else {
		            			g.setColor(back);
		            		}
		            		g.fillRect(-3, -2, getSize().width+8, getSize().height+8);
	                        super.paint(g);
	                    }
	            };
            }
        });
        panel.setBorder(new LineBorder(border));
	}

	public static void hideDivider(JSplitPane panel) {

		panel.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
	            return new BasicSplitPaneDivider(this) {
					private static final long serialVersionUID = 1396997974252737249L;

					public void setBorder(Border b) {
	                }
	
	                @Override
	                public void paint(Graphics g) {
	                }
	            };
            }
        });
	}
	
}
