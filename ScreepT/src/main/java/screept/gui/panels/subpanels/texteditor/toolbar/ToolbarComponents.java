/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini)
 * =========================================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package screept.gui.panels.subpanels.texteditor.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.formdev.flatlaf.util.UIScale;

/**
 * Flat, self painted components of the file editor toolbar: they look the same with every look and feel, never take the
 * keyboard focus away from the text area, and show hover / pressed / selected states with rounded backgrounds.
 */
public final class ToolbarComponents {

	private ToolbarComponents() {
	}

	/** Icon only push button */
	public static class Button extends JButton {
		private static final long serialVersionUID = 1L;

		public Button(EditorIcons.Kind kind, String tooltip) {
			super(EditorIcons.of(kind, ToolbarStyle.ICON_SIZE));
			setToolTipText(tooltip);
			ToolbarStyle.setup(this);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			ToolbarStyle.setup(this);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(ToolbarStyle.preferredWidth(this, null, false), UIScale.scale(ToolbarStyle.BUTTON_SIZE));
		}

		@Override
		protected void paintComponent(Graphics g) {
			ToolbarStyle.paintButton(this, g, null, false, false);
		}

		@Override
		public void paintBorder(Graphics g) {
			// no border
		}
	}

	/** Icon only on/off button (selected = panel visible, option active...) */
	public static class Toggle extends JToggleButton {
		private static final long serialVersionUID = 1L;

		public Toggle(EditorIcons.Kind kind, String tooltip) {
			super(EditorIcons.of(kind, ToolbarStyle.ICON_SIZE));
			setToolTipText(tooltip);
			ToolbarStyle.setup(this);
		}

		@Override
		public void updateUI() {
			super.updateUI();
			ToolbarStyle.setup(this);
		}

		/** The state is the selection: older code colors these toggles by hand, that is ignored here */
		@Override
		public void setBackground(Color bg) {
			// the selected state is painted by the toolbar style
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(ToolbarStyle.preferredWidth(this, null, false), UIScale.scale(ToolbarStyle.BUTTON_SIZE));
		}

		@Override
		protected void paintComponent(Graphics g) {
			ToolbarStyle.paintButton(this, g, null, false, false);
		}

		@Override
		public void paintBorder(Graphics g) {
			// no border
		}
	}

	/** Button with icon, label and chevron that opens a drop down menu built every time it is opened (so the states are always current) */
	public static class MenuButton extends JButton {
		private static final long serialVersionUID = 1L;

		private final Supplier<JPopupMenu> menuSupplier;
		private boolean popupOpen;

		public MenuButton(EditorIcons.Kind kind, String label, String tooltip, Supplier<JPopupMenu> menuSupplier) {
			super(label, EditorIcons.of(kind, ToolbarStyle.ICON_SIZE));
			this.menuSupplier = menuSupplier;
			setToolTipText(tooltip);
			ToolbarStyle.setup(this);
			addActionListener(e -> {
				if (popupOpen) {
					return;
				}
				JPopupMenu menu = this.menuSupplier.get();
				menu.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						popupOpen = true;
						repaint();
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
						// the click that closes the menu must not reopen it
						javax.swing.Timer timer = new javax.swing.Timer(150, ev -> {
							popupOpen = false;
							repaint();
						});
						timer.setRepeats(false);
						timer.start();
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
						// handled by popupMenuWillBecomeInvisible
					}
				});
				menu.show(this, 0, getHeight());
			});
		}

		@Override
		public void updateUI() {
			super.updateUI();
			ToolbarStyle.setup(this);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(ToolbarStyle.preferredWidth(this, getText(), true), UIScale.scale(ToolbarStyle.BUTTON_SIZE));
		}

		@Override
		protected void paintComponent(Graphics g) {
			ToolbarStyle.paintButton(this, g, getText(), true, popupOpen);
		}

		@Override
		public void paintBorder(Graphics g) {
			// no border
		}
	}

	/** Thin vertical line between two groups of the toolbar */
	public static class Separator extends JComponent {
		private static final long serialVersionUID = 1L;

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(UIScale.scale(7), UIScale.scale(ToolbarStyle.BUTTON_SIZE));
		}

		@Override
		protected void paintComponent(Graphics g) {
			ToolbarStyle.drawSeparator(g, getWidth(), getHeight());
		}
	}
}
