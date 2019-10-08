package gUI.frames;

import resources.GeneralConfig;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.SigarException;

import frameutils.utils.GuiUtils;
import mainobj.INItializer;
import net.miginfocom.swing.MigLayout;
import network.NetworkUtils;
import network.om.FtpSpeedTestOptions;
import network.om.NetworkAdapter;
import resources.IconsPathConfigurator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.awt.Cursor;

public class NetInfoFrame extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(NetInfoFrame.class); 
	
	private static final String errMSG = "### ERROR ###";
	private static final String UNSELECTED = "##############";
	private static final String CALCULATING = "Calculating...";
	
	// flag to signal active
	public static boolean active=false;
	
	// gui fields
	private JComboBox<NetworkAdapter> comboAdapters;
	private JLabel lblNetworkInfoTool;
	private JPanel headerPanelTitle;
	private JLabel lblSelectedNetworkAdapter;
	private JScrollPane ConncetionInfoScrollPanel;
	private JPanel panel;
	private JPanel panel_1;
	private JButton btnCalculateConnSpeed;
	private JLabel lblUpload;
	private JLabel lblDownload;
	private JLabel lblMacAddress;
	private JLabel lblLocalIp;
	private JLabel lblPublicIp;
	private JLabel lblAdapterName;
	private JLabel lblDNS1;
	private JTextField dynLblAdapterName;
	private JTextField dynLblMacAddress;
	private JTextField dynLblLocalIP;
	private JTextField dynLblPublicIP;
	private JTextField dynLblDNS1;
	private JSeparator separatorVertMiddle;
	private JLabel labelLatency;
	private JLabel lblHostName;
	private JTextField dynLblHostName;
	private JLabel dynLblUPspeed;
	private JLabel dynLblDOWNspeed;
	private JLabel dynLblLatency;
	private JLabel lblDNS2;
	private JTextField dynLblDNS2;
	private JLabel lblDefaultGateway;
	private JTextField dynLblDefGateway;
	private JLabel lblSubnetMask;
	private JTextField dynLblSubnetMask;
	private JLabel lblMTU;
	private JTextField dynLblMTU;
	private JComboBox<FtpSpeedTestOptions> comboSpeedTestOpt;
	private JProgressBar progressBarUp;
	private JProgressBar progressBarDown;
	private JProgressBar progressBarLatency;
	private JSeparator separatorUpSpeed;
	private JSeparator separator;
	private JLabel lblBroadcastAddress;
	private JTextField dynLblBroadcastAddr;
	
	// network related objects
	public NetInfoFrame thisPanel;
	private NetworkUtils netUtils;
	public NetworkAdapter currentSelAdapter;
	
////////////////////////// CONSTRUCTOR //////////////////////////
	
	public NetInfoFrame(INItializer currentConfig) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setMaximumSize(new Dimension(800, 450));
		setMinimumSize(new Dimension(750, 400));
		
		active=true;
		thisPanel = this;
		
		// SW window icon
		GuiUtils.setFrameIcon(IconsPathConfigurator.ICON_NET_INFO, this);
		
		try {
			netUtils = new NetworkUtils();
		} catch (IOException e) {
			handleException("Exception initializing network informations", e);
		}
		
		initialize();
		
		addEventHandlers();
	}
	
////////////////////////// INITIALIZER METHOD //////////////////////////
	
	private void initialize() {
	
		getContentPane().setBackground(Color.BLACK);
		getContentPane().setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[50px,grow][25%,grow][60%,grow][50px,grow]", "[60px][60px][75%,grow]"));
		
		headerPanelTitle = new JPanel();
		headerPanelTitle.setMinimumSize(new Dimension(100, 10));
		headerPanelTitle.setForeground(SystemColor.menu);
		headerPanelTitle.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		headerPanelTitle.setBackground(new Color(119, 136, 153));
		getContentPane().add(headerPanelTitle, "cell 0 0 4 1,grow");
		headerPanelTitle.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[grow,trailing][100%,grow,center][grow,trailing]", "[20px,grow]"));
		
		lblNetworkInfoTool = new JLabel("Network Info Tool");
		lblNetworkInfoTool.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNetworkInfoTool.setHorizontalTextPosition(SwingConstants.CENTER);
		lblNetworkInfoTool.setHorizontalAlignment(SwingConstants.CENTER);
		lblNetworkInfoTool.setForeground(new Color(0, 0, 0));
		lblNetworkInfoTool.setFont(new Font("Lithos Pro Regular", Font.BOLD, 24));
		lblNetworkInfoTool.setBackground(SystemColor.activeCaptionBorder);
		headerPanelTitle.add(lblNetworkInfoTool, "cell 1 0,alignx center,growy");
		
		lblSelectedNetworkAdapter = new JLabel("Selected Network Adapter :");
		lblSelectedNetworkAdapter.setBackground(Color.WHITE);
		lblSelectedNetworkAdapter.setForeground(Color.LIGHT_GRAY);
		lblSelectedNetworkAdapter.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblSelectedNetworkAdapter, "cell 1 1,alignx center");
		
		comboAdapters = new JComboBox<NetworkAdapter>();
		comboAdapters.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if(netUtils!=null) {
			reloadAdaptersCombo();
		}else {
			updateNetDisplayedERROR();
		}
		getContentPane().add(comboAdapters, "cell 2 1,growx");
		
		ConncetionInfoScrollPanel = new JScrollPane();
		ConncetionInfoScrollPanel.setBackground(Color.WHITE);
		ConncetionInfoScrollPanel.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		getContentPane().add(ConncetionInfoScrollPanel, "cell 0 2 4 1,grow");
		
		panel = new JPanel();
		panel.setBackground(new Color(255, 250, 240));
		ConncetionInfoScrollPanel.setViewportView(panel);
		panel.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[12.5%,grow][12.5%,grow][12.5%,grow][10px][12.5%,grow][12.5%,grow][12.5%,grow]", "[40,grow][40,grow][40,grow][40,grow][40,grow][40,grow][40,grow][40,grow]"));
		
		lblAdapterName = new JLabel("Adapter Name:");
		lblAdapterName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblAdapterName, "cell 0 0,alignx center");
		
		dynLblAdapterName = new JTextField(UNSELECTED);
		dynLblAdapterName.setEditable(false);
		dynLblAdapterName.setForeground(new Color(72, 61, 139));
		dynLblAdapterName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblAdapterName, "cell 1 0 2 1,growx");
		
		btnCalculateConnSpeed = new JButton("Calculate connection speed");
		btnCalculateConnSpeed.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCalculateConnSpeed.setMinimumSize(new Dimension(0, 40));
		btnCalculateConnSpeed.setFont(new Font("Segoe UI", Font.BOLD, 16));
		btnCalculateConnSpeed.setBackground(new Color(192, 192, 192));
		panel.add(btnCalculateConnSpeed, "cell 4 0 2 1,growx");
		
//		comboSpeedTestOpt = new JComboBox<FtpSpeedTestOptions>(); // active this only while updating graphics with windowbuilder
		comboSpeedTestOpt = new JComboBox<FtpSpeedTestOptions>(FtpSpeedTestOptions.values());
		comboSpeedTestOpt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel.add(comboSpeedTestOpt, "cell 6 0,alignx center,aligny center");
		
		lblHostName = new JLabel("Host Name:");
		lblHostName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblHostName, "cell 0 1,alignx center");
		
		dynLblHostName = new JTextField(UNSELECTED);
		dynLblHostName.setEditable(false);
		dynLblHostName.setForeground(new Color(72, 61, 139));
		dynLblHostName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblHostName, "cell 1 1 2 1,growx");
		
		lblMacAddress = new JLabel("Mac Address:");
		lblMacAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblMacAddress, "cell 0 2,alignx center");
		
		dynLblMacAddress = new JTextField(UNSELECTED);
		dynLblMacAddress.setEditable(false);
		dynLblMacAddress.setForeground(new Color(72, 61, 139));
		dynLblMacAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblMacAddress, "cell 1 2 2 1,growx");
		
		separatorVertMiddle = new JSeparator();
		separatorVertMiddle.setBackground(new Color(0, 0, 0));
		separatorVertMiddle.setOrientation(SwingConstants.VERTICAL);
		panel.add(separatorVertMiddle, "cell 3 0 1 8,alignx center,growy");
		
		panel_1 = new JPanel();
		panel_1.setBackground(Color.LIGHT_GRAY);
		panel_1.setBorder(new LineBorder(new Color(0, 51, 51), 2));
		panel.add(panel_1, "cell 4 1 3 4,grow");
		panel_1.setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[10%,grow][50%,grow]", "[15px][30px,grow][10px][10px][30px,grow][10px][10px][30px,grow]"));
		
		progressBarUp = new JProgressBar();
		progressBarUp.setBackground(Color.DARK_GRAY);
		progressBarUp.setForeground(Color.BLACK);
		progressBarUp.setStringPainted(true);
		panel_1.add(progressBarUp, "cell 0 0 2 1,growx");
		
		lblUpload = new JLabel("Upload:");
		lblUpload.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(lblUpload, "cell 0 1,alignx center");
		
		dynLblUPspeed = new JLabel(UNSELECTED);
		dynLblUPspeed.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblUPspeed.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblUPspeed, "cell 1 1,alignx center");
		
		separatorUpSpeed = new JSeparator();
		separatorUpSpeed.setPreferredSize(new Dimension(0, 4));
		separatorUpSpeed.setForeground(Color.BLACK);
		panel_1.add(separatorUpSpeed, "cell 0 2 2 1,growx");
		
		progressBarDown = new JProgressBar();
		progressBarDown.setBackground(Color.DARK_GRAY);
		progressBarDown.setForeground(Color.BLACK);
		progressBarDown.setStringPainted(true);
		panel_1.add(progressBarDown, "cell 0 3 2 1,growx");
		
		lblDownload = new JLabel("Download:");
		lblDownload.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(lblDownload, "cell 0 4,alignx center");
		
		dynLblDOWNspeed = new JLabel(UNSELECTED);
		dynLblDOWNspeed.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblDOWNspeed, "cell 1 4,alignx center");
		
		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(0, 4));
		separator.setForeground(Color.BLACK);
		panel_1.add(separator, "cell 0 5 2 1,growx");
		
		progressBarLatency = new JProgressBar();
		panel_1.add(progressBarLatency, "cell 0 6 2 1,growx");
		
		labelLatency = new JLabel("Latency:");
		labelLatency.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(labelLatency, "cell 0 7,alignx center");
		
		dynLblLatency = new JLabel(UNSELECTED);
		dynLblLatency.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblLatency, "cell 1 7,alignx center");
		
		lblLocalIp = new JLabel("Local IP:");
		lblLocalIp.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblLocalIp, "cell 0 3,alignx center");
		
		dynLblLocalIP = new JTextField(UNSELECTED);
		dynLblLocalIP.setEditable(false);
		dynLblLocalIP.setForeground(new Color(72, 61, 139));
		dynLblLocalIP.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblLocalIP, "cell 1 3 2 1,growx");
		
		lblPublicIp = new JLabel("Public IP:");
		lblPublicIp.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblPublicIp, "cell 0 4,alignx center,aligny center");
		
		dynLblPublicIP = new JTextField(UNSELECTED);
		dynLblPublicIP.setEditable(false);
		dynLblPublicIP.setForeground(new Color(72, 61, 139));
		dynLblPublicIP.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblPublicIP, "cell 1 4 2 1,growx");
		
		lblSubnetMask = new JLabel("Subnet Mask:");
		lblSubnetMask.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblSubnetMask, "cell 0 5,alignx center");
		
		dynLblSubnetMask = new JTextField(UNSELECTED);
		dynLblSubnetMask.setForeground(new Color(72, 61, 139));
		dynLblSubnetMask.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblSubnetMask.setEditable(false);
		panel.add(dynLblSubnetMask, "cell 1 5 2 1,growx");
		
		lblDefaultGateway = new JLabel("Default Gateway:");
		lblDefaultGateway.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblDefaultGateway, "cell 4 5,alignx center");
		
		dynLblDefGateway = new JTextField(UNSELECTED);
		dynLblDefGateway.setForeground(new Color(72, 61, 139));
		dynLblDefGateway.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblDefGateway.setEditable(false);
		panel.add(dynLblDefGateway, "cell 5 5 2 1,growx");
		
		lblBroadcastAddress = new JLabel("Broadcast address:");
		lblBroadcastAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblBroadcastAddress, "cell 0 6,alignx center");
		
		dynLblBroadcastAddr = new JTextField("##############");
		dynLblBroadcastAddr.setForeground(new Color(72, 61, 139));
		dynLblBroadcastAddr.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblBroadcastAddr.setEditable(false);
		panel.add(dynLblBroadcastAddr, "cell 1 6 2 1,growx");
		
		lblDNS1 = new JLabel("DNS 1:");
		lblDNS1.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblDNS1, "cell 4 6,alignx center");
		
		dynLblDNS1 = new JTextField(UNSELECTED);
		dynLblDNS1.setEditable(false);
		dynLblDNS1.setForeground(new Color(72, 61, 139));
		dynLblDNS1.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblDNS1, "cell 5 6 2 1,growx");
		
		lblDNS2 = new JLabel("DNS 2:");
		lblDNS2.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblDNS2, "cell 4 7,alignx center");
		
		dynLblDNS2 = new JTextField(UNSELECTED);
		dynLblDNS2.setForeground(new Color(72, 61, 139));
		dynLblDNS2.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblDNS2.setEditable(false);
		panel.add(dynLblDNS2, "cell 5 7 2 1,growx");
		
		lblMTU = new JLabel("MTU:");
		lblMTU.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblMTU, "cell 0 7,alignx center");
		
		dynLblMTU = new JTextField("##############");
		dynLblMTU.setForeground(new Color(72, 61, 139));
		dynLblMTU.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblMTU.setEditable(false);
		panel.add(dynLblMTU, "cell 1 7 2 1,growx");
		
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: "+NetInfoFrame.class.getName());	        	
				active = false;
	        	dispose();
		    }
		});
		
		// call now cause before some gui elements can not be initialized yet
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				updateNetDisplayedINFO();
				updateAdapterGUIinfos();
				validateConnectivity();
			}
		}).start();
		
		logger.info(this.getClass().getName() + " Started");
	}
	
////////////////////////// EVENT HANDLERS ACTION LISTENERS //////////////////////////
	
	private void addEventHandlers() {
		
		// COMBO ADAPTER SELECTOR SELECTION CHANGE
		comboAdapters.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						
						updateCalculatingDispLbl();
						resetSpeedTestValues();
						updateNetDisplayedINFO();
						validateConnectivity();
					}
				}).start();
			}
		});
		
		// SPEED CALCULATOR BUTTON LISTENER
		btnCalculateConnSpeed.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						resetSpeedTestValues();

						dynLblUPspeed.setText("Calculating...");
						dynLblDOWNspeed.setText("Calculating...");
						dynLblLatency.setText("Calculating...");
						
						calculateLatency();
						calculateDOWNspeed((FtpSpeedTestOptions)comboSpeedTestOpt.getSelectedItem());
						calculateUPspeed((FtpSpeedTestOptions)comboSpeedTestOpt.getSelectedItem());
					}
				}).start();
			}
		});
	}
	
////////////////////////// AND NOW SOME USEFUL METHOD //////////////////////////
	
	private void calculateLatency() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
				progressBarLatency.setValue(0);
				progressBarLatency.setForeground(Color.blue);
				try {
					int latency = NetworkUtils.retrieveLatency(currentSelAdapter.getNetworkInterface());
					dynLblLatency.setText(String.valueOf(latency)+" ms");
					progressBarLatency.setValue(100);
					progressBarLatency.setForeground(Color.blue);
				} catch (UnknownHostException e) {
					String msg = "Host "+GeneralConfig.testAddress+" not recognized";
					dynLblLatency.setText(msg);
					logger.error(msg, e);
					progressBarLatency.setValue(0);
					progressBarLatency.setForeground(Color.red);
				} catch (IOException e) {
					String msg = "IO error occcurred";
					dynLblLatency.setText(msg);
					logger.error(msg, e);
					progressBarLatency.setValue(0);
					progressBarLatency.setForeground(Color.red);
				}
			}
		}).start();
	}
	
	private void calculateUPspeed(FtpSpeedTestOptions options) {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		try {
			dynLblUPspeed.setText(NetworkUtils.retrieveUpSpeed(currentSelAdapter.getInetAddr(), options, progressBarUp));
		} catch (IOException | InterruptedException e) {
			String msg = "IO error occcurred";
			dynLblUPspeed.setText(msg);
			resetSpeedTestValues();
			logger.error(msg, e);
		}
	}
	
	private void calculateDOWNspeed(FtpSpeedTestOptions options) {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		try {
			dynLblDOWNspeed.setText(NetworkUtils.retrieveDownSpeed(currentSelAdapter.getInetAddr(), options, progressBarDown));
		} catch (IOException e) {
			String msg = "IO error occcurred";
			dynLblDOWNspeed.setText(msg);
			resetSpeedTestValues();
			logger.error(msg, e);
		}
	}
	
	private void validateConnectivity() {
		boolean networkUp = false;
		
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		if(currentSelAdapter.interfaceConfig != null) {
			networkUp = true;
		}
		
		btnCalculateConnSpeed.setEnabled(networkUp);
	}
	
	private void handleException(String msg, Throwable e) {
		logger.error(msg, e);
		JOptionPane.showMessageDialog(thisPanel, msg, msg, JOptionPane.ERROR_MESSAGE);
	}

	// interface related info
	private void updateNetDisplayedINFO() {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		dynLblAdapterName.setText(currentSelAdapter.name);
		dynLblLocalIP.setText(currentSelAdapter.locIP);
		dynLblMacAddress.setText(currentSelAdapter.locMAC);
		dynLblHostName.setText(currentSelAdapter.hostName);
		dynLblMTU.setText(String.valueOf(currentSelAdapter.getMTU()));
		NetInterfaceConfig netInfo = currentSelAdapter.interfaceConfig;
		String broadcastAddr = netInfo != null ? netInfo.getBroadcast() : errMSG;
		String subMask = netInfo != null ? netInfo.getNetmask() : errMSG;
		dynLblSubnetMask.setText(subMask);
		dynLblBroadcastAddr.setText(broadcastAddr);
		try {
			dynLblPublicIP.setText(netUtils.retrieveAdapterPublicIP(currentSelAdapter));
		} catch (IOException e1) {
			String msg = "Connection Error";
			dynLblPublicIP.setText(msg);
			handleException(msg, e1);
		}
	}
	
	private void updateCalculatingDispLbl() {
		
		dynLblAdapterName.setText(CALCULATING);
		dynLblLocalIP.setText(CALCULATING);
		dynLblMacAddress.setText(CALCULATING);
		dynLblHostName.setText(CALCULATING);
		dynLblMTU.setText(String.valueOf(CALCULATING));
		dynLblSubnetMask.setText(CALCULATING);
		dynLblBroadcastAddr.setText(CALCULATING);
		dynLblPublicIP.setText(CALCULATING);
	}
	
	// interface unrelated info
	private void updateAdapterGUIinfos() {
		
		try {
			NetInfo netInfo = NetworkUtils.getSysINFO().getNetInfo();
			dynLblDNS1.setText(netInfo.getPrimaryDns());
			dynLblDNS2.setText(netInfo.getSecondaryDns());
			dynLblDefGateway.setText(netInfo.getDefaultGateway());
		} catch (SigarException e) {
			String msg = "Error retrieving network Informations";
			dynLblDefGateway.setText(errMSG);
			dynLblDNS1.setText(errMSG);
			dynLblDNS2.setText(errMSG);
			handleException(msg, e);
		}
	}
	
	private void updateNetDisplayedERROR() {
		dynLblAdapterName.setText(errMSG);
		dynLblLocalIP.setText(errMSG);
		dynLblMacAddress.setText(errMSG);
		dynLblHostName.setText(errMSG);
		dynLblPublicIP.setText(errMSG);
		dynLblDNS1.setText(errMSG);
		dynLblDNS2.setText(errMSG);
	}
	
	private void reloadAdaptersCombo() {
		comboAdapters.removeAllItems();
		DefaultComboBoxModel<NetworkAdapter> model = new DefaultComboBoxModel<NetworkAdapter>( netUtils.getAdapters() );
		comboAdapters.setModel( model );
		comboAdapters.revalidate();
		comboAdapters.repaint();
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
	}
	
	private void resetSpeedTestValues() {
		progressBarDown.setValue(0);
		progressBarLatency.setValue(0);
		progressBarUp.setValue(0);
		dynLblDOWNspeed.setText(UNSELECTED);
		dynLblUPspeed.setText(UNSELECTED);
		dynLblLatency.setText(UNSELECTED);
		
		progressBarDown.setIndeterminate(false);
		progressBarLatency.setIndeterminate(false);
		progressBarUp.setIndeterminate(false);
	}
}
