/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.panels.crypter;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import screept.crypter.engine.CipherAlgorithm;
import screept.crypter.engine.CipherLayer;
import screept.crypter.engine.CryptoProfile;
import screept.crypter.engine.FileCrypter;
import screept.crypter.engine.KdfAlgorithm;
import screept.gui.frames.OptionDescriptions;

/**
 * "Custom protection": the user stacks the layers (always applied in the order shown), picks the key derivation
 * and how folders are exported. The summary at the bottom shows the result live and explains what is not valid.
 */
public final class CrypterAdvancedDialog {

	private CrypterAdvancedDialog() {
	}

	/** @return true if the user confirmed (settings are updated), false if cancelled */
	public static boolean show(Component parent, CrypterSettings settings) {

		final CipherLayer[] order = { CipherAlgorithm.AES_128_GCM, CipherAlgorithm.AES_256_GCM, CipherAlgorithm.CHACHA20_POLY1305,
				CipherAlgorithm.STEGA_CREEPT, CipherAlgorithm.SHUFFLE };
		final String[] names = { "AES-128", "AES-256", "ChaCha20-Poly1305", "StegaCREEPT key image", "Shuffle" };
		final JCheckBox[] boxes = new JCheckBox[order.length];
		for (int i = 0; i < order.length; i++) {
			boxes[i] = new JCheckBox(names[i], settings.layers.contains(order[i]));
			boxes[i].setToolTipText(html(OptionDescriptions.crypterOption(names[i])));
		}

		final JComboBox<KdfAlgorithm> kdf = new JComboBox<KdfAlgorithm>(KdfAlgorithm.values());
		kdf.setSelectedItem(settings.kdf);
		kdf.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				return super.getListCellRendererComponent(l, CrypterSettings.kdfLabel((KdfAlgorithm) v), i, s, f);
			}
		});
		kdf.setToolTipText(html(OptionDescriptions.crypterOption("Key derivation")));

		final JComboBox<FileCrypter.OutputMode> folder = new JComboBox<FileCrypter.OutputMode>(FileCrypter.OutputMode.values());
		folder.setSelectedItem(settings.outputMode);
		folder.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
				return super.getListCellRendererComponent(l, CrypterSettings.outputModeLabel((FileCrypter.OutputMode) v), i, s, f);
			}
		});
		folder.setToolTipText(html(OptionDescriptions.crypterOption("Folders")));

		final JLabel summary = new JLabel(" ");
		Runnable refresh = new Runnable() {
			@Override
			public void run() {
				try {
					CrypterSettings tmp = fill(new CrypterSettings(), order, boxes, kdf, folder);
					CryptoProfile p = tmp.buildProfile();
					summary.setForeground(new Color(107, 219, 105));
					summary.setText(html("Result: " + p.describe() + (p.needsKeyImage() ? " (a key image is needed)" : "")));
				} catch (IllegalArgumentException ex) {
					summary.setForeground(new Color(255, 140, 90));
					summary.setText(html("Pick at least one AES or ChaCha20 layer: Shuffle alone does not protect anything."));
				}
			}
		};
		java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				refresh.run();
			}
		};
		for (JCheckBox b : boxes) {
			b.addActionListener(al);
		}
		kdf.addActionListener(al);
		refresh.run();

		JPanel p = new JPanel(new MigLayout("insets 10, gapy 6, wrap 1, fillx", "[grow, fill]"));
		p.add(hint("It starts from the mode selected in the list: change what you need and press OK, the mode becomes Custom."));
		p.add(section("Layers, applied top to bottom"), "gaptop 6");
		for (JCheckBox b : boxes) {
			p.add(b, "gapleft 12");
		}
		p.add(section("Password to key"), "gaptop 8");
		p.add(kdf);
		p.add(section("Folders"), "gaptop 8");
		p.add(folder);
		p.add(summary, "gaptop 10, wmin 380");

		while (true) {
			int r = JOptionPane.showConfirmDialog(parent, p, "Custom protection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (r != JOptionPane.OK_OPTION) {
				return false;
			}
			try {
				CrypterSettings chosen = fill(new CrypterSettings(), order, boxes, kdf, folder);
				chosen.buildProfile(); // validates
				settings.layers = chosen.layers;
				settings.kdf = chosen.kdf;
				settings.outputMode = chosen.outputMode;
				return true;
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(parent, "Pick at least one AES or ChaCha20 layer: Shuffle alone does not protect anything.",
						"Custom protection", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private static CrypterSettings fill(CrypterSettings s, CipherLayer[] order, JCheckBox[] boxes, JComboBox<KdfAlgorithm> kdf,
			JComboBox<FileCrypter.OutputMode> folder) {
		List<CipherLayer> layers = new ArrayList<CipherLayer>();
		for (int i = 0; i < order.length; i++) {
			if (boxes[i].isSelected()) {
				layers.add(order[i]);
			}
		}
		s.layers = layers;
		s.kdf = (KdfAlgorithm) kdf.getSelectedItem();
		s.outputMode = (FileCrypter.OutputMode) folder.getSelectedItem();
		return s;
	}

	private static JLabel hint(String text) {
		JLabel l = new JLabel(html(text));
		l.setForeground(new Color(160, 168, 176));
		return l;
	}

	private static JLabel section(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD));
		return l;
	}

	private static String html(String text) {
		String t = text == null ? "" : text;
		return "<html><body style='width:340px'>" + t.replace("&", "&amp;").replace("<", "&lt;") + "</body></html>";
	}
}
