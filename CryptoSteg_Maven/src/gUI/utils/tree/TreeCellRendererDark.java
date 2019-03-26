package gUI.utils.tree;

import java.awt.Color;

import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeCellRendererDark extends DefaultTreeCellRenderer{


    @Override
    public Color getBackgroundNonSelectionColor() {
        return (null);
    }

    @Override
    public Color getBackgroundSelectionColor() {
        return Color.BLACK;
    }

    @Override
    public Color getBackground() {
        return (null);
    }
    
    @Override
    public Color getForeground() {
        return Color.LIGHT_GRAY;
    }

//    @Override
//    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
//        final Component ret = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
//
//        final DefaultMutableTreeNode node = ((DefaultMutableTreeNode) (value));
//        this.setText(value.toString());
//        return ret;
//    
//	}
	
}
