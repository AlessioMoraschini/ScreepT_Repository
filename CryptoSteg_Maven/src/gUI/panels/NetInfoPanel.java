package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import uTILS.network.NetworkAdapter;
import uTILS.network.NetworkUtils;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JScrollPane;
import java.awt.Button;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JButton;

public class NetInfoPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(NetInfoPanel.class); 
	
	private static final String errMSG = "### ERROR ###";
	private static final String UNSELECTED = "##############";
	
	// flag to signal active
	public static boolean active=false;
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
	private JLabel lblOther;
	private JTextField dynLblAdapterName;
	private JTextField dynLblMacAddress;
	private JTextField dynLblLocalIP;
	private JTextField dynLblPublicIP;
	private JTextField dynLblOther;
	private JSeparator separator;
	
	// network related objects
	public NetInfoPanel thisPanel;
	public NetworkAdapter currentSelAdapter;
	private NetworkUtils netUtils;
	private JLabel labelLatency;
	private JLabel lblHostName;
	private JTextField dynLblHostName;
	private JLabel dynLblUPspeed;
	private JLabel dynLblDOWNspeed;
	private JLabel dynLblLatency;
	
	// CONSTRUCTOR
	
	public NetInfoPanel(INItializer currentConfig) {
		setMaximumSize(new Dimension(800, 450));
		setMinimumSize(new Dimension(750, 400));
		
		active=true;
		thisPanel = this;
		
		try {
			netUtils = new NetworkUtils();
		} catch (IOException e) {
			handleException("Exception initializing network informations", e);
		}
		
		initialize();
		
		addEventHandlers();
	}
	
	// METHODS
	
	private void initialize() {
	
		getContentPane().setBackground(new Color(210, 180, 140));
		getContentPane().setLayout(new MigLayout("", "[50px,grow][25%,grow][60%,grow][50px,grow]", "[70px][60px][75%]"));
		
		headerPanelTitle = new JPanel();
		headerPanelTitle.setMinimumSize(new Dimension(100, 10));
		headerPanelTitle.setForeground(SystemColor.menu);
		headerPanelTitle.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		headerPanelTitle.setBackground(new Color(169, 169, 169));
		getContentPane().add(headerPanelTitle, "cell 0 0 4 1,grow");
		headerPanelTitle.setLayout(new MigLayout("", "[grow,trailing][100%,grow,center][grow,trailing]", "[20px,grow]"));
		
		lblNetworkInfoTool = new JLabel("Network Info Tool");
		lblNetworkInfoTool.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNetworkInfoTool.setHorizontalTextPosition(SwingConstants.CENTER);
		lblNetworkInfoTool.setHorizontalAlignment(SwingConstants.CENTER);
		lblNetworkInfoTool.setForeground(Color.BLACK);
		lblNetworkInfoTool.setFont(new Font("Lithos Pro Regular", Font.BOLD, 19));
		lblNetworkInfoTool.setBackground(SystemColor.activeCaptionBorder);
		headerPanelTitle.add(lblNetworkInfoTool, "cell 1 0,alignx center,growy");
		
		lblSelectedNetworkAdapter = new JLabel("Selected Network Adapter :");
		lblSelectedNetworkAdapter.setForeground(new Color(128, 0, 0));
		lblSelectedNetworkAdapter.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblSelectedNetworkAdapter, "cell 1 1,alignx center");
		
		comboAdapters = new JComboBox<NetworkAdapter>();
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
		panel.setLayout(new MigLayout("", "[12.5%,grow][12.5%,grow][12.5%,grow][10px][12.5%,grow][12.5%,grow][12.5%,grow]", "[40,grow][40,grow][40,grow][40,grow][40,grow][40,grow]"));
		
		lblAdapterName = new JLabel("Adapter Name:");
		lblAdapterName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblAdapterName, "cell 0 0,alignx center");
		
		dynLblAdapterName = new JTextField("-");
		dynLblAdapterName.setEditable(false);
		dynLblAdapterName.setForeground(new Color(72, 61, 139));
		dynLblAdapterName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblAdapterName, "cell 1 0 2 1,growx");
		
		btnCalculateConnSpeed = new JButton("Calculate connection speed");
		btnCalculateConnSpeed.setMinimumSize(new Dimension(0, 40));
		btnCalculateConnSpeed.setFont(new Font("Segoe UI", Font.BOLD, 16));
		btnCalculateConnSpeed.setBackground(new Color(192, 192, 192));
		panel.add(btnCalculateConnSpeed, "cell 4 0 3 1,growx");
		
		lblHostName = new JLabel("Host Name:");
		lblHostName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblHostName, "cell 0 1,alignx center");
		
		dynLblHostName = new JTextField("-");
		dynLblHostName.setEditable(false);
		dynLblHostName.setForeground(new Color(72, 61, 139));
		dynLblHostName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblHostName, "cell 1 1 2 1,growx");
		
		lblMacAddress = new JLabel("Mac Address:");
		lblMacAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblMacAddress, "cell 0 2,alignx center");
		
		dynLblMacAddress = new JTextField("-");
		dynLblMacAddress.setEditable(false);
		dynLblMacAddress.setForeground(new Color(72, 61, 139));
		dynLblMacAddress.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblMacAddress, "cell 1 2 2 1,growx");
		
		separator = new JSeparator();
		separator.setBackground(new Color(0, 0, 0));
		separator.setOrientation(SwingConstants.VERTICAL);
		panel.add(separator, "cell 3 0 1 6,alignx center,growy");
		
		panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 51, 51), 2));
		panel.add(panel_1, "cell 4 1 3 5,grow");
		panel_1.setLayout(new MigLayout("", "[10%,grow][50%,grow]", "[30px,grow][30px,grow][30px,grow]"));
		
		lblUpload = new JLabel("Upload:");
		lblUpload.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(lblUpload, "cell 0 0,alignx center");
		
		dynLblUPspeed = new JLabel(UNSELECTED);
		dynLblUPspeed.setHorizontalAlignment(SwingConstants.CENTER);
		dynLblUPspeed.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblUPspeed, "cell 1 0,alignx center");
		
		lblDownload = new JLabel("Download:");
		lblDownload.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(lblDownload, "cell 0 1,alignx center");
		
		dynLblDOWNspeed = new JLabel(UNSELECTED);
		dynLblDOWNspeed.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblDOWNspeed, "cell 1 1,alignx center");
		
		labelLatency = new JLabel("Latency:");
		labelLatency.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(labelLatency, "cell 0 2,alignx center");
		
		dynLblLatency = new JLabel(UNSELECTED);
		dynLblLatency.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel_1.add(dynLblLatency, "cell 1 2,alignx center");
		
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
		
		dynLblPublicIP = new JTextField("-");
		dynLblPublicIP.setEditable(false);
		dynLblPublicIP.setForeground(new Color(72, 61, 139));
		dynLblPublicIP.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblPublicIP, "cell 1 4 2 1,growx");
		
		lblOther = new JLabel("Other:");
		lblOther.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(lblOther, "cell 0 5,alignx center");
		
		dynLblOther = new JTextField(UNSELECTED);
		dynLblOther.setEditable(false);
		dynLblOther.setForeground(new Color(72, 61, 139));
		dynLblOther.setFont(new Font("Segoe UI", Font.BOLD, 14));
		panel.add(dynLblOther, "cell 1 5 2 1,growx");
		
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: "+NetInfoPanel.class.getName());	        	
				active = false;
	        	dispose();
		    }
		});
		
		// call now cause before some gui elements can not be initialized yet
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				updateNetDisplayedINFO();
			}
		}).start();
	}
	
	private void addEventHandlers() {
		
		// COMBO ADAPTER SELECTOR SELECTION CHANGE
		comboAdapters.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateNetDisplayedINFO();
			}
		});
	}
	
	private void handleException(String msg, Throwable e) {
		logger.error(msg, e);
		JOptionPane.showMessageDialog(thisPanel, msg, msg, JOptionPane.ERROR_MESSAGE);
	}

	private void updateNetDisplayedINFO() {
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
		dynLblAdapterName.setText(currentSelAdapter.name);
		dynLblLocalIP.setText(currentSelAdapter.locIP);
		dynLblMacAddress.setText(currentSelAdapter.locMAC);
		dynLblHostName.setText(currentSelAdapter.hostName);
		try {
			dynLblPublicIP.setText(netUtils.retrieveAdapterPublicIP(currentSelAdapter));
		} catch (IOException e1) {
			String msg = "Connection Error";
			dynLblPublicIP.setText(msg);
			handleException(msg, e1);
		}
		dynLblOther.setText(UNSELECTED);
	}
	
	private void updateNetDisplayedERROR() {
		dynLblAdapterName.setText(errMSG);
		dynLblLocalIP.setText(errMSG);
		dynLblMacAddress.setText(errMSG);
		dynLblHostName.setText(errMSG);
		dynLblPublicIP.setText(errMSG);
		dynLblOther.setText(errMSG);
	}
	
	private void reloadAdaptersCombo() {
		comboAdapters.removeAllItems();
		DefaultComboBoxModel<NetworkAdapter> model = new DefaultComboBoxModel<NetworkAdapter>( netUtils.getAdapters() );
		comboAdapters.setModel( model );
		//comboTemplateSelector = new JComboBox<String>(retrieveTemplatesFilesFromDir(TEMPLATES_FOLDER));
		comboAdapters.revalidate();
		comboAdapters.repaint();
		currentSelAdapter = (NetworkAdapter) comboAdapters.getSelectedItem();
	}
}
