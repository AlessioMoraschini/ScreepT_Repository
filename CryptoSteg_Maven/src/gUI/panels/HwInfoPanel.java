package gUI.panels;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import iNIT.INItializer;
import net.miginfocom.swing.MigLayout;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JComboBox;

public class HwInfoPanel extends JFrame{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(HwInfoPanel.class); 
	
	private static final String errMSG = "### ERROR ###";
	private static final String UNSELECTED = "##############";
	
	// flag to signal active
	public static boolean active=false;
	private JLabel lblHardwareInfoTool;
	private JPanel panelHeaderTitle;
	private JScrollPane ConncetionInfoScrollPanel;
	private JPanel mainContainerPanel;
	private JLabel lblJavaVersion;
	private JLabel lblOsName;
	private JTextField dynLblOsName;
	private JTextField dynLblJavaVersion;
	
	// network related objects
	public HwInfoPanel thisPanel;
	private JLabel lblOsVersion;
	private JTextField dynLblOsVersion;
	private JLabel lblRam;
	private JLabel lblFreeRam;
	private JLabel lblUsedRam;
	private JTextField dynLblRam;
	private JTextField dynLblFreeRam;
	private JTextField textField_2;
	private JSeparator separator;
	private JPanel panelRAM;
	private JLabel lblArchitecture;
	private JTextField dynLblArch;
	private JPanel panelGeneral;
	private JPanel panelCPU;
	private JLabel lblCPU;
	private JTextField dynLblCPU;
	private JLabel lblCpuCores;
	private JTextField dynLblNofCores;
	private JLabel lblVendor;
	private JTextField dynLblVendor;
	private JLabel lblNofSockets;
	private JTextField dynLblNofSockets;
	private JLabel lblCpuFrequency;
	private JLabel lblCacheSize;
	private JTextField dynLblCpuFrequency;
	private JTextField dynLblCacheSize;
	private JPanel panelMOBO;
	private JLabel lblMotherboard;
	private JTextField dynLblMoboModel;
	private JLabel lblMoboVendor;
	private JTextField dynLblMoboVendor;
	private JTextField dynLblSerialNumber;
	private JLabel lblSerialNumber;
	private JPanel panelHD;
	private JLabel lblHd;
	private JComboBox comboBox;
	private JLabel lblReadSpeed;
	private JLabel lblWriteSpeed;
	private JTextField textField;
	private JTextField textField_1;
	private JPanel panelMonitors;
	private JLabel lblDisplay;
	private JComboBox comboBox_1;
	private JLabel label;
	private JTextField textField_3;
	private JLabel label_1;
	private JTextField textField_4;
	
////////////////////////// CONSTRUCTOR //////////////////////////
	
	public HwInfoPanel(INItializer currentConfig) {
		setMaximumSize(new Dimension(800, 450));
		setMinimumSize(new Dimension(750, 400));
		
		active = true;
		thisPanel = this;
		
		initialize();
		
		addEventHandlers();
	}
	
////////////////////////// INITIALIZER METHOD //////////////////////////
	
	private void initialize() {
	
		getContentPane().setBackground(new Color(0, 0, 0));
		getContentPane().setLayout(new MigLayout("", "[50px,grow][25%,grow][60%,grow][50px,grow]", "[70px][60px][75%]"));
		
		panelHeaderTitle = new JPanel();
		panelHeaderTitle.setMinimumSize(new Dimension(100, 10));
		panelHeaderTitle.setForeground(SystemColor.menu);
		panelHeaderTitle.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		panelHeaderTitle.setBackground(new Color(70, 130, 180));
		getContentPane().add(panelHeaderTitle, "cell 0 0 4 1,grow");
		panelHeaderTitle.setLayout(new MigLayout("", "[grow,trailing][100%,grow,center][grow,trailing]", "[20px,grow]"));
		
		lblHardwareInfoTool = new JLabel("System Info Tool");
		lblHardwareInfoTool.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblHardwareInfoTool.setHorizontalTextPosition(SwingConstants.CENTER);
		lblHardwareInfoTool.setHorizontalAlignment(SwingConstants.CENTER);
		lblHardwareInfoTool.setForeground(new Color(0, 0, 0));
		lblHardwareInfoTool.setFont(new Font("Lithos Pro Regular", Font.BOLD, 24));
		lblHardwareInfoTool.setBackground(SystemColor.activeCaptionBorder);
		panelHeaderTitle.add(lblHardwareInfoTool, "cell 1 0,alignx center,growy");
		
		ConncetionInfoScrollPanel = new JScrollPane();
		ConncetionInfoScrollPanel.setBackground(Color.WHITE);
		ConncetionInfoScrollPanel.setBorder(new LineBorder(Color.DARK_GRAY, 4));
		getContentPane().add(ConncetionInfoScrollPanel, "cell 0 1 4 2,grow");
		
		mainContainerPanel = new JPanel();
		mainContainerPanel.setBackground(new Color(211, 211, 211));
		ConncetionInfoScrollPanel.setViewportView(mainContainerPanel);
		mainContainerPanel.setLayout(new MigLayout("", "[12.5%,grow][12.5%,grow][12.5%,grow][12.5%,grow]", "[40,grow][5][50,grow][50,grow][50,grow][40,grow][40,grow][40,grow][40,grow][40,grow]"));
		
		panelGeneral = new JPanel();
		panelGeneral.setBackground(Color.DARK_GRAY);
		mainContainerPanel.add(panelGeneral, "cell 0 0 4 1,grow");
		panelGeneral.setLayout(new MigLayout("", "[100][grow][grow][grow][grow][grow][grow][grow]", "[grow]"));
		
		lblArchitecture = new JLabel("Architecture:");
		lblArchitecture.setForeground(Color.LIGHT_GRAY);
		panelGeneral.add(lblArchitecture, "cell 0 0,alignx center");
		lblArchitecture.setFont(new Font("Segoe UI", Font.BOLD, 16));
		
		dynLblArch = new JTextField("##############");
		panelGeneral.add(dynLblArch, "cell 1 0,growx");
		dynLblArch.setForeground(new Color(72, 61, 139));
		dynLblArch.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblArch.setEditable(false);
		
		lblOsName = new JLabel("OS Name");
		lblOsName.setForeground(Color.LIGHT_GRAY);
		panelGeneral.add(lblOsName, "cell 2 0,alignx center");
		lblOsName.setFont(new Font("Segoe UI", Font.BOLD, 16));
		
		dynLblOsName = new JTextField(UNSELECTED);
		panelGeneral.add(dynLblOsName, "cell 3 0,growx");
		dynLblOsName.setEditable(false);
		dynLblOsName.setForeground(new Color(72, 61, 139));
		dynLblOsName.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblOsVersion = new JLabel("OS Version");
		lblOsVersion.setForeground(Color.LIGHT_GRAY);
		panelGeneral.add(lblOsVersion, "cell 4 0,alignx center");
		lblOsVersion.setFont(new Font("Segoe UI", Font.BOLD, 16));
		
		dynLblOsVersion = new JTextField(UNSELECTED);
		panelGeneral.add(dynLblOsVersion, "cell 5 0,growx");
		dynLblOsVersion.setEditable(false);
		dynLblOsVersion.setForeground(new Color(72, 61, 139));
		dynLblOsVersion.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		lblJavaVersion = new JLabel("Java Version");
		lblJavaVersion.setForeground(Color.LIGHT_GRAY);
		panelGeneral.add(lblJavaVersion, "cell 6 0,alignx center");
		lblJavaVersion.setFont(new Font("Segoe UI", Font.BOLD, 16));
		
		dynLblJavaVersion = new JTextField(UNSELECTED);
		panelGeneral.add(dynLblJavaVersion, "cell 7 0,growx");
		dynLblJavaVersion.setEditable(false);
		dynLblJavaVersion.setForeground(new Color(72, 61, 139));
		dynLblJavaVersion.setFont(new Font("Segoe UI", Font.BOLD, 14));
		
		separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.DARK_GRAY);
		mainContainerPanel.add(separator, "cell 0 1 4 1,growx");
		
		panelRAM = new JPanel();
		panelRAM.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
		panelRAM.setBackground(new Color(169, 169, 169));
		mainContainerPanel.add(panelRAM, "cell 0 2 4 1,grow");
		panelRAM.setLayout(new MigLayout("", "[100][160,grow][80][40,grow][100][40,grow]", "[15px][grow]"));
		
		lblRam = new JLabel("RAM:");
		panelRAM.add(lblRam, "cell 0 0 1 2,alignx center");
		lblRam.setFont(new Font("Segoe UI", Font.BOLD, 18));
		
		dynLblRam = new JTextField("##############");
		panelRAM.add(dynLblRam, "cell 1 0 1 2,growx");
		dynLblRam.setForeground(new Color(72, 61, 139));
		dynLblRam.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblRam.setEditable(false);
		
		lblFreeRam = new JLabel("Free:");
		panelRAM.add(lblFreeRam, "cell 2 0 1 2,alignx center");
		lblFreeRam.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		
		dynLblFreeRam = new JTextField("##############");
		panelRAM.add(dynLblFreeRam, "cell 3 0 1 2,growx");
		dynLblFreeRam.setForeground(new Color(72, 61, 139));
		dynLblFreeRam.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblFreeRam.setEditable(false);
		
		lblUsedRam = new JLabel("Used:");
		panelRAM.add(lblUsedRam, "cell 4 0 1 2,alignx center");
		lblUsedRam.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		
		textField_2 = new JTextField("##############");
		panelRAM.add(textField_2, "cell 5 0 1 2,growx");
		textField_2.setForeground(new Color(72, 61, 139));
		textField_2.setFont(new Font("Segoe UI", Font.BOLD, 14));
		textField_2.setEditable(false);
		
		panelCPU = new JPanel();
		panelCPU.setBorder(new LineBorder(new Color(64, 64, 64), 2, true));
		panelCPU.setBackground(new Color(169, 169, 169));
		mainContainerPanel.add(panelCPU, "cell 0 3 4 3,grow");
		panelCPU.setLayout(new MigLayout("", "[100][160px,grow][grow][grow][100px,grow][grow]", "[grow][grow][grow]"));
		
		lblCPU = new JLabel("CPU:");
		lblCPU.setFont(new Font("Segoe UI", Font.BOLD, 18));
		panelCPU.add(lblCPU, "cell 0 0,alignx center");
		
		dynLblCPU = new JTextField("##############");
		dynLblCPU.setForeground(new Color(72, 61, 139));
		dynLblCPU.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblCPU.setEditable(false);
		panelCPU.add(dynLblCPU, "cell 1 0,growx");
		
		lblNofSockets = new JLabel("Number of Sockets:");
		lblNofSockets.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelCPU.add(lblNofSockets, "cell 2 0,alignx center");
		
		dynLblNofSockets = new JTextField("##############");
		dynLblNofSockets.setForeground(new Color(72, 61, 139));
		dynLblNofSockets.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblNofSockets.setEditable(false);
		panelCPU.add(dynLblNofSockets, "cell 3 0,growx");
		
		lblCpuFrequency = new JLabel("Cpu Frequency:");
		lblCpuFrequency.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelCPU.add(lblCpuFrequency, "cell 4 0,alignx center");
		
		dynLblCpuFrequency = new JTextField("##############");
		dynLblCpuFrequency.setForeground(new Color(72, 61, 139));
		dynLblCpuFrequency.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblCpuFrequency.setEditable(false);
		panelCPU.add(dynLblCpuFrequency, "cell 5 0,growx");
		
		lblVendor = new JLabel("Vendor:");
		panelCPU.add(lblVendor, "cell 0 1,alignx center");
		lblVendor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		
		dynLblVendor = new JTextField("##############");
		dynLblVendor.setForeground(new Color(72, 61, 139));
		dynLblVendor.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblVendor.setEditable(false);
		panelCPU.add(dynLblVendor, "cell 1 1,growx");
		
		lblCpuCores = new JLabel("Number of Cores:");
		lblCpuCores.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelCPU.add(lblCpuCores, "cell 2 1,alignx center");
		
		dynLblNofCores = new JTextField("##############");
		dynLblNofCores.setForeground(new Color(72, 61, 139));
		dynLblNofCores.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblNofCores.setEditable(false);
		panelCPU.add(dynLblNofCores, "cell 3 1,growx");
		
		lblCacheSize = new JLabel("Cache Size:");
		lblCacheSize.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelCPU.add(lblCacheSize, "cell 4 1,alignx center");
		
		dynLblCacheSize = new JTextField("##############");
		dynLblCacheSize.setForeground(new Color(72, 61, 139));
		dynLblCacheSize.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblCacheSize.setEditable(false);
		panelCPU.add(dynLblCacheSize, "cell 5 1,growx");
		
		panelMOBO = new JPanel();
		panelMOBO.setBorder(new LineBorder(new Color(64, 64, 64), 2, true));
		panelMOBO.setBackground(new Color(169, 169, 169));
		mainContainerPanel.add(panelMOBO, "cell 0 6 4 1,grow");
		panelMOBO.setLayout(new MigLayout("", "[100][160][110][grow][130][grow]", "[grow]"));
		
		lblMotherboard = new JLabel("MOBO:");
		lblMotherboard.setFont(new Font("Segoe UI", Font.BOLD, 18));
		panelMOBO.add(lblMotherboard, "cell 0 0,alignx center");
		
		dynLblMoboModel = new JTextField("##############");
		dynLblMoboModel.setForeground(new Color(72, 61, 139));
		dynLblMoboModel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblMoboModel.setEditable(false);
		panelMOBO.add(dynLblMoboModel, "cell 1 0,growx");
		
		lblMoboVendor = new JLabel("Vendor:");
		lblMoboVendor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelMOBO.add(lblMoboVendor, "cell 2 0,alignx center");
		
		dynLblMoboVendor = new JTextField("##############");
		dynLblMoboVendor.setForeground(new Color(72, 61, 139));
		dynLblMoboVendor.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblMoboVendor.setEditable(false);
		panelMOBO.add(dynLblMoboVendor, "cell 3 0,growx");
		
		lblSerialNumber = new JLabel("Serial Number:");
		lblSerialNumber.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelMOBO.add(lblSerialNumber, "cell 4 0,alignx center");
		
		dynLblSerialNumber = new JTextField("##############");
		dynLblSerialNumber.setForeground(new Color(72, 61, 139));
		dynLblSerialNumber.setFont(new Font("Segoe UI", Font.BOLD, 14));
		dynLblSerialNumber.setEditable(false);
		panelMOBO.add(dynLblSerialNumber, "cell 5 0,growx");
		
		panelHD = new JPanel();
		panelHD.setBorder(new LineBorder(new Color(64, 64, 64), 2, true));
		panelHD.setBackground(new Color(169, 169, 169));
		mainContainerPanel.add(panelHD, "cell 0 7 4 1,grow");
		panelHD.setLayout(new MigLayout("", "[100][160][110][grow][130px][grow]", "[]"));
		
		lblHd = new JLabel("HD:");
		lblHd.setFont(new Font("Segoe UI", Font.BOLD, 18));
		panelHD.add(lblHd, "cell 0 0,alignx center");
		
		comboBox = new JComboBox();
		panelHD.add(comboBox, "cell 1 0,growx");
		
		lblReadSpeed = new JLabel("Read Speed:");
		lblReadSpeed.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelHD.add(lblReadSpeed, "cell 2 0,alignx center");
		
		textField = new JTextField("##############");
		textField.setForeground(new Color(72, 61, 139));
		textField.setFont(new Font("Segoe UI", Font.BOLD, 14));
		textField.setEditable(false);
		panelHD.add(textField, "cell 3 0,growx");
		
		lblWriteSpeed = new JLabel("Write Speed:");
		lblWriteSpeed.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelHD.add(lblWriteSpeed, "cell 4 0,alignx center");
		
		textField_1 = new JTextField("##############");
		textField_1.setForeground(new Color(72, 61, 139));
		textField_1.setFont(new Font("Segoe UI", Font.BOLD, 14));
		textField_1.setEditable(false);
		panelHD.add(textField_1, "cell 5 0,growx");
		
		panelMonitors = new JPanel();
		panelMonitors.setBorder(new LineBorder(new Color(64, 64, 64), 2, true));
		panelMonitors.setBackground(new Color(169, 169, 169));
		mainContainerPanel.add(panelMonitors, "cell 0 8 4 1,grow");
		panelMonitors.setLayout(new MigLayout("", "[100][160][110][grow][130px][grow]", "[]"));
		
		lblDisplay = new JLabel("DISPLAY:");
		lblDisplay.setFont(new Font("Segoe UI", Font.BOLD, 18));
		panelMonitors.add(lblDisplay, "cell 0 0,alignx center");
		
		comboBox_1 = new JComboBox();
		panelMonitors.add(comboBox_1, "cell 1 0,growx");
		
		label = new JLabel("Read Speed:");
		label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelMonitors.add(label, "cell 2 0,alignx center");
		
		textField_3 = new JTextField("##############");
		textField_3.setForeground(new Color(72, 61, 139));
		textField_3.setFont(new Font("Segoe UI", Font.BOLD, 14));
		textField_3.setEditable(false);
		panelMonitors.add(textField_3, "cell 3 0,growx");
		
		label_1 = new JLabel("Write Speed:");
		label_1.setFont(new Font("Segoe UI", Font.PLAIN, 16));
		panelMonitors.add(label_1, "cell 4 0,alignx center");
		
		textField_4 = new JTextField("##############");
		textField_4.setForeground(new Color(72, 61, 139));
		textField_4.setFont(new Font("Segoe UI", Font.BOLD, 14));
		textField_4.setEditable(false);
		panelMonitors.add(textField_4, "cell 5 0,growx");
		
		// default close operation(same as reset) 
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: "+HwInfoPanel.class.getName());	        	
				active = false;
	        	dispose();
		    }
		});
		
		// call now cause before some gui elements can not be initialized yet
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
			}
		}).start();
	}
	
////////////////////////// EVENT HANDLERS ACTION LISTENERS //////////////////////////
	
	private void addEventHandlers() {
		
		
	}
	
////////////////////////// AND NOW SOME USEFUL METHOD //////////////////////////
	
	
	private void handleException(String msg, Throwable e) {
		logger.error(msg, e);
		JOptionPane.showMessageDialog(thisPanel, msg, msg, JOptionPane.ERROR_MESSAGE);
	}
}
