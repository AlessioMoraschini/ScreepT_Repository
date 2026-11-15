package screept.gui.frames;

import java.awt.Dialog;
import java.awt.Window;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.commons.imagestudio.DialogFit;
import initializer.configs.impl.INItializer;
import net.miginfocom.swing.MigLayout;
import various.common.light.i18n.I18n;

/** Small modal window to choose the language of the whole application: the choice is applied at once and remembered */
public class LanguageDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(LanguageDialog.class);

	private String chosen;

	public LanguageDialog(Window owner, INItializer configuration) {
		super(owner, "Select language", Dialog.ModalityType.APPLICATION_MODAL);
		chosen = I18n.getLanguage();

		JPanel content = new JPanel(new MigLayout("insets 16 18 14 18, wrap 1, gapy 8, hidemode 3", "[grow,fill,260:n:n]", ""));
		content.add(ToolStyle.header("Language", "Choose the language of the application"), "gapbottom 4");

		ButtonGroup group = new ButtonGroup();
		for (I18n.Language language : I18n.LANGUAGES) {
			JRadioButton radio = new JRadioButton(language.nativeName);
			radio.setSelected(language.code.equals(chosen));
			ToolStyle.hand(radio);
			radio.addActionListener(e -> chosen = language.code);
			radio.setFont(radio.getFont().deriveFont(radio.getFont().getSize2D() + 1f));
			group.add(radio);
			content.add(radio, "gapleft 6");
		}

		content.add(ToolStyle.mutedLabel("Some texts may stay in English until translated"), "gaptop 6, wmin 0");

		JButton ok = ToolStyle.button("OK", null, null);
		JButton cancel = ToolStyle.button("Cancel", null, null);
		JPanel buttons = new JPanel(new MigLayout("insets 0, gapx 8", "push[][]", ""));
		buttons.setOpaque(false);
		buttons.add(cancel);
		buttons.add(ok);
		content.add(buttons, "gaptop 6, growx");

		ok.addActionListener(e -> {
			apply(configuration);
			dispose();
		});
		cancel.addActionListener(e -> dispose()); // cancelled by user
		getRootPane().setDefaultButton(ok);
		ToolStyle.bindKey(this, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "closeLanguage", this::dispose);

		setContentPane(content);
		DialogFit.apply(this, owner);
		setLocationRelativeTo(owner);
	}

	private void apply(INItializer configuration) {
		if (chosen == null || chosen.equals(I18n.getLanguage())) {
			return;
		}
		I18n.setLanguage(chosen);
		if (configuration != null) {
			try {
				configuration.getGenOpt().setLanguage(chosen);
				configuration.writeCurrentConfigIniFile();
			} catch (Exception e) {
				logger.warn("Cannot remember the language", e);
			}
		}
	}

	public static void open(Window owner, INItializer configuration) {
		new LanguageDialog(owner, configuration).setVisible(true);
	}
}
