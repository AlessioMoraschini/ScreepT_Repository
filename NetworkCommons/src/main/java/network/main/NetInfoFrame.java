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
package network.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.frame.arch.ToolIcons;
import gui.commons.frameutils.frame.arch.ToolStyle;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import net.miginfocom.swing.MigLayout;
import network.NetworkAdapter;
import network.NetworkUtils;
import network.om.FtpSpeedTestOptions;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;
import various.common.light.gui.dialogs.msg.JOptionHelper;

/**
 * Network Info: details of a network adapter (addresses, gateway, DNS, public IP) and a connection speed test.
 * By default only physical adapters with their IPv4 address are listed; "Deep search" shows everything that is up
 * (virtual machines, VPNs, containers, tunnels, IPv6, link-local).
 */
public class NetInfoFrame extends ParentFrame{
	private static final long serialVersionUID = -2907513458441322410L;

	// NetInfoFrame logger
	static Logger logger = Logger.getLogger(NetInfoFrame.class);

	public static NetInfoFrame singletonInstance = null;

	private static final String errMSG = "Error";
	private static final String UNSELECTED = "-";
	private static final String CALCULATING = "Calculating...";

	private static final String SPEED_TEST_TOOLTIP = "Downloads and uploads a random generated file with FTP, measuring up/down speed";

	// gui fields
	private JComboBox<NetworkAdapter> comboAdapters;
	private JCheckBox chkDeepSearch;
	private JLabel lblAdaptersInfo;
	private JButton btnCalculateConnSpeed;
	private JTextField dynLblAdapterName;
	private JTextField dynLblMacAddress;
	private JTextField dynLblLocalIP;
	private JTextField dynLblPublicIP;
	private JTextField dynLblDNS1;
	private JTextField dynLblDNS2;
	private JTextField dynLblHostName;
	private JTextField dynLblDefGateway;
	private JTextField dynLblSubnetMask;
	private JTextField dynLblMTU;
	private JTextField dynLblBroadcastAddr;
	private JLabel dynLblUPspeed;
	private JLabel dynLblDOWNspeed;
	private JLabel dynLblLatency;
	private JComboBox<FtpSpeedTestOptions> comboSpeedTestOpt;
	private JProgressBar progressBarUp;
	private JProgressBar progressBarDown;
	private JProgressBar progressBarLatency;

	/** Label (caption) -> field, in display order: used by "Copy all" */
	private final Map<String, JTextField> infoFields = new LinkedHashMap<String, JTextField>();
	/** True while the combo is refilled: its events must not trigger a refresh each */
	private volatile boolean reloadingCombo = false;

	// network related objects
	private NetworkUtils netUtils;
	public NetworkAdapter currentSelAdapter;

////////////////////////// CONSTRUCTOR //////////////////////////

	public NetInfoFrame() {
		this(null);
	}

	/**
	 * @param preloadedNetUtils adapters already retrieved off the EDT (null: they are retrieved here, blocking the caller)
	 */
	public NetInfoFrame(NetworkUtils preloadedNetUtils) {
		super();

		logger.info(this.getClass().getName() + " Starting...");

		setTitle("Network Info");

		isActive.set(true);
		singletonInstance = this;

		// SW window icon
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_NET_INFO, this);

		try {
			netUtils = preloadedNetUtils != null ? preloadedNetUtils : new NetworkUtils();
		} catch (IOException e) {
			handleException("Exception initializing network informations", e);
		}

		initialize();

		addEventHandlers();

		logger.info(this.getClass().getName() + ": ready!");
	}

////////////////////////// INITIALIZER METHOD //////////////////////////

	private void initialize() {

		getContentPane().setLayout(new MigLayout("fill, insets 16, gapy 12", "[grow,fill]", "[][][grow,fill]"));

		getContentPane().add(ToolStyle.header("Network Info", "Addresses, gateway, DNS and speed of your network adapters"), "growx, wrap");

		// toolbar: adapter selection
		JPanel toolbar = ToolStyle.card(new MigLayout("insets 10 12 10 12, gapx 8", "[][grow,fill][][]", "[]"));
		toolbar.add(ToolStyle.caption("Adapter"));
		comboAdapters = new JComboBox<NetworkAdapter>();
		comboAdapters.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		comboAdapters.setRenderer(new AdapterRenderer());
		toolbar.add(comboAdapters, "growx, wmin 200");
		chkDeepSearch = new JCheckBox("Deep search");
		chkDeepSearch.setToolTipText("<html>Off: only physical adapters (Ethernet, Wi-Fi) with their IPv4 address.<br>On: also virtual machines, VPNs, containers, tunnels, IPv6 and link-local addresses.</html>");
		ToolStyle.hand(chkDeepSearch);
		toolbar.add(chkDeepSearch);
		JButton btnRefresh = ToolStyle.iconButton(ToolIcons.Kind.REFRESH, "Reload the adapters list (F5)");
		btnRefresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reloadAdaptersFromSystem();
			}
		});
		toolbar.add(btnRefresh);
		getContentPane().add(toolbar, "growx, split 1, wrap");

		// body: interface card + speed card
		JPanel body = new JPanel(new MigLayout("insets 0, gapx 12", "[grow 60,fill][grow 40,fill]", "[grow,fill]"));
		body.setOpaque(false);
		body.add(buildInterfaceCard());
		body.add(buildSpeedCard());
		getContentPane().add(body, "grow, wrap");

		lblAdaptersInfo = ToolStyle.mutedLabel(" ");
		getContentPane().add(lblAdaptersInfo, "growx");

		ToolStyle.bindKey(this, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0), "reloadAdapters", new Runnable() {
			@Override
			public void run() {
				reloadAdaptersFromSystem();
			}
		});

		if(netUtils!=null) {
			reloadAdaptersCombo();
		}else {
			updateNetDisplayedERROR();
		}

		// public IP, DNS and gateway come from the network and OS commands: retrieve them off the EDT, so the window shows up at once
		updateCalculatingDispLbl();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					updateNetDisplayedINFO();
				} catch (Exception e) {
					logger.error("Error retrieving network informations", e);
				}
			}
		}, "NetInfo-initial-load").start();

		logger.info(this.getClass().getName() + " Started");
	}

	private JPanel buildInterfaceCard() {
		JPanel card = ToolStyle.card(new MigLayout("insets 14 16 14 16, wrap 3, gapy 6", "[right][grow,fill][]", ""));
		card.add(ToolStyle.caption("Interface"), "span 3, left, wrap 6");

		dynLblAdapterName = infoRow(card, "Adapter name");
		dynLblHostName = infoRow(card, "Host name");
		dynLblMacAddress = infoRow(card, "MAC address");
		dynLblMTU = infoRow(card, "MTU");
		card.add(ToolStyle.caption("Addressing"), "span 3, left, gaptop 10, wrap 6");
		dynLblLocalIP = infoRow(card, "Local IP");
		dynLblPublicIP = infoRow(card, "Public IP");
		dynLblSubnetMask = infoRow(card, "Subnet mask");
		dynLblBroadcastAddr = infoRow(card, "Broadcast");
		dynLblDefGateway = infoRow(card, "Default gateway");
		card.add(ToolStyle.caption("Name servers"), "span 3, left, gaptop 10, wrap 6");
		dynLblDNS1 = infoRow(card, "DNS 1");
		dynLblDNS2 = infoRow(card, "DNS 2");

		JButton btnCopyAll = ToolStyle.button("Copy all", ToolIcons.Kind.COPY, "Copy every value of this adapter as text (Ctrl+Shift+C)");
		btnCopyAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StringBuilder text = new StringBuilder();
				for (Map.Entry<String, JTextField> entry : infoFields.entrySet()) {
					text.append(entry.getKey()).append(": ").append(entry.getValue().getText()).append(System.lineSeparator());
				}
				ToolStyle.copyToClipboard(text.toString());
			}
		});
		card.add(btnCopyAll, "span 3, right, gaptop 12");
		ToolStyle.bindKey(this, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK), "copyAllInfo", new Runnable() {
			@Override
			public void run() {
				btnCopyAll.doClick(0);
			}
		});
		return card;
	}

	/** Caption, read only monospace value (selectable) and a copy button */
	private JTextField infoRow(JPanel card, String caption) {
		card.add(ToolStyle.mutedLabel(caption));
		final JTextField field = new JTextField(UNSELECTED);
		field.setEditable(false);
		field.setFont(ToolStyle.mono(0f));
		field.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		field.setOpaque(false);
		card.add(field);
		JButton copy = ToolStyle.iconButton(ToolIcons.Kind.COPY, "Copy " + caption);
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ToolStyle.copyToClipboard(field.getText());
			}
		});
		card.add(copy);
		infoFields.put(caption, field);
		return field;
	}

	private JPanel buildSpeedCard() {
		JPanel card = ToolStyle.card(new MigLayout("insets 14 16 14 16, wrap 1, gapy 4", "[grow,fill]", ""));
		card.add(ToolStyle.caption("Connection speed"), "wrap 6");

		dynLblLatency = bigValue();
		progressBarLatency = progress();
		metric(card, "Latency", dynLblLatency, progressBarLatency);
		dynLblDOWNspeed = bigValue();
		progressBarDown = progress();
		metric(card, "Download", dynLblDOWNspeed, progressBarDown);
		dynLblUPspeed = bigValue();
		progressBarUp = progress();
		metric(card, "Upload", dynLblUPspeed, progressBarUp);

		card.add(ToolStyle.mutedLabel("Test size"), "gaptop 8");
		comboSpeedTestOpt = new JComboBox<FtpSpeedTestOptions>(FtpSpeedTestOptions.values());
		ToolStyle.hand(comboSpeedTestOpt);
		card.add(comboSpeedTestOpt, "growx");

		btnCalculateConnSpeed = ToolStyle.accent(ToolStyle.button("Start speed test", null, SPEED_TEST_TOOLTIP), ToolStyle.GREEN);
		btnCalculateConnSpeed.setPreferredSize(new Dimension(160, 34));
		card.add(btnCalculateConnSpeed, "growx, gaptop 6");
		return card;
	}

	private void metric(JPanel card, String caption, JLabel value, JProgressBar bar) {
		card.add(ToolStyle.mutedLabel(caption), "gaptop 6");
		card.add(value);
		card.add(bar, "growx, h 8!");
	}

	private JLabel bigValue() {
		JLabel label = new JLabel(UNSELECTED);
		label.setFont(ToolStyle.font(6f, Font.BOLD));
		return label;
	}

	private JProgressBar progress() {
		JProgressBar bar = new JProgressBar();
		bar.setStringPainted(false);
		return bar;
	}

	/** Adapter name, address and a "virtual" mark (only visible with Deep search) */
	private static class AdapterRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof NetworkAdapter) {
				NetworkAdapter adapter = (NetworkAdapter) value;
				setText(adapter.name + "   " + adapter.locIP + (adapter.isVirtual() ? "   (virtual)" : ""));
			}
			return this;
		}
	}

////////////////////////// EVENT HANDLERS ACTION LISTENERS //////////////////////////

	private void addEventHandlers() {

		// default close operation(same as reset)
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {

	        	logger.debug("Closing Frame: "+NetInfoFrame.class.getName());
	        	singletonInstance = null;
	        	isActive.set(false);
	        	dispose();
		    }
		});

		// COMBO ADAPTER SELECTOR SELECTION CHANGE
		comboAdapters.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (reloadingCombo) {
					return;
				}
				refreshSelectedAdapterAsync();
			}
		});

		// DEEP SEARCH: more or fewer adapters in the list
		chkDeepSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				reloadAdaptersCombo();
				refreshSelectedAdapterAsync();
			}
		});

		// SPEED CALCULATOR BUTTON LISTENER
		btnCalculateConnSpeed.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {

					@Override
					public void run() {
						setEnabledSafe(btnCalculateConnSpeed, false);
						btnCalculateConnSpeed.setToolTipText("A test is already running: wait for the results");
						resetSpeedTestValues(true, true, true);

						setText(dynLblUPspeed, CALCULATING);
						setText(dynLblDOWNspeed, CALCULATING);
						setText(dynLblLatency, CALCULATING);

						calculateLatency();
						calculateDOWNspeed((FtpSpeedTestOptions)comboSpeedTestOpt.getSelectedItem());
						calculateUPspeed((FtpSpeedTestOptions)comboSpeedTestOpt.getSelectedItem());

						btnCalculateConnSpeed.setToolTipText(SPEED_TEST_TOOLTIP);
						setEnabledSafe(btnCalculateConnSpeed, true);
					}
				}).start();
			}
		});
	}

	private void refreshSelectedAdapterAsync() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateCalculatingDispLbl();
				resetSpeedTestValues(true, true, true);
				updateNetDisplayedINFO();
			}
		}, "NetInfo-adapter-refresh").start();
	}

	/** F5 / refresh button: discovers the adapters again (a cable plugged, a VPN started...) */
	private void reloadAdaptersFromSystem() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					netUtils = new NetworkUtils();
				} catch (IOException e) {
					handleException("Exception reading network informations", e);
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						reloadAdaptersCombo();
						refreshSelectedAdapterAsync();
					}
				});
			}
		}, "NetInfo-reload").start();
	}

////////////////////////// AND NOW SOME USEFUL METHOD //////////////////////////

	private void calculateLatency() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
				setProgress(progressBarLatency, 0, null);
				try {
					int latency = NetworkUtils.retrieveLatencyV2(currentSelAdapter.getInetAddr());
					setText(dynLblLatency, String.valueOf(latency)+" ms");
					setProgress(progressBarLatency, 100, ToolStyle.GREEN);

				} catch (UnknownHostException e) {
					String msg = "Host "+GeneralConfig.testAddress+" not recognized";
					setText(dynLblLatency, msg);
					logger.error(msg, e);
					setProgress(progressBarLatency, 0, ToolStyle.RED);
				} catch (Exception e) {
					String msg = "Error occurred";
					setText(dynLblLatency, msg);
					logger.error(msg, e);
					setProgress(progressBarLatency, 0, ToolStyle.RED);
				}
			}
		}).start();
	}

	private void calculateUPspeed(FtpSpeedTestOptions options) {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		try {
			setText(dynLblUPspeed, NetworkUtils.retrieveUpSpeed(currentSelAdapter.getInetAddr(), options, progressBarUp));
		} catch (Exception e) {
			String msg = "An error occcurred";
			setText(dynLblUPspeed, msg);
			progressBarUp.setIndeterminate(false);
			progressBarUp.setValue(0);
			logger.error(msg, e);
		}
	}

	private void calculateDOWNspeed(FtpSpeedTestOptions options) {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		try {
			setText(dynLblDOWNspeed, NetworkUtils.retrieveDownSpeedV2(()->!this.isActive.get(), currentSelAdapter.getInetAddr(), options, progressBarDown));
		} catch (Exception e) {
			String msg = "An error occcurred";
			setText(dynLblDOWNspeed, msg);
			progressBarDown.setIndeterminate(false);
			progressBarDown.setValue(0);
			logger.error(msg, e);
		}
	}

	private void handleException(String msg, Throwable e) {
		logger.error(msg, e);
		new JOptionHelper(singletonInstance).error(msg, msg);
	}

	// thread safe setters: the network work runs off the EDT
	private static void setText(final JTextComponent component, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				component.setText(text);
				component.setCaretPosition(0);
			}
		});
	}

	private static void setText(final JLabel label, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
			}
		});
	}

	private static void setEnabledSafe(final Component component, final boolean enabled) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				component.setEnabled(enabled);
			}
		});
	}

	private static void setProgress(final JProgressBar bar, final int value, final Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				bar.setValue(value);
				bar.setForeground(color);
			}
		});
	}

	// interface related info
	private void updateNetDisplayedINFO() {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		if (currentSelAdapter == null) {
			updateNetDisplayedERROR();
			return;
		}
		setText(dynLblAdapterName, currentSelAdapter.name);
		setText(dynLblLocalIP, currentSelAdapter.locIP);
		setText(dynLblMacAddress, currentSelAdapter.locMAC);
		setText(dynLblHostName, currentSelAdapter.hostName);
		setText(dynLblMTU, String.valueOf(currentSelAdapter.getMTU()));
		setText(dynLblSubnetMask, currentSelAdapter.getSubnetMaskFromInetAddr());
		setText(dynLblBroadcastAddr, currentSelAdapter.getBroadCastFromInetAddr());
		try {
			setText(dynLblPublicIP, netUtils.retrieveAdapterPublicIP(currentSelAdapter));
		} catch (IOException e1) {
			String msg = "Connection Error";
			setText(dynLblPublicIP, msg);
			handleException(msg, e1);
		}

		updateAdapterGUIinfos(currentSelAdapter.getInetAddr());
	}

	private void updateCalculatingDispLbl() {

		setText(dynLblAdapterName, CALCULATING);
		setText(dynLblLocalIP, CALCULATING);
		setText(dynLblMacAddress, CALCULATING);
		setText(dynLblHostName, CALCULATING);
		setText(dynLblMTU, CALCULATING);
		setText(dynLblSubnetMask, CALCULATING);
		setText(dynLblBroadcastAddr, CALCULATING);
		setText(dynLblPublicIP, CALCULATING);
		setText(dynLblDefGateway, CALCULATING);
		setText(dynLblDNS1, CALCULATING);
		setText(dynLblDNS2, CALCULATING);
	}

	// interface unrelated info
	private void updateAdapterGUIinfos(InetAddress inetAddress) {

		List<String> nameServers = NetworkUtils.getDnsServersUsingOSCommands(inetAddress);
		if (nameServers.isEmpty()) {
			setText(dynLblDNS1, UNSELECTED);
			setText(dynLblDNS2, UNSELECTED);
		} else {
			setText(dynLblDNS1, nameServers.get(0));
			setText(dynLblDNS2, nameServers.size() > 1 ? nameServers.get(1) : UNSELECTED);
		}

		try {
			setText(dynLblDefGateway, NetworkUtils.getIpNetstatGateway());
		} catch (Exception e) {
			String msg = "Error retrieving network Informations";
			setText(dynLblDefGateway, errMSG);
			handleException(msg, e);
		}
	}

	private void updateNetDisplayedERROR() {
		JTextField[] all = {dynLblAdapterName, dynLblLocalIP, dynLblMacAddress, dynLblHostName, dynLblPublicIP, dynLblDNS1, dynLblDNS2,
				dynLblMTU, dynLblSubnetMask, dynLblBroadcastAddr, dynLblDefGateway};
		for (JTextField field : all) {
			setText(field, errMSG);
		}
	}

	private void reloadAdaptersCombo() {
		boolean deep = chkDeepSearch.isSelected();
		reloadingCombo = true;
		try {
			DefaultComboBoxModel<NetworkAdapter> model = new DefaultComboBoxModel<NetworkAdapter>( netUtils.getAdapters(deep) );
			comboAdapters.setModel( model );
			comboAdapters.revalidate();
			comboAdapters.repaint();
		} finally {
			reloadingCombo = false;
		}
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		int shown = netUtils.getAdapters(deep).size();
		int total = netUtils.getAdapters(true).size();
		lblAdaptersInfo.setText(deep || shown >= total
				? shown + " adapter(s) found"
				: shown + " physical adapter(s) shown, " + (total - shown) + " virtual or secondary hidden (enable Deep search to see them)");
	}

	private void resetSpeedTestValues(boolean latency, boolean upload, boolean download) {
		if (download) {
			setProgress(progressBarDown, 0, null);
			setText(dynLblDOWNspeed, UNSELECTED);
			progressBarDown.setIndeterminate(false);
		}
		if (upload) {
			setProgress(progressBarUp, 0, null);
			setText(dynLblUPspeed, UNSELECTED);
			progressBarUp.setIndeterminate(false);
		}
		if (latency) {
			setProgress(progressBarLatency, 0, null);
			setText(dynLblLatency, UNSELECTED);
			progressBarLatency.setIndeterminate(false);
		}

	}

	@Override
	public Dimension getDefaultDimension() {
		return new Dimension(940, 760);
	}

	public static NetInfoFrame getInstance() {
		return getInstance(null);
	}

	public static NetInfoFrame getInstance(NetworkUtils preloadedNetUtils) {
		return singletonInstance == null ? new NetInfoFrame(preloadedNetUtils) : singletonInstance;
	}
}
