package network.scanner.dmad;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JSpinner;

import org.ini4j.Ini;
import org.ini4j.Wini;
import org.jnetpcap.Pcap;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import network.scanner.Dumper;

import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SpinnerNumberModel;
import javax.swing.Box;
import javax.swing.border.MatteBorder;
import javax.swing.SpinnerDateModel;
import javax.swing.JCheckBox;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Calendar;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;



public class csNa_GUI {

	private JFrame frmCsnaBy;
	private JTextArea ConsoleFTP;
	private JTextField KEY;
	private JTextField HOST;
	private JTextField USER;
	private JTextField DIR;
	private JTextField EXPR_FILTER;
	private JTextField ExportUserPath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					csNa_GUI window = new csNa_GUI();
					window.frmCsnaBy.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public csNa_GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCsnaBy = new JFrame();
		frmCsnaBy.setFont(new Font("Dialog", Font.BOLD, 21));
		frmCsnaBy.setBackground(Color.WHITE);
		frmCsnaBy.setTitle("DMAD - Download, Merge, Analyze and Dump - by Alessio");
		frmCsnaBy.getContentPane().setBackground(SystemColor.activeCaption);
		frmCsnaBy.setBounds(100, 100, 1298, 707);
		frmCsnaBy.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCsnaBy.getContentPane().setLayout(null);
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(7, 15, 51, 20);
		lblPort.setFont(new Font("SansSerif", Font.PLAIN, 15));
		lblPort.setBackground(Color.LIGHT_GRAY);
		lblPort.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(lblPort);
		
		JLabel lblFtpHost = new JLabel("Host");
		lblFtpHost.setBounds(7, 60, 51, 20);
		lblFtpHost.setFont(new Font("SansSerif", Font.PLAIN, 15));
		lblFtpHost.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(lblFtpHost);
		
		JLabel lblKey = new JLabel("Key");
		lblKey.setBounds(134, 15, 34, 20);
		lblKey.setFont(new Font("SansSerif", Font.PLAIN, 15));
		lblKey.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(lblKey);
		
		JLabel lblUtilizzaI = new JLabel("-> Utilizza i Dump filtrati ");
		lblUtilizzaI.setForeground(UIManager.getColor("Button.light"));
		lblUtilizzaI.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblUtilizzaI.setHorizontalAlignment(SwingConstants.CENTER);
		lblUtilizzaI.setBounds(1062, 197, 210, 20);
		frmCsnaBy.getContentPane().add(lblUtilizzaI);
		
		JLabel lblDir = new JLabel("User");
		lblDir.setBounds(7, 103, 51, 20);
		lblDir.setFont(new Font("SansSerif", Font.PLAIN, 15));
		lblDir.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(lblDir);
		
		JLabel lblFtpsLog = new JLabel("MODULO FTPS");
		lblFtpsLog.setBounds(400, 192, 144, 22);
		lblFtpsLog.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblFtpsLog.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(lblFtpsLog);
		
		JLabel lblMainConsole = new JLabel("DMAD - Console");
		lblMainConsole.setBackground(new Color(230, 230, 250));
		lblMainConsole.setBounds(154, 281, 253, 38);
		lblMainConsole.setHorizontalAlignment(SwingConstants.CENTER);
		lblMainConsole.setFont(new Font("Tahoma", Font.PLAIN, 30));
		frmCsnaBy.getContentPane().add(lblMainConsole);
		
		ExportUserPath = new JTextField();
		ExportUserPath.setBackground(new Color(255, 245, 238));
		ExportUserPath.setHorizontalAlignment(SwingConstants.CENTER);
		ExportUserPath.setFont(new Font("Tahoma", Font.PLAIN, 14));
		ExportUserPath.setText("DMAD_Export_Utente");
		ExportUserPath.setBounds(1002, 241, 228, 26);
		frmCsnaBy.getContentPane().add(ExportUserPath);
		ExportUserPath.setColumns(10);
		
		JLabel exportMSG = new JLabel("Salva come: \\Export\\Export_Pt0_");
		exportMSG.setHorizontalAlignment(SwingConstants.CENTER);
		exportMSG.setBounds(699, 238, 303, 32);
		exportMSG.setFont(new Font("Tahoma", Font.PLAIN, 18));
		frmCsnaBy.getContentPane().add(exportMSG);
		
		JSpinner portFilter = new JSpinner();
		portFilter.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				exportMSG.setText("Salva come: \\Export\\Export_Pt"+(Integer)portFilter.getValue()+"_");
				}
		});
		portFilter.setBounds(735, 112, 90, 32);
		portFilter.setFont(new Font("Tahoma", Font.PLAIN, 18));
		portFilter.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		frmCsnaBy.getContentPane().add(portFilter);
		
		JRadioButton EXPR_FLAG = new JRadioButton("Filtro Payload");
		EXPR_FLAG.setBounds(656, 156, 152, 25);
		EXPR_FLAG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(EXPR_FLAG.isSelected()) {
					EXPR_FILTER.setEnabled(true);
				}else {
					EXPR_FILTER.setEnabled(false);
				}
			}
		});
		
		JCheckBox FTP_DELETE_FLAG = new JCheckBox("/Files_FTP");
		FTP_DELETE_FLAG.setHorizontalAlignment(SwingConstants.CENTER);
		FTP_DELETE_FLAG.setOpaque(false);
		FTP_DELETE_FLAG.setFont(new Font("Tahoma", Font.PLAIN, 18));
		FTP_DELETE_FLAG.setBackground(SystemColor.activeCaption);
		FTP_DELETE_FLAG.setBounds(665, 10, 159, 29);
		frmCsnaBy.getContentPane().add(FTP_DELETE_FLAG);
		
		JRadioButton PORT_YN = new JRadioButton("Porta");
		PORT_YN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(PORT_YN.isSelected()) {
					portFilter.setEnabled(true);
				}else {
					portFilter.setEnabled(false);
				}
			}
		});
		
		PORT_YN.setSelected(true);
		PORT_YN.setFont(new Font("SansSerif", Font.PLAIN, 18));
		PORT_YN.setBackground(SystemColor.activeCaption);
		PORT_YN.setBounds(656, 114, 77, 29);
		frmCsnaBy.getContentPane().add(PORT_YN);
		
		JCheckBox USER_DELETE_FLAG = new JCheckBox("/Files_Utente");
		USER_DELETE_FLAG.setHorizontalAlignment(SwingConstants.CENTER);
		USER_DELETE_FLAG.setOpaque(false);
		USER_DELETE_FLAG.setFont(new Font("Tahoma", Font.PLAIN, 18));
		USER_DELETE_FLAG.setBackground(SystemColor.activeCaption);
		USER_DELETE_FLAG.setBounds(665, 40, 181, 29);
		frmCsnaBy.getContentPane().add(USER_DELETE_FLAG);
		
		JCheckBox EXPORT_DELETE_FLAG = new JCheckBox("/Export");
		EXPORT_DELETE_FLAG.setHorizontalAlignment(SwingConstants.CENTER);
		EXPORT_DELETE_FLAG.setFont(new Font("Tahoma", Font.PLAIN, 18));
		EXPORT_DELETE_FLAG.setOpaque(false);
		EXPORT_DELETE_FLAG.setBackground(SystemColor.activeCaption);
		EXPORT_DELETE_FLAG.setBounds(831, 5, 144, 64);
		frmCsnaBy.getContentPane().add(EXPORT_DELETE_FLAG);
		EXPR_FLAG.setBackground(SystemColor.activeCaption);
		EXPR_FLAG.setFont(new Font("SansSerif", Font.PLAIN, 18));
		frmCsnaBy.getContentPane().add(EXPR_FLAG);
		
		JRadioButton FLOW_CONTR_FLAG = new JRadioButton("Flow Control TCP");
		FLOW_CONTR_FLAG.setBounds(656, 195, 181, 25);
		FLOW_CONTR_FLAG.setBackground(SystemColor.activeCaption);
		FLOW_CONTR_FLAG.setFont(new Font("SansSerif", Font.PLAIN, 18));
		frmCsnaBy.getContentPane().add(FLOW_CONTR_FLAG);
		
		JRadioButton DEEP_SCAN_FLAG = new JRadioButton("Scansione approfondita");
		DEEP_SCAN_FLAG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (DEEP_SCAN_FLAG.isSelected()) {
					lblUtilizzaI.setText("-> Utilizza i Dump \"NOfilter\"");
					lblUtilizzaI.setForeground(Color.black);
				}
				else {
					lblUtilizzaI.setText("-> Utilizza i Dump filtrati");
					lblUtilizzaI.setForeground(UIManager.getColor("Button.light"));
				}
					
					
			}
		});
		DEEP_SCAN_FLAG.setBounds(840, 195, 225, 24);
		DEEP_SCAN_FLAG.setBackground(SystemColor.activeCaption);
		DEEP_SCAN_FLAG.setFont(new Font("SansSerif", Font.PLAIN, 18));
		frmCsnaBy.getContentPane().add(DEEP_SCAN_FLAG);
		
		JSpinner PORTFTP = new JSpinner();
		PORTFTP.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		PORTFTP.setBounds(66, 7, 64, 34);
		PORTFTP.setFont(new Font("Tahoma", Font.PLAIN, 16));
		frmCsnaBy.getContentPane().add(PORTFTP);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(7, 329, 1266, 318);
		frmCsnaBy.getContentPane().add(scrollPane_1);
		
		JTextArea csNaLOG = new JTextArea();
		csNaLOG.setEditable(false);
		scrollPane_1.setViewportView(csNaLOG);
		
		JButton Check = new JButton("Connetti al server");
		Check.setBounds(7, 187, 181, 33);
		Check.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {				
				ConsoleFTP.append("\n>> Lettura dati\n");
				Check.setEnabled(false);
				String host=HOST.getText();
				String user=USER.getText();
				String key=KEY.getText();
				String FTPdir=DIR.getText();
				int portftp=21;	
				try {
					portftp=((Integer)PORTFTP.getValue());
				}catch(Exception e) {
					PORTFTP.setValue(Integer.valueOf(21));
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> La porta deve essere un numero!");
				}
				ConsoleFTP.append("\n>> Connessione in corso...");
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length()-1);
				final int p=portftp;
				new Thread(new Runnable() {
				    public void run() {
				        //apro un thread per non bloccare il messaggio iniziale
				boolean check=false; 
				Dumper dumper;
				String []file=null;
				try {
					dumper=new Dumper(host, user, key, p);
					check=Dumper.CheckFTP(host, user, key, p);
					file=dumper.GetRemoteFiles("/"+FTPdir);
					Check.setEnabled(true);
				}catch (Exception e) {
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> Connessione fallita.\n");
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
					csNaLOG.append("\n>> Connessione fallita.\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					Check.setEnabled(true);
				}
				Check.setEnabled(true);
				StringTokenizer tokenizer;
				
				if(check) {
					ConsoleFTP.append("\n>> Porta ftp: "+((Integer)PORTFTP.getValue()).toString());
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> "+user+"\n>> Connessione riuscita.\n");
					csNaLOG.append("\n>> Lettura files nella cartella: "+FTPdir+"\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				else {
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> Connessione fallita.\n");
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
					csNaLOG.append("\n>> Connessione fallita.\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					JOptionPane.showMessageDialog(frmCsnaBy, "Errore nella connessione a: "+HOST.getText());
				}
				for (String s:file) {
					tokenizer=new StringTokenizer(s, "-");
					String token=tokenizer.nextToken();
					if(token.equals("NOfilter")) {
						csNaLOG.append("\n -> "+s+": rilevato DUMP non filtrato");
					}else if(token.equals("Port")) {
						csNaLOG.append("\n -> "+s+": rilevato DUMP filtrato");
					}
					
					else {
						csNaLOG.append("\n\n   # "+s+" : File generico/directory -> non verr� scaricato da DMAD");
					}
				}
				ConsoleFTP.append("\n\n>> FINE, DMAD pronto:");
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				csNaLOG.setSelectionStart(csNaLOG.getText().length());
				
				    }
				}).start();	
				
			}
		});
		
		
		csNaLOG.setFont(new Font("Monospaced", Font.PLAIN, 12));
		csNaLOG.setTabSize(12);
		csNaLOG.setText("DMAD pronto:\r\n>>");
		csNaLOG.setBackground(new Color(255, 255, 255));
		Check.setFont(new Font("SansSerif", Font.BOLD, 16));
		Check.setBackground(new Color(240, 255, 255));
		frmCsnaBy.getContentPane().add(Check);
		
		KEY = new JTextField();
		KEY.setHorizontalAlignment(SwingConstants.CENTER);
		KEY.setBounds(172, 9, 144, 32);
		KEY.setFont(new Font("SansSerif", Font.PLAIN, 15));
		frmCsnaBy.getContentPane().add(KEY);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(325, 9, 313, 173);
		frmCsnaBy.getContentPane().add(scrollPane);
		
		ConsoleFTP = new JTextArea();
		ConsoleFTP.setBackground(new Color(255, 255, 240));
		scrollPane.setViewportView(ConsoleFTP);
		ConsoleFTP.setFont(new Font("SansSerif", Font.PLAIN, 13));
		ConsoleFTP.setText("DMAD PRONTO:\r\n>>");
		ConsoleFTP.setEditable(false);
		ConsoleFTP.setColumns(10);
		
		HOST = new JTextField();
		HOST.setHorizontalAlignment(SwingConstants.CENTER);
		HOST.setBounds(66, 54, 250, 32);
		HOST.setFont(new Font("SansSerif", Font.PLAIN, 15));
		frmCsnaBy.getContentPane().add(HOST);
		HOST.setColumns(10);
		
		USER = new JTextField();
		USER.setHorizontalAlignment(SwingConstants.CENTER);
		USER.setBounds(66, 98, 250, 32);
		USER.setFont(new Font("SansSerif", Font.PLAIN, 15));
		USER.setColumns(10);
		frmCsnaBy.getContentPane().add(USER);
		
		DIR = new JTextField();
		DIR.setHorizontalAlignment(SwingConstants.CENTER);
		DIR.setBounds(66, 142, 250, 32);
		DIR.setFont(new Font("SansSerif", Font.PLAIN, 15));
		DIR.setColumns(10);
		frmCsnaBy.getContentPane().add(DIR);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setBounds(400, 189, 144, 28);
		horizontalBox_1.setBackground(SystemColor.inactiveCaption);
		horizontalBox_1.setBorder(new MatteBorder(1, 1, 1, 1, (Color) SystemColor.menuText));
		frmCsnaBy.getContentPane().add(horizontalBox_1);
		
		JLabel label = new JLabel("DIR");
		label.setBounds(7, 148, 51, 20);
		label.setFont(new Font("SansSerif", Font.PLAIN, 15));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(label);
		
		JButton CLC = new JButton("CLC");
		CLC.setBounds(7, 281, 129, 38);
		CLC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				ConsoleFTP.setText("DMAD PRONTO \n>>" );
				csNaLOG.setText("DMAD pronto:\n>>\n");
								
			}
		});
		CLC.setFont(new Font("SansSerif", Font.BOLD, 18));
		CLC.setBackground(SystemColor.inactiveCaption);
		frmCsnaBy.getContentPane().add(CLC);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(646, 7, 2, 220);
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBackground(SystemColor.controlHighlight);
		frmCsnaBy.getContentPane().add(separator_1);
		
		EXPR_FILTER = new JTextField();
		EXPR_FILTER.setText(".*[a-zA-Z0-9._%-]+[0-9].*");
		EXPR_FILTER.setFont(new Font("Tahoma", Font.PLAIN, 17));
		EXPR_FILTER.setBounds(807, 154, 454, 32);
		EXPR_FILTER.setEnabled(false);
		EXPR_FILTER.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(EXPR_FILTER);
		EXPR_FILTER.setColumns(10);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(7, 226, 1278, 2);
		separator.setBackground(SystemColor.control);
		frmCsnaBy.getContentPane().add(separator);
		
		JSpinner Data_FINE = new JSpinner();
		Data_FINE.setBounds(1112, 112, 149, 32);
		Data_FINE.setFont(new Font("SansSerif", Font.PLAIN, 18));
		Data_FINE.setModel(new SpinnerDateModel(new Date(1520463600000L), null, null, Calendar.WEEK_OF_YEAR));
		frmCsnaBy.getContentPane().add(Data_FINE);
		
		JSpinner Data_INIZIO = new JSpinner();
		Data_INIZIO.setBounds(938, 112, 149, 32);
		Data_INIZIO.setModel(new SpinnerDateModel(new Date(1520463600000L), null, null, Calendar.DAY_OF_MONTH));
		Data_INIZIO.setFont(new Font("SansSerif", Font.PLAIN, 18));
		frmCsnaBy.getContentPane().add(Data_INIZIO);
		
		JButton SaveIni = new JButton("Salva impostazioni");
		SaveIni.setBounds(198, 235, 181, 35);
		SaveIni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Wini ini;
				boolean cond=HOST.getText().equals("")||USER.getText().equals("");
				if(!cond) {
					try {
						ConsoleFTP.append("\n>> Salvataggio delle impostazioni...");
						ini=new Wini(new File("CONFIG.ini"));
						ini.put("FTP_LOGIN", "PathFTP",DIR.getText());
						ini.put("SPLIT_OPTION","EXPRESSION_FILTER",EXPR_FILTER.getText());
						ini.put("FTP_LOGIN", "HostFTP",HOST.getText());
						ini.put("FTP_LOGIN", "UsrFTP",USER.getText());
						if(!KEY.getText().equals("")) {
							int YN=JOptionPane.showConfirmDialog(frmCsnaBy, "Vuoi salvare la Password del Server ftp specificata? (Opzione sconsigliata)","Conferma salvataggio password",JOptionPane.YES_NO_OPTION);
							if(YN==JOptionPane.YES_OPTION) {
								ini.put("FTP_LOGIN", "KeyFTP",KEY.getText());
								ConsoleFTP.append("\n  Chiave ftp -> CONFIG.ini\n");
							}else {
								ini.put("FTP_LOGIN", "KeyFTP","");
								ConsoleFTP.append("\n >> Chiave non salvata ;)\n");
							}
						}else {
							ini.put("FTP_LOGIN", "KeyFTP",KEY.getText());
							ConsoleFTP.append("\n >> Chiave ftp resettata.\n");
						}
						String datemin = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Data_INIZIO.getValue());
						String datemax = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Data_FINE.getValue());
						ini.put("SPLIT_OPTION", "Date_min",datemin);
						ini.put("SPLIT_OPTION", "Date_max",datemax);
						//ini.put("FTP_LOGIN", "KeyFTP",KEY.getText());
						ini.put("FTP_LOGIN", "PortFTP",(Integer)PORTFTP.getValue());
						ini.put("SPLIT_OPTION","Port",(Integer)portFilter.getValue());
						
						ini.store();
						ConsoleFTP.append("\n>> Impostazioni salvate!");
						ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						csNaLOG.setSelectionStart(csNaLOG.getText().length());
					}catch(Exception e) {
						ConsoleFTP.append("\n>> Errore nel salvataggio delle impostazioni!");
						ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
					}
				}else {
					ConsoleFTP.append("\n>> Impossibile salvare: campi HOST e USER vuoti.");
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				
				
			}
		});
		SaveIni.setFont(new Font("SansSerif", Font.PLAIN, 13));
		SaveIni.setBackground(new Color(169, 169, 169));
		frmCsnaBy.getContentPane().add(SaveIni);
		
		JButton LoadIni = new JButton("Carica impostazioni");
		LoadIni.setBounds(7, 235, 181, 35);
		LoadIni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Ini inifile=new Ini();
				DateFormat parser = new SimpleDateFormat ("yyyy_MM_dd_HH_mm_ss");
				try {			
					Ini conFile = new Ini(new FileReader("CONFIG.ini"));
					inifile=conFile;
				}catch(Exception ex) {
					ConsoleFTP.append("\n>> Errore nel caricamento delle impostazioni :(\n");
				}
				DIR.setText(inifile.get("FTP_LOGIN", "PathFTP"));
				HOST.setText(inifile.get("FTP_LOGIN", "HostFTP"));
				USER.setText(inifile.get("FTP_LOGIN", "UsrFTP"));
				EXPR_FILTER.setText(inifile.get("SPLIT_OPTION","EXPRESSION_FILTER"));
				String k=inifile.get("FTP_LOGIN","KeyFTP");
				if(k.equals("")) {
					ConsoleFTP.append("\n >> Chiave ftp non rilevata nel file CONFIG.ini\n");
				}else {
					KEY.setText(k);
					ConsoleFTP.append("\n >> Password FTP rilevata: "+k+" \n\n >> E' sconsigliato salvare la password \n");	
				}
				try {
					Data_INIZIO.setValue(parser.parse(inifile.get("SPLIT_OPTION", "Date_min")));
					Data_FINE.setValue(parser.parse(inifile.get("SPLIT_OPTION", "Date_max")));
				}catch(Exception exc) {}
				
				//KEY.setText(inifile.get("FTP_LOGIN", "KeyFTP"));
				PORTFTP.setValue(Integer.valueOf(inifile.get("FTP_LOGIN", "PortFTP")));
				portFilter.setValue(Integer.valueOf(inifile.get("SPLIT_OPTION", "Port")));
				ConsoleFTP.append("\n>> Impostazioni caricate!");
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
								
			}
		});
		LoadIni.setFont(new Font("SansSerif", Font.PLAIN, 13));
		LoadIni.setBackground(new Color(169, 169, 169));
		frmCsnaBy.getContentPane().add(LoadIni);
				
		
		JLabel lblInizio = new JLabel("Intervallo");
		lblInizio.setBounds(829, 112, 104, 32);
		lblInizio.setHorizontalAlignment(SwingConstants.CENTER);
		lblInizio.setFont(new Font("Tahoma", Font.BOLD, 18));
		frmCsnaBy.getContentPane().add(lblInizio);
		
		JLabel lblFine = new JLabel("-");
		lblFine.setBounds(1088, 112, 22, 32);
		lblFine.setHorizontalAlignment(SwingConstants.CENTER);
		lblFine.setFont(new Font("SansSerif", Font.BOLD, 18));
		frmCsnaBy.getContentPane().add(lblFine);
		
		JButton DownloadDump = new JButton("Scarica File");
		DownloadDump.setBounds(198, 187, 181, 33);
		DownloadDump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DownloadDump.setEnabled(false);
				ConsoleFTP.setText(ConsoleFTP.getText()+"\n\n>> Connessione in corso...");
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				String host=HOST.getText();
				String user=USER.getText();
				String key=KEY.getText();
				String FTPdir=DIR.getText();
				int portftp=21;	
				try {
					portftp=((Integer)PORTFTP.getValue());
				}catch(Exception e) {
					PORTFTP.setValue(Integer.valueOf(21));
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> La porta deve essere un numero!");
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}final int p=portftp;
				new Thread(new Runnable() {
				    public void run() {
				        //apro un thread per non bloccare il messaggio iniziale
				Dumper dumper=new Dumper(host, user, key, p);
				boolean check=Dumper.CheckFTP(host, user, key, p);
				String[] file=dumper.GetRemoteFiles("/"+FTPdir);
				//ottengo i nomi di tutti i file contenuti della cartella
				StringTokenizer tokenizer;
				
				if(check) {
					ConsoleFTP.append("\n>> Connessione riuscita\n>> Porta ftp: "+((Integer)PORTFTP.getValue()).toString());
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> "+user+": Inizio download file...\n");
					csNaLOG.append("\n\n>> Lettura files nella cartella: "+FTPdir+"\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				else {
					ConsoleFTP.setText(ConsoleFTP.getText()+"\n>> Connessione fallita.\n");
					csNaLOG.append("\n>> Connessione fallita.\n");
					DownloadDump.setEnabled(true);
				}
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				//ora cerco i file voluti
				int cont=0;
				for (String s:file) {
					cont++;
					if(cont>=file.length)
					{
						DownloadDump.setEnabled(true);
					}
					tokenizer=new StringTokenizer(s, "-");
					String type=tokenizer.nextToken();
					//inizializzo variabili per il confronto della data del file con 
					//l'intervallo del filtro
					DateFormat df = new SimpleDateFormat ("yyyy_MM_dd.HH_mm");
					df.setLenient (false);
					Date inizio=(Date)Data_INIZIO.getValue();
					df.format(inizio);
					Date fine=(Date)Data_FINE.getValue();
					df.format(fine);
					// Ora posso iniziare a distinguere se file filtrato per porta o non filtrato
					if(type.equals("NOfilter")) {
						tokenizer.nextToken();
						String fileDate="";
						// ricavo la data del dump e cancello l'estensione .cap
						fileDate=tokenizer.nextToken();
						//csNaLOG.append(fileDate);
						fileDate=fileDate.substring(0,(fileDate.length()-4));
						//csNaLOG.append("->"+fileDate);
						Date d=new Date();
						try {
							d = df.parse (fileDate);
						}catch(Exception e) {
							csNaLOG.append("\nErrore nella lettura dell'intervallo selezionato! :( \n");
							ConsoleFTP.setText("\n>> Errore lettura intervallo");
							DownloadDump.setEnabled(true);
							
						}						
						//csNaLOG.append("\nIntervallo selezionato: "+inizio.toString()+"-"+fine.toString());
						boolean condizione=d.compareTo(inizio)>=0&&d.compareTo(fine)<=0;
						if(condizione) {
							csNaLOG.append("\n >> File trovato:"+s+"\n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							File local=new File("Files_FTP/"+s);
							// se file esiste non lo scrivo
							if (local.exists()) {
								csNaLOG.append("\nFile esistente in /Files_FTP, download non necessario!\n");
								ConsoleFTP.append(s+"\n>> file esistente in Files_FTP\n\n");
								csNaLOG.setSelectionStart(csNaLOG.getText().length());
								ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							//altrimenti, se tutto va a buon fine avviso del corretto salvataggio
							}else if(dumper.saveLocalFile(FTPdir, s, "Files_FTP/"+s)) {
								csNaLOG.append("\n    - Files_FTP/"+s+" -> Salvato in locale. [Ora puoi esportare merge NOfilter e usare la scansione approfondita nel filtro]\n");
								ConsoleFTP.append(s+"\n>> File salvato\n\n");
								csNaLOG.setSelectionStart(csNaLOG.getText().length());
								ConsoleFTP.setSelectionStart(csNaLOG.getText().length());
							}
						}
						
					//se file splittato							
					}else if(type.equals("Port")){
						tokenizer.nextToken(); //porta del file splittato
						tokenizer.nextToken();// IP
						String fileDate="";
						// ricavo la data del dump e cancello l'estensione .cap
						fileDate=tokenizer.nextToken();
						//csNaLOG.append(fileDate);
						fileDate=fileDate.substring(0,(fileDate.length()-4));
						//csNaLOG.append("->"+fileDate);
						Date d=new Date();
						try {
							d = df.parse (fileDate);
						}catch(Exception e) {
							csNaLOG.append("\nErrore nella lettura dell'intervallo selezionato! :( \n");
						}						
						//csNaLOG.append("\nIntervallo selezionato: "+inizio.toString()+"-"+fine.toString());
						boolean condizione=d.compareTo(inizio)>=0&&d.compareTo(fine)<=0;
						//int userFilter=0;	
						//try {
						//	userFilter=((Integer)portFilter.getValue());
						//}catch(Exception e) {}
						if(condizione) {
							csNaLOG.append("\n >> File trovato:"+s+"\n");
							File local=new File("Files_FTP/"+s);
							// se file esiste non lo scrivo
							if (local.exists()) {
								csNaLOG.append("\nFile esistente in /Files_FTP, download non necessario!\n");
								ConsoleFTP.append(s+"\n>> file esistente in Files_FTP\n\n");
								csNaLOG.setSelectionStart(csNaLOG.getText().length());
								ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							}else if(dumper.saveLocalFile(FTPdir, s, "Files_FTP/"+s)) {
								csNaLOG.append("\n    - Files_FTP/"+s+" -> Salvato in locale.\n");
								ConsoleFTP.append(s+"\n>> File salvato\n\n");
								csNaLOG.setSelectionStart(csNaLOG.getText().length());
								ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							}
						}
											
					}
				}
				JOptionPane.showMessageDialog(frmCsnaBy, "Download terminato! File scaricati in /Files_FTP");
				csNaLOG.append("\n\n>> FINE, DMAD pronto:");
				ConsoleFTP.append("\n\n>> TERMINATO!");
				csNaLOG.setSelectionStart(csNaLOG.getText().length());
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				DownloadDump.setEnabled(true);
				    }
				}).start();	
				
			}
		});
		
		DownloadDump.setBackground(new Color(240, 255, 255));
		DownloadDump.setFont(new Font("SansSerif", Font.BOLD, 17));
		frmCsnaBy.getContentPane().add(DownloadDump);
		
		JLabel ftponoff = new JLabel("Modalit\u00E0 ftps attiva");
		ftponoff.setBounds(1012, 281, 243, 32);
		ftponoff.setForeground(new Color(240, 255, 255));
		ftponoff.setFont(new Font("Tahoma", Font.BOLD, 18));
		ftponoff.setHorizontalAlignment(SwingConstants.CENTER);
		frmCsnaBy.getContentPane().add(ftponoff);
		
		JLabel msg = new JLabel("Cartella sorgente: \\Files_FTP");
		msg.setHorizontalAlignment(SwingConstants.CENTER);
		msg.setBounds(705, 274, 303, 47);
		msg.setFont(new Font("Tahoma", Font.BOLD, 18));
		frmCsnaBy.getContentPane().add(msg);			
		
		JCheckBox ftpON_OFF = new JCheckBox("(ON)");
		ftpON_OFF.setForeground(SystemColor.activeCaptionText);
		ftpON_OFF.setOpaque(false);
		ftpON_OFF.setBounds(558, 184, 77, 38);
		ftpON_OFF.setFont(new Font("Tahoma", Font.BOLD, 16));
		ftpON_OFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(ftpON_OFF.isSelected()) { //accendi
					ConsoleFTP.append("\n ### Modulo ftps attivato ###\n");
					ftpON_OFF.setText("(ON)");
					lblFtpsLog.setForeground(Color.black);
					USER.setEnabled(true);
					PORTFTP.setEnabled(true);
					HOST.setEnabled(true);
					KEY.setEnabled(true);
					lblFtpsLog.setText("MODULO FTPS");
					DIR.setEnabled(true);
					Check.setEnabled(true);
					DownloadDump.setEnabled(true);
					ftponoff.setForeground(new Color(240, 255, 255));
					ftponoff.setText("Modalit� ftps attiva");
					msg.setText("Cartella sorgente: \\Files_FTP");
										
				}else { //spegni
					ConsoleFTP.append("\n ### Modulo ftps disattivato ###\n");
					lblFtpsLog.setForeground(Color.gray);
					ftpON_OFF.setText("(OFF)");
					USER.setEnabled(false);
					lblFtpsLog.setText("MODULO FTPS");
					PORTFTP.setEnabled(false);
					HOST.setEnabled(false);
					KEY.setEnabled(false);
					DIR.setEnabled(false);
					Check.setEnabled(false);
					DownloadDump.setEnabled(false);
					ftponoff.setForeground(Color.blue);
					ftponoff.setText("Modalit� ftps disattivata");
					msg.setText("Cartella sorgente: \\Files_Utente");
					
				}
			}
		});
		ftpON_OFF.setSelected(true);
		ftpON_OFF.setBackground(SystemColor.activeCaption);
		frmCsnaBy.getContentPane().add(ftpON_OFF);
		
		
		JButton btnFiltracartellaFtp = new JButton("FILTRA E UNISCI");
		btnFiltracartellaFtp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String sourcefolder;
				Vector <PcapPacket> packetBlacklist = new Vector<PcapPacket>(); 
				
				//se modulo ftp attivo uso files_FTP
				if(ftpON_OFF.isSelected()) {
					sourcefolder="Files_FTP";
					csNaLOG.append("\n\n>> Lettura cartella Files_Utente: \n\n");
				}else {//altrimenti uso i file caricati dall'utente
					sourcefolder="Files_Utente";
					csNaLOG.append("\n\n>> Lettura cartella Files_FTP: \n\n");
				}
				File cartellaSrc=new File(sourcefolder);
				String []filesSorgenti=cartellaSrc.list();
				for(String s:filesSorgenti) {
					csNaLOG.append(" - "+s+"\n");
				}
				if(filesSorgenti.length==0) {
					csNaLOG.append(" >> Nessun file nella cartella!\n");
					csNaLOG.append("\n\n ################### EXPORT TERMINATO. ###################\n\n");
					ConsoleFTP.append("\n>> EXPORT TERMINATO\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				btnFiltracartellaFtp.setEnabled(false);
				new Thread(new Runnable() {
				    public void run() {
				        //apro un thread per non bloccare il messaggio iniziale
				    	int PORTAscan=0;
						
						try {
							PORTAscan=((Integer)portFilter.getValue());
						}catch(Exception e) {btnFiltracartellaFtp.setEnabled(true);}
				    	boolean nessunFile=true;
				    	String exportName2="Export/Export_Filtered_"+ExportUserPath.getText()+".cap";
		        String exportName="Export/Export_Pt_"+PORTAscan+"_"+ExportUserPath.getText()+".cap";
		        exportMSG.setText("Salva come: \\Export\\Export_Pt_"+PORTAscan);
		        boolean firstappend=false;//usato per evitare l'append del global header la prima volta
                CaptureCycle cicloSplit;
				for (String s:filesSorgenti) {
					
					StringTokenizer tokenizer=new StringTokenizer(s, "-");
					String type=tokenizer.nextToken();
					//inizializzo variabili per il confronto della data del file con 
					//l'intervallo del filtro
					DateFormat df = new SimpleDateFormat ("yyyy_MM_dd.HH_mm");
					df.setLenient (false);
					Date inizio=(Date)Data_INIZIO.getValue();
					df.format(inizio);
					Date fine=(Date)Data_FINE.getValue();
					df.format(fine);
					StringBuilder errbuf = new StringBuilder(); 
					
					// Ora posso  distinguere se file filtrato per porta o non filtrato
					if(type.equals("NOfilter")&&(DEEP_SCAN_FLAG.isSelected()||!PORT_YN.isSelected())) {
						String IP=tokenizer.nextToken();
						String fileDate="";
						// ricavo la data del dump e cancello l'estensione .cap
						fileDate=tokenizer.nextToken();
						//csNaLOG.append(fileDate);
						fileDate=fileDate.substring(0,(fileDate.length()-4));
						//csNaLOG.append("->"+fileDate);
						Date d=new Date();
						try {
							d = df.parse (fileDate);
						}catch(Exception e) {
							btnFiltracartellaFtp.setEnabled(true);
							csNaLOG.append("\n\nErrore nella lettura dell'intervallo selezionato! :( \n");
							ConsoleFTP.append("\n>> Errore lettura intervallo :( \n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						}						
						//csNaLOG.append("\nIntervallo selezionato: "+inizio.toString()+"-"+fine.toString());
						boolean condizione=d.compareTo(inizio)>=0&&d.compareTo(fine)<=0;
						if(condizione) {
							
							cicloSplit=new CaptureCycle(errbuf, sourcefolder, "Temp", IP, FLOW_CONTR_FLAG.isSelected(), EXPR_FLAG.isSelected(), EXPR_FILTER.getText());
								//csNaLOG.append("\n\n"+cicloSplit.getOUTPUT()+"\n\n");
							if(PORT_YN.isSelected()) {
								String splitResult=cicloSplit.Split(s, PORTAscan, packetBlacklist);
								CapMerger.MergeToOUT(splitResult, exportName, firstappend);
							}
							else {
								
								String splitResult=cicloSplit.Filter(s, packetBlacklist);
								CapMerger.MergeToOUT(splitResult, exportName2, firstappend);
								
							}
								
							nessunFile=false;
							csNaLOG.append("\n>> Il file: "+s+" soddisfa i requisiti del filtro :D\n\n>> aggiunto a "+exportName+" :)\n");
							ConsoleFTP.append("\n>> file salvato");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							if(firstappend==false) {
								firstappend=true;
							}
						}
						if(!condizione) {
							csNaLOG.append("\nIl file: "+s+" non soddisfa i requisiti del filtro :(\n");
						}
					}//end if(equals("NOfilter"))
					//l'else riguarda il caso file splittato per porta(senza deep scan attivo)
					//per evitare pacchetti ripetuti due volte - servirebbe un controllo pi�
					//intelligente sull'esistenza del corrispettivo dump filtrato/nonfiltrato
					else if(type.equals("Port")&&!DEEP_SCAN_FLAG.isSelected()&&PORT_YN.isSelected()){
						String port=tokenizer.nextToken(); //porta del file splittato
						csNaLOG.append("\n -> "+s+": rilevato DUMP splittato sulla porta: "+port);
						String IP=tokenizer.nextToken();// IP
						String fileDate="";
						// ricavo la data del dump e cancello l'estensione .cap
						fileDate=tokenizer.nextToken();
						//csNaLOG.append(fileDate);
						fileDate=fileDate.substring(0,(fileDate.length()-4));
						//csNaLOG.append("->"+fileDate);
						Date d=new Date();
						try {
							d = df.parse (fileDate);
						}catch(Exception e) {
							btnFiltracartellaFtp.setEnabled(true);
							csNaLOG.append("\nErrore nella lettura dell'intervallo selezionato! :( \n");
							ConsoleFTP.append("\n>> Errore lettura intervallo :(\n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						}						
						//csNaLOG.append("\nIntervallo selezionato: "+inizio.toString()+"-"+fine.toString());
						boolean condizione=d.compareTo(inizio)>=0&&d.compareTo(fine)<=0;
						int userFilter=0;	
						try {
							userFilter=((Integer)portFilter.getValue());
						}catch(Exception e) {btnFiltracartellaFtp.setEnabled(true);}
						if(condizione&&userFilter==Integer.parseInt(port)) {
							
							
							cicloSplit=new CaptureCycle(errbuf, sourcefolder, "Temp", IP, FLOW_CONTR_FLAG.isSelected(), EXPR_FLAG.isSelected(), EXPR_FILTER.getText());
							String splitResult=cicloSplit.Split(s, PORTAscan, packetBlacklist);
							CapMerger.MergeToOUT(splitResult, exportName, firstappend);
							nessunFile=false;
							csNaLOG.append("\n>> Il file soddisfa i requisiti del filtro! :) \n\n>> aggiunto a "+exportName+" :)\n");
							ConsoleFTP.append("\n>> file salvato");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							if(firstappend==false) {
								firstappend=true;
							}
														
						}
											
					}
				}
				//try {
				//	Files.write(Paths.get("./log.txt"), "Log:\n\n".getBytes());
				//}catch (Exception e) {
				//}
				csNaLOG.append("\n\n    ############# Merge dei file filtrati completato ##############\n\n      ############# Inizio analisi Export #############\n");
				ConsoleFTP.append("\n\n>> Fase di export terminata\n>> Inizio visualizzazione");
				csNaLOG.setSelectionStart(csNaLOG.getText().length());
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				//ora visualizzo il file creato
				if(nessunFile) { // se nessun file corrisponde al filtro
					csNaLOG.append("\n\n>> Nessun file corrispondente ai parametri del filtro :(");
					ConsoleFTP.append("\n>> Nessun file corrisponde al filtro");
					btnFiltracartellaFtp.setEnabled(true);
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				else { // se invece sono state trovate corrispondenze
					StringBuilder buff=new StringBuilder();
					if(!PORT_YN.isSelected()) {
						btnFiltracartellaFtp.setEnabled(true);
					 	JOptionPane.showMessageDialog(frmCsnaBy, (new File(exportName2)).getName()+": Completato - "+ (new File(exportName2)).length()+" Byte di dati");
						return;
					}
					final Pcap reader = Pcap.openOffline(exportName, buff);
					if (reader == null) {
						csNaLOG.append("\n>> Errore nella lettura dell'Export creato :(\n");
						ConsoleFTP.append("\n>> Errore lettura Export\n");
					}
					
					    	reader.loop(-1, new JPacketHandler<StringBuilder>() {
					            //inizializzo il tipo dei pacchetti di riferimento che user� per 
								//riconoscere se salvare il pacchetto in nextpacket()
								final Tcp tcp = new Tcp();
					           	final Udp udp = new Udp();
					           	final Ip4 ip = new Ip4();
								int i=0;
								public void nextPacket(JPacket packet, StringBuilder errbuf) 
								{
									i++;
									if(packet.hasHeader(tcp)&&packet.hasHeader(ip)) {
										packet.getHeader(tcp);
										packet.getHeader(ip);
										//try {
										//	Files.write(Paths.get("./log.txt"), tcp.toString().getBytes(),StandardOpenOption.APPEND);
										//}catch (Exception e) {
										//}
										ConsoleFTP.append("\n-> Pacchetto(TCP) numero: #"+ i);
										csNaLOG.append("\n\n-> (TCP)Pacchetto numero: #"+ i);
										csNaLOG.append(" - tcp.dst_port="+ tcp.destination());
										csNaLOG.append(" - tcp.src_port="+ tcp.source());
										csNaLOG.append(" - ip.dst="+FormatUtils.ip(ip.destination()));
										csNaLOG.append(" - ip.src="+FormatUtils.ip(ip.source()));
										csNaLOG.append(" - tcp.ACK="+ tcp.ack());
										csNaLOG.append(" - tcp.SYN="+ tcp.flags_SYN());
										csNaLOG.append(" - tcp.FIN="+ tcp.flags_FIN());
										csNaLOG.append(" - TCP Payload (N of bytes)="+ tcp.getPayloadLength());
										
										
										//String payload=new String(tcp.getPayload());
										//csNaLOG.append("\n - tcp.payload="+ payload);
										
									}else if(packet.hasHeader(udp)) {
										packet.getHeader(udp);
										packet.getHeader(ip);
										ConsoleFTP.append("\n-> Pacchetto(UDP) numero: #"+ i);
										csNaLOG.append("\n\n-> (UDP)Pacchetto numero: #"+ i);
										csNaLOG.append(" - udp.dst_port="+ udp.destination());
										csNaLOG.append(" - udp.src_port="+ udp.source());
										csNaLOG.append(" - ip.dst="+FormatUtils.ip(ip.destination()));
										csNaLOG.append(" - ip.src="+FormatUtils.ip(ip.source()));
										csNaLOG.append(" - UDP Payload (N of bytes)="+ udp.getPayloadLength());
										//String payload=new String(udp.getPayload());
										//csNaLOG.append("\n - udp.payload="+ payload);	
									}
									csNaLOG.setSelectionStart(csNaLOG.getText().length());
									ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
								}//end nextpacket()
							}, buff);//end loop scansione file
							reader.close(); //chiusura pcap 
							if(firstappend) {
				              	File export=new File(exportName);
				               	if (export.length()<1024) {//byte
				               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()+" Byte di dati");
							   	}else if(export.length()>=1024 && export.length()<(1024*1024)) {//kbyte
				               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()/1024+" kByte di dati");             
				               	}else {//MByte
				               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()/(1024*1024)+" MByte di dati");
					        	}
				            }
							csNaLOG.append("\n\n>> ################### COMPLETATO. ###################\n\n");
							ConsoleFTP.append("\n\n>> FILTRA! terminato correttamente\n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
							btnFiltracartellaFtp.setEnabled(true);
					  				
				}
				    }
				}).start();
				
				
			}
		});
		btnFiltracartellaFtp.setBounds(422, 272, 268, 49);
		btnFiltracartellaFtp.setFont(new Font("SansSerif", Font.BOLD, 20));
		btnFiltracartellaFtp.setBackground(new Color(240, 248, 255));
		frmCsnaBy.getContentPane().add(btnFiltracartellaFtp);
		
		
		
		
		JLabel lblImpostazioniAvanzateDel = new JLabel("Opzioni filtro");
		lblImpostazioniAvanzateDel.setForeground(new Color(47, 79, 79));
		lblImpostazioniAvanzateDel.setBounds(675, 74, 572, 28);
		lblImpostazioniAvanzateDel.setHorizontalAlignment(SwingConstants.CENTER);
		lblImpostazioniAvanzateDel.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblImpostazioniAvanzateDel.setAlignmentX(0.5f);
		frmCsnaBy.getContentPane().add(lblImpostazioniAvanzateDel);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		horizontalBox_2.setBounds(665, 73, 596, 32);
		horizontalBox_2.setBorder(new MatteBorder(3, 3, 3, 3, (Color) new Color(0, 0, 0)));
		horizontalBox_2.setBackground(SystemColor.inactiveCaption);
		frmCsnaBy.getContentPane().add(horizontalBox_2);
		
		JButton MergeExports = new JButton("Esporta merge non filtrato");
		MergeExports.setBounds(422, 234, 268, 33);
		MergeExports.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String sourcefolder;
				//se modulo ftp attivo uso files_FTP
				if(ftpON_OFF.isSelected()) {
					sourcefolder="Files_FTP";
					csNaLOG.append("\n\n>> Lettura cartella Files_Utente: \n");
				}else {//altrimenti uso i file caricati dall'utente
					sourcefolder="Files_Utente";
					csNaLOG.append("\n\n>> Lettura cartella Files_FTP: \n");
				}
				File cartellaSrc=new File(sourcefolder);
				String []filesSorgenti=cartellaSrc.list();
				for(String s:filesSorgenti) {
					csNaLOG.append("\n - "+s);
				}
				if(filesSorgenti.length==0) {
					csNaLOG.append(" >> Nessun file nella cartella!\n");
					csNaLOG.append("\n\n>> ################### EXPORT TERMINATO. ###################\n\n");
					ConsoleFTP.append("\n\n>> EXPORT TERMINATO\n");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
				
				//String date = new SimpleDateFormat("dd_MM_yyyy_HH.mm.ss").format( new Date());
				String exportName="Export/Export_NOfilter_"+ExportUserPath.getText()+".cap";
				exportMSG.setText("Salva come: \\Export\\Export_NOfilter_");
				boolean firstappend=false;//usato per evitare l'append del global header la prima volta
                for (String s:filesSorgenti) {
					
					StringTokenizer tokenizer=new StringTokenizer(s, "-");
					String type=tokenizer.nextToken();
					//inizializzo variabili per il confronto della data del file con 
					//l'intervallo del filtro
					DateFormat df = new SimpleDateFormat ("yyyy_MM_dd.HH_mm");
					df.setLenient (false);
					Date inizio=(Date)Data_INIZIO.getValue();
					df.format(inizio);
					Date fine=(Date)Data_FINE.getValue();
					df.format(fine);
					// Ora posso  distinguere se file filtrato per porta o non filtrato
					if(type.equals("NOfilter")) {
						tokenizer.nextToken();
						String fileDate="";
						// ricavo la data del dump e cancello l'estensione .cap
						fileDate=tokenizer.nextToken();
						//csNaLOG.append(fileDate);
						fileDate=fileDate.substring(0,(fileDate.length()-4));
						//csNaLOG.append("->"+fileDate);
						Date d=new Date();
						try {
							d = df.parse (fileDate);
						}catch(Exception e) {
							csNaLOG.append("\nErrore nella lettura dell'intervallo selezionato! :( \n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
							ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						}						
						//csNaLOG.append("\nIntervallo selezionato: "+inizio.toString()+"-"+fine.toString());
						boolean condizione=d.compareTo(inizio)>=0&&d.compareTo(fine)<=0;
						if(condizione) {
							csNaLOG.append("\nIl file: "+s+" soddisfa i requisiti del filtro :D\n\n>> aggiunto a "+exportName+" :)\n");
							CapMerger.MergeToOUT((sourcefolder+"/"+s), exportName, firstappend);
							if(firstappend==false) {
								firstappend=true;
							}
						}
						if(!condizione) {
							csNaLOG.append("\nIl file: "+s+" non soddisfa i requisiti del filtro :(\n");
							csNaLOG.setSelectionStart(csNaLOG.getText().length());
						}
					}}
                if(firstappend) {
	              	File export=new File(exportName);
	               	if (export.length()<1024) {//byte
	               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()+" Byte di dati");
				   	}else if(export.length()>=1024 && export.length()<(1024*1024)) {//kbyte
	               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()/1024+" kByte di dati");             
	               	}else {//MByte
	               	  	JOptionPane.showMessageDialog(frmCsnaBy, "Completato, il file "+export.getName()+" ha "+export.length()/(1024*1024)+" MByte di dati");
		        	}
	            }
                csNaLOG.append("\n\n>> ################### EXPORT TERMINATO. ###################\n\n");
				ConsoleFTP.append("\n\n>> EXPORT TERMINATO\n");
				csNaLOG.setSelectionStart(csNaLOG.getText().length());
				ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
			}
		});
		MergeExports.setBackground(new Color(240, 248, 255));
		MergeExports.setFont(new Font("SansSerif", Font.BOLD, 17));
		frmCsnaBy.getContentPane().add(MergeExports);
		
		JLabel lblcap = new JLabel(".cap");
		lblcap.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblcap.setBounds(1233, 236, 39, 34);
		frmCsnaBy.getContentPane().add(lblcap);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		horizontalBox_3.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
		horizontalBox_3.setForeground(Color.DARK_GRAY);
		horizontalBox_3.setBounds(151, 281, 256, 40);
		frmCsnaBy.getContentPane().add(horizontalBox_3);
		
		JButton btnEliminaDumpLocali = new JButton("Elimina File");
		btnEliminaDumpLocali.setFont(new Font("Tahoma", Font.BOLD, 17));
		btnEliminaDumpLocali.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				File sourcefolder1=new File("Files_FTP");
				File sourcefolder2=new File("Files_Utente");
				File sourcefolder3=new File("Export");
				
				if(FTP_DELETE_FLAG.isSelected()||EXPORT_DELETE_FLAG.isSelected()||USER_DELETE_FLAG.isSelected()) {
		    		
					int selectedOption = JOptionPane.showConfirmDialog(frmCsnaBy, 
                              "Vuoi davvero eliminare i file nelle cartelle selezionate? L'operazione non � reversibile.", 
                              "Elimina file nelle cartelle locali", 
                              JOptionPane.YES_NO_OPTION); 
					
					if (selectedOption == JOptionPane.YES_OPTION) {
					    csNaLOG.append("\n >> Avvio eliminazione files in corso...\n");
					    ConsoleFTP.append("\n >> CONFERMATO - AVVIO SCANSIONE FILE");
					    new Thread(new Runnable() {
	    					
							public void run() {
													
						    	try {
						    		btnEliminaDumpLocali.setEnabled(false);
						    		if(FTP_DELETE_FLAG.isSelected()) {
						    			File []deletelist1=sourcefolder1.listFiles();
						    			if(deletelist1.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder1.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder1.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											csNaLOG.append("\n ###### Cartella "+sourcefolder1.getName()+" ######\n");
											for(File f:deletelist1) {
												if(!f.delete()) {
													ConsoleFTP.append("\n >> Eliminazione di:\n"+f.getName()+"\n >> Non riuscita." );
													csNaLOG.append("\n >> Eliminazione di: "+f.getName()+" non riuscita." );
												}    
												else {
													ConsoleFTP.append("\n -> "+f.getName()+"\n >> ELIMINATO" );
													csNaLOG.append("\n -> "+f.getName()+" >> ELIMINATO." );
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
											}
										}
						    		}
						    		if(USER_DELETE_FLAG.isSelected()) {
						    			File []deletelist2=sourcefolder2.listFiles();
						    			if(deletelist2.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder2.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder2.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											csNaLOG.append("\n ###### Cartella "+sourcefolder2.getName()+" ######\n");
											for(File f:deletelist2) {
												if(!f.delete()) {
													ConsoleFTP.append("\n >> Eliminazione di:\n"+f.getName()+"\n >> Non riuscita." );
													csNaLOG.append("\n >> Eliminazione di: "+f.getName()+" non riuscita." );
												}    
												else {
													ConsoleFTP.append("\n -> "+f.getName()+"\n >> ELIMINATO" );
													csNaLOG.append("\n -> "+f.getName()+" >> ELIMINATO." );
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
											}
										}
						    		}
						    		if(EXPORT_DELETE_FLAG.isSelected()) {
						    			File []deletelist3=sourcefolder3.listFiles();
						    			if(deletelist3.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder3.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder3.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											csNaLOG.append("\n ###### Cartella "+sourcefolder3.getName()+" ######\n");
											for(File f:deletelist3) {
												if(!f.delete()) {
													ConsoleFTP.append("\n >> Eliminazione di:\n"+f.getName()+"\n >> Non riuscita." );
													csNaLOG.append("\n >> Eliminazione di: "+f.getName()+" non riuscita." );
												}    
												else {
													ConsoleFTP.append("\n -> "+f.getName()+"\n >> ELIMINATO" );
													csNaLOG.append("\n -> "+f.getName()+" >> ELIMINATO." );
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
											}
										}
						    		}
						    		btnEliminaDumpLocali.setEnabled(true);
						    		csNaLOG.append("\n >> Eliminazione files terminata.\n");
									ConsoleFTP.append("\n >> Elimina terminato\n");
									csNaLOG.setSelectionStart(csNaLOG.getText().length());
									ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						    		
						    	}catch(Exception e) {
						    		btnEliminaDumpLocali.setEnabled(true);
						    		csNaLOG.append("\n >> ERRORE NELL'ELIMINAZIONE DEI FILE!\n");
									ConsoleFTP.append("\n >>  ERRORE NELL'ELIMINAZIONE!\n");
									csNaLOG.setSelectionStart(csNaLOG.getText().length());
									ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						    	}//fine catch
						    }}).start();//fine thread
					    
					
					}else { // se l'utente annulla l'operazione
						csNaLOG.append("\n >> OPERAZIONE ANNULLATA DALL'UTENTE!\n");
						ConsoleFTP.append("\n >> ANNULLATA DALL'UTENTE\n\n >>");
						csNaLOG.setSelectionStart(csNaLOG.getText().length());
						ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
			    	}
	    		}//FINE if almeno un check selezionato
				else {
					csNaLOG.append("\n >> Nessuna cartella selezionata\n");
					ConsoleFTP.append("\n >> Nessuna cartella selezionata\n\n >>");
					csNaLOG.setSelectionStart(csNaLOG.getText().length());
					ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
				}
			}
		});
		btnEliminaDumpLocali.setBackground(new Color(255, 228, 181));
		btnEliminaDumpLocali.setBounds(991, 5, 270, 30);
		frmCsnaBy.getContentPane().add(btnEliminaDumpLocali);
		
		JButton btnVediFile = new JButton("Visualizza File");
		btnVediFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				File sourcefolder1=new File("Files_FTP");
				File sourcefolder2=new File("Files_Utente");
				File sourcefolder3=new File("Export");
				
				if(FTP_DELETE_FLAG.isSelected()||EXPORT_DELETE_FLAG.isSelected()||USER_DELETE_FLAG.isSelected()) {
		    		 new Thread(new Runnable() {
	    				public void run() {
					
	    					try {
						    		if(FTP_DELETE_FLAG.isSelected()) {
						    			File []list1=sourcefolder1.listFiles();
						    			ConsoleFTP.append("\n >> Directory: "+sourcefolder1.getName());
						    			csNaLOG.append("\n ################### CARTELLA: "+sourcefolder1.getName()+" #########################\n\n");
						    			if(list1.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder1.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder1.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											ConsoleFTP.append("\n >> Lettura directory: "+sourcefolder1.getName()+"\n");
											for(File f:list1) {
												if(f.isDirectory()) {													
													csNaLOG.append("\n >> Directory: "+f.getName());
												}    
												else if(f.isFile()) {
													String fname=f.getName();
													String extension=fname.substring(fname.length()-4,fname.length());
													if(!extension.equals(".cap")) {
														csNaLOG.append("\n -> File generico: "+f.getName());
													}else {
														csNaLOG.append("\n### File .cap, contiene pacchetti di rete: "+f.getName()+" ("+f.length()+" Byte)");
													}
													
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
											}
										}
						    		}
						    		if(USER_DELETE_FLAG.isSelected()) {
						    			ConsoleFTP.append("\n >> Directory: "+sourcefolder2.getName()+"\n");
						    			csNaLOG.append("\n\n ################### CARTELLA: "+sourcefolder2.getName()+" #########################\n\n");
						    			File []list2=sourcefolder2.listFiles();
						    			if(list2.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder2.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder2.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											for(File f:list2) {
												if(f.isDirectory()) {													
													csNaLOG.append("\n >> Directory:"+f.getName());
												}    
												else if(f.isFile()) {
													String fname=f.getName();
													String extension=fname.substring(fname.length()-4,fname.length());
													if(!extension.equals(".cap")) {
														csNaLOG.append("\n -> File generico: "+f.getName());
													}else {
														csNaLOG.append("\n### File .cap, contiene pacchetti di rete: "+f.getName()+" ("+f.length()+" Byte)");
															}
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
												
											}
										}
						    		}
						    		if(EXPORT_DELETE_FLAG.isSelected()) {
						    			ConsoleFTP.append("\n >> Directory: "+sourcefolder3.getName()+"!\n");
						    			csNaLOG.append("\n\n ################### CARTELLA: "+sourcefolder3.getName()+" #########################\n\n");
						    			File []list3=sourcefolder3.listFiles();
						    			if(list3.length==0) {
											csNaLOG.append("\n >> Nessun file nella cartella "+sourcefolder3.getName()+"!\n");
											ConsoleFTP.append("\n >> Nessun file in "+sourcefolder3.getName()+"!\n");
											csNaLOG.setSelectionStart(csNaLOG.getText().length());
											ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
										}else {
											for(File f:list3) {
												if(f.isDirectory()) {													
													csNaLOG.append("\n >> Directory:"+f.getName());
												}    
												else if(f.isFile()) {
													String fname=f.getName();
													String extension=fname.substring(fname.length()-4,fname.length());
													if(!extension.equals(".cap")) {
														csNaLOG.append("\n -> File generico: "+f.getName());
													}else {
														csNaLOG.append("\n### File .cap, contiene pacchetti di rete: "+f.getName()+" ("+f.length()+" Byte)");
													}
												}
												ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
												csNaLOG.setSelectionStart(csNaLOG.getText().length());
											}
										}
						    		}
						    		
						    		csNaLOG.append("\n >> TERMINATO.\n");
									ConsoleFTP.append("\n >> TERMINATO!\n");
									csNaLOG.setSelectionStart(csNaLOG.getText().length());
									ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						    		
						    	}catch(Exception e) {
						    		csNaLOG.append("\n >> ERRORE NELL'APERTURA DEI FILE!\n");
									ConsoleFTP.append("\n >>  ERRORE NELLA LETTURA!\n");
									csNaLOG.setSelectionStart(csNaLOG.getText().length());
									ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
						    	}//fine catch
						    }}).start();//fine thread
					    
					
					}else {
						csNaLOG.append("\n\n >> TERMINATO (Nessuna directory selezionata)\n");
						ConsoleFTP.append("\n >> NESSUNA CARTELLA SELEZIONATA!\n");
						csNaLOG.setSelectionStart(csNaLOG.getText().length());
						ConsoleFTP.setSelectionStart(ConsoleFTP.getText().length());
					}
	    		}//FINE if almeno un check selezionato
					
		});
		btnVediFile.setFont(new Font("Tahoma", Font.BOLD, 17));
		btnVediFile.setBackground(new Color(240, 255, 240));
		btnVediFile.setBounds(991, 39, 270, 30);
		frmCsnaBy.getContentPane().add(btnVediFile);
			
		
	}
}
