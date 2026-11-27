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
package gui.texteditor.commons.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;

import various.common.light.i18n.I18n;

/**
 * Opacity slider shared by the search dialogs (find, replace, search in files): from {@link #MIN}% to 100%, the value chosen is kept
 * while ScreepT is open and is the same for all of them.
 */
public final class DialogOpacity {

	/** Below this the window would be too hard to find */
	public static final int MIN = 35;

	private static int last = 100;

	private DialogOpacity() {
	}

	/**
	 * A window can be translucent only without the system title bar: the dialog uses the one drawn by the Look and Feel
	 * (same buttons, drag and resize). Must run before the dialog is displayable.
	 */
	public static void makeCapable(JDialog dialog) {
		try {
			if (!dialog.isDisplayable()) {
				dialog.setUndecorated(true);
				dialog.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
			}
		} catch (Exception e) {
			// stays a normal dialog, without the transparency
		}
	}

	/** Row "Opacity [slider] 80%" to put at the bottom of the dialog */
	public static JPanel panel(JDialog dialog) {
		JPanel panel = new JPanel(new BorderLayout(8, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
		JLabel title = new JLabel(I18n.t("fsearch.opacity"));
		JLabel value = new JLabel(last + "%");
		value.setPreferredSize(new Dimension(value.getFontMetrics(value.getFont()).stringWidth("100%") + 6, value.getPreferredSize().height));
		JSlider slider = new JSlider(MIN, 100, last);
		String tip = I18n.t("fsearch.opacity_tip");
		title.setToolTipText(tip);
		slider.setToolTipText(tip);
		slider.addChangeListener(e -> {
			last = Math.max(MIN, slider.getValue());
			value.setText(last + "%");
			apply(dialog);
		});
		panel.add(title, BorderLayout.LINE_START);
		panel.add(slider, BorderLayout.CENTER);
		panel.add(value, BorderLayout.LINE_END);
		return panel;
	}

	public static void apply(JDialog dialog) {
		try {
			dialog.setOpacity(Math.max(MIN, last) / 100f);
		} catch (Exception e) {
			// translucency not supported here: the dialog stays opaque
		}
	}

	/** Opacity of the user and a size that shows all the content (texts and fonts change after the first pack) */
	public static void prepareToShow(JDialog dialog) {
		apply(dialog);
		// the root pane is asked (title bar included): the window keeps an old size in its cache after the first pack
		Dimension need = dialog.getRootPane().getPreferredSize();
		java.awt.Insets in = dialog.getInsets();
		need = new Dimension(need.width + in.left + in.right, need.height + in.top + in.bottom);
		Dimension now = dialog.getSize();
		if (now.width < need.width || now.height < need.height) {
			dialog.setSize(Math.max(now.width, need.width), Math.max(now.height, need.height));
		}
	}
}
