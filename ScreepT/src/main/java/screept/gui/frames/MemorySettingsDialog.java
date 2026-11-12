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
package screept.gui.frames;

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
			javax.swing.JOptionPane.showMessageDialog(owner, "The launcher settings file was not found: the memory cannot be changed from here.", "Maximum memory",
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

		final JDialog dialog = new JDialog(owner, "Maximum memory of ScreepT", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
		dialog.setLayout(new MigLayout("insets 16, gapy 8, wrap 1", "[grow,fill]", ""));
		dialog.add(ToolStyle.caption("Maximum memory (Java heap)"));
		dialog.add(new JLabel("<html>Running now: <b>" + runningMb + " MB</b> &nbsp;&nbsp; Saved in the launcher: <b>" + savedMb + " MB</b><br>"
				+ "Choose from the minimum set in the launcher to the most this PC can give now.</html>"));

		long start = Math.max(minMb, Math.min(limitMax, savedMb));
		final JSlider slider = new JSlider((int) minMb, (int) limitMax, (int) start);
		slider.setMajorTickSpacing((int) Math.max(STEP, (limitMax - minMb) / 4 / STEP * STEP));
		slider.setPaintTicks(true);
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel((int) start, (int) minMb, (int) limitMax, STEP));
		final JLabel hint = ToolStyle.mutedLabel(" ");
		final JButton save = ToolStyle.accent(ToolStyle.button("Save", null, "Write the value in the launcher settings"), ToolStyle.GREEN);

		final Runnable update = new Runnable() {
			@Override
			public void run() {
				int v = slider.getValue();
				String text;
				if (totalPc > 0 && v > totalPc * 3 / 4) {
					text = "More than 75% of the PC memory: Windows and other programs may slow down.";
				} else if (v > freeNow) {
					text = "More than the memory free now: it may use the disk swap.";
				} else {
					text = v == (int) limitMax ? "This is the most available right now." : v == (int) minMb ? "This is the minimum set in the launcher." : " ";
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
		JButton bMin = ToolStyle.button("Minimum", null, "The minimum set in the launcher (initial heap, at least 256 MB)");
		bMin.addActionListener(e -> slider.setValue((int) minMb));
		JButton bRec = ToolStyle.button("Recommended", null, "A quarter of the PC memory, at most what is free now");
		bRec.addActionListener(e -> {
			long rec = totalPc > 0 ? Math.min(limitMax, Math.max(minMb, totalPc / 4)) : savedMb;
			slider.setValue((int) (rec / STEP * STEP));
		});
		JButton bMax = ToolStyle.button("Maximum", null, "The most the JVM and the PC can give right now");
		bMax.addActionListener(e -> slider.setValue((int) limitMax));
		row.add(bMin);
		row.add(bRec);
		row.add(bMax);
		dialog.add(row, "growx");
		dialog.add(hint, "wmin 0");
		dialog.add(ToolStyle.mutedLabel("<html>PC memory: " + (totalPc > 0 ? totalPc + " MB, " + freeNow + " MB available to ScreepT now" : "not available")
				+ (jvm64 ? "" : " (32 bit Java: 1400 MB at most)") + "</html>"), "wmin 0");

		JButton cancel = ToolStyle.button("Cancel", null, "Close without changes");
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
			Object[] options = {"Restart now", "Later"};
			int choice = JOptionPane.showOptionDialog(owner, "<html>The new maximum memory is saved in the launcher settings.<br>"
					+ "It applies the next time ScreepT starts: <b>a restart is needed</b>.<br><br>Restart ScreepT now?</html>", "Maximum memory", JOptionPane.DEFAULT_OPTION,
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
			JOptionPane.showMessageDialog(owner, "The launcher settings could not be written.", "Maximum memory", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
}
