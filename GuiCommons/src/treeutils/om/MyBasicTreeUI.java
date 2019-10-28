package treeutils.om;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;

import treeutils.TreeUtils;

public class MyBasicTreeUI extends BasicTreeUI
{
  @Override
  public Icon getExpandedIcon()
  {
    return null;
  }

  @Override
  public Icon getCollapsedIcon()
  {
    return null;
  }

  @Override
  protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom)
  {}

  @Override
  protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right)
  {}

  @Override
  protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf)
  {
    if(tree.isRowSelected(row))
    {
      g.setColor(TreeUtils.DEF_BACK_SELECTION);
      g.fillRect(0, row * tree.getRowHeight(), tree.getWidth(), tree.getRowHeight());
    }

    super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
  }
}