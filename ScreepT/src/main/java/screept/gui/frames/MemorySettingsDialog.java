/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
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
package screept.gui.frames;

import various.common.light.i18n.I18n;
import java.awt.Window;
import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import gui.commons.frameutils.frame.arch.ToolStyle;
import maingUI.MainGuiManager;
import net.miginfocom.swing.MigLayout;
import screept.general.utils.LauncherIniOptions;

/**
 * Small window to change the maximum memory (Java heap) of ScreepT. The value goes in the launcher settings
 * ({@code memoryArgs}) and applies at the next start: after saving it asks whether to restart now.
 * The choice goes from the minimum configured in the launcher (its initial heap, at least 256 MB) to the most the JVM and
 * the PC can give right now (free memory plus what ScreepT holds, which is released at the restart).
 */
public final class MemorySettingsDialog {

	private static final long MB = 1024L * 1024L;
	private static final int STEP = 64;
	private static final Pattern XMX = Pattern.compile("-Xmx(\\d+)([mMgG])");
	private static final Pattern XMS = Pattern.compile("-Xms(\\d+)([mMgG])");

	private MemorySettingsDialog() {
	}

	private static long toMb(Matcher m) {
		long v = Long.parseLong(m.group(1));
		return m.group(2).equalsIgnoreCase("g") ? v * 1024 : v;
	}

	/** Opens the window (modal) and, if the value is saved, asks about the restart */
	public static void show(Window owner) {
		Runtime runtime = Runtime.getRuntime();
		long runningMb = runtime.maxMemory() / MB;
		String savedArgs = LauncherIniOptions.getMemoryArgs();
		if (savedArgs == null) {
			javax.swing.JOptionPane.showMessageDialog(owner, I18n.t("memory.the_launcher_settings_file_was_not"), I18n.t("memory.maximum_memory"),
					javax.swing.JOptionPane.WARNING_MESSAGE);
			return;
		}
		Matcher mx = XMX.matcher(savedArgs);
		Matcher ms = XMS.matcher(savedArgs);
		final long savedMb = mx.find() ? toMb(mx) : runningMb;
		final long initialMb = ms.find() ? toMb(ms) : 0;

		// limits: the JVM and the free memory of the PC right now
		long totalMb = 0;
		long freeMb = 0;
		java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		if (os instanceof com.sun.management.OperatingSystemMXBean) {
			com.sun.management.OperatingSystemMXBean sun = (com.sun.management.OperatingSystemMXBean) os;
			totalMb = sun.getTotalPhysicalMemorySize() / MB;
			freeMb = sun.getFreePhysicalMemorySize() / MB;
		}
		boolean jvm64 = !"32".equals(System.getProperty("sun.arch.data.model"));
		long heldMb = runtime.totalMemory() / MB;
		long maxMb = totalMb > 0 ? Math.min(freeMb + heldMb, totalMb * 9 / 10) : Math.max(savedMb, 4096);
		if (!jvm64) {
			maxMb = Math.min(maxMb, 1400);
		}
		final long minMb = Math.max(256, initialMb);
		maxMb = Math.max(minMb, maxMb / STEP * STEP);
		final long limitMax = maxMb;
		final long totalPc = totalMb;
		final long freeNow = freeMb + heldMb;

		final JDialog dialog = new JDialog(owner, I18n.t("memory.maximum_memory_of_screept"), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new MigLayout("insets 16, gapy 8, wrap 1", "[grow,fill]", ""));
		dialog.add(ToolStyle.caption(I18n.t("memory.maximum_memory_java_heap")));
		dialog.add(new JLabel(I18n.t("memory.running_now") + runningMb + I18n.t("memory.mb_nbsp_nbsp_saved_in_the") + savedMb + I18n.t("memory.mb_choose_from_the_minimum_set")));

		long start = Math.max(minMb, Math.min(limitMax, savedMb));
		final JSlider slider = new JSlider((int) minMb, (int) limitMax, (int) start);
		slider.setMajorTickSpacing((int) Math.max(STEP, (limitMax - minMb) / 4 / STEP * STEP));
		slider.setPaintTicks(true);
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel((int) start, (int) minMb, (int) limitMax, STEP));
		final JLabel hint = ToolStyle.mutedLabel(" ");
		final JButton save = ToolStyle.accent(ToolStyle.button(I18n.t("common.save"), null, I18n.t("memory.write_the_value_in_the_launcher")), ToolStyle.GREEN);

		final Runnable update = new Runnable() {
			@Override
			public void run() {
				int v = slider.getValue();
				String text;
				if (totalPc > 0 && v > totalPc * 3 / 4) {
					text = I18n.t("memory.more_than_75_of_the_pc");
				} else if (v > freeNow) {
					text = I18n.t("memory.more_than_the_memory_free_now");
				} else {
					text = v == (int) limitMax ? I18n.t("memory.this_is_the_most_available_right") : v == (int) minMb ? I18n.t("memory.this_is_the_minimum_set_in") : " ";
				}
				hint.setText(text);
				save.setEnabled(v != savedMb);
			}
		};
		slider.addChangeListener(e -> {
			if (((Number) spinner.getValue()).intValue() != slider.getValue()) {
				spinner.setValue(slider.getValue());
			}
			update.run();
		});
		spinner.addChangeListener(e -> {
			int v = ((Number) spinner.getValue()).intValue();
			if (slider.getValue() != v) {
				slider.setValue(v);
			}
		});

		dialog.add(slider, "growx");
		javax.swing.JPanel row = new javax.swing.JPanel(new MigLayout("insets 0, gapx 8", "[][80!][]push[][][]", "[]"));
		row.setOpaque(false);
		row.add(new JLabel("MB"));
		row.add(spinner);
		row.add(new JLabel());
		JButton bMin = ToolStyle.button("Minimum", null, I18n.t("memory.the_minimum_set_in_the_launcher"));
		bMin.addActionListener(e -> slider.setValue((int) minMb));
		JButton bRec = ToolStyle.button("Recommended", null, I18n.t("memory.a_quarter_of_the_pc_memory"));
		bRec.addActionListener(e -> {
			long rec = totalPc > 0 ? Math.min(limitMax, Math.max(minMb, totalPc / 4)) : savedMb;
			slider.setValue((int) (rec / STEP * STEP));
		});
		JButton bMax = ToolStyle.button("Maximum", null, I18n.t("memory.the_most_the_jvm_and_the"));
		bMax.addActionListener(e -> slider.setValue((int) limitMax));
		row.add(bMin);
		row.add(bRec);
		row.add(bMax);
		dialog.add(row, "growx");
		dialog.add(hint, "wmin 0");
		dialog.add(ToolStyle.mutedLabel(I18n.t("memory.pc_memory") + (totalPc > 0 ? totalPc + " MB, " + freeNow + I18n.t("memory.mb_available_to_screept_now") : I18n.t("memory.not_available"))
				+ (jvm64 ? "" : I18n.t("memory.32_bit_java_1400_mb_at")) + "</html>"), "wmin 0");

		JButton cancel = ToolStyle.button(I18n.t("common.cancel"), null, I18n.t("memory.close_without_changes"));
		final boolean[] saved = {false};
		save.addActionListener(e -> {
			if (write(owner, savedArgs, slider.getValue(), initialMb)) {
				saved[0] = true;
				dialog.dispose();
			}
		});
		cancel.addActionListener(e -> dialog.dispose());
		javax.swing.JPanel buttons = new javax.swing.JPanel(new MigLayout("insets 0, gapx 8", "push[][]", "[]"));
		buttons.setOpaque(false);
		buttons.add(cancel);
		buttons.add(save, "w 110!");
		dialog.add(buttons, "growx, gaptop 6");
		update.run();

		gui.commons.imagestudio.DialogFit.apply(dialog, owner);
		dialog.setVisible(true);

		if (saved[0]) {
			Object[] options = {I18n.t("memory.restart_now"), "Later"};
			int choice = JOptionPane.showOptionDialog(owner, I18n.t("memory.the_new_maximum_memory_is_saved"), I18n.t("memory.maximum_memory"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (choice == 0) {
				MainGuiManager.restartApplication(false);
			}
		}
	}

	/** Replaces -Xmx (and keeps -Xms not above it) in the saved arguments, leaving every other option as it is */
	private static boolean write(Window owner, String oldArgs, int newMb, long initialMb) {
		String args = oldArgs;
		Matcher mx = XMX.matcher(args);
		args = mx.find() ? mx.replaceFirst("-Xmx" + newMb + "m") : ("-Xmx" + newMb + "m " + args).trim();
		Matcher ms = XMS.matcher(args);
		if (ms.find() && toMb(ms) > newMb) {
			args = ms.replaceFirst("-Xms" + newMb + "m");
		}
		if (!LauncherIniOptions.setMemoryArgs(args)) {
			JOptionPane.showMessageDialog(owner, I18n.t("memory.the_launcher_settings_could_not_be"), I18n.t("memory.maximum_memory"), JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
}
