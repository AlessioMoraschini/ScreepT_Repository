package splitpanel;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;

public class SplitPaneUtils {
	public static void setDividerColors(Color back, Color border, JSplitPane panel) {

		panel.setUI(new BasicSplitPaneUI() {
            public BasicSplitPaneDivider createDefaultDivider() {
            return new BasicSplitPaneDivider(this) {
                public void setBorder(Border b) {
                }

                @Override
                    public void paint(Graphics g) {
                    g.setColor(back);
                    g.fillRoundRect(0, -5, getSize().width, getSize().height+10, 20, 20);
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
