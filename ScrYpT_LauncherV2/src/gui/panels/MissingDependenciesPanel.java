package gui.panels;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import launcherutils.om.DependencyModel;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;

public class MissingDependenciesPanel extends JDialog{
	
	// KeyGenPanel logger
	static Logger logger = Logger.getLogger(MissingDependenciesPanel.class); 
	
	private StartPanel parentStarterFrame;
	private MissingDependenciesPanel thisPanel;
	private ArrayList<DependencyModel> dependeciesMissing;
	private JTable table;
	private JLabel lblTitleHeaderMessage;
	private static final String[] columnNames = {"Dependency name", "Critical dependency (T/F)", "Dependency actions"};
	private JButton btnIgnoreWarnings;
	private JButton btnRefreshNCheck;
	
	// constructor
	public MissingDependenciesPanel(String title, StartPanel parentStarter) {
		
		parentStarterFrame = parentStarter;
		thisPanel = this;
		thisPanel.setTitle(title);
		
		dependeciesMissing = parentStarter.LAUNCHER.dependeciesMissing;
		
		getContentPane().setBackground(Color.DARK_GRAY);
		getContentPane().setLayout(new MigLayout("", "[50%,grow][50%,grow]", "[50px:80px][grow][40px:n,grow]"));
		
		lblTitleHeaderMessage = new JLabel("Some dependencies are missing, if they're critical you have to solve them before to start ScrYpt!");
		lblTitleHeaderMessage.setBackground(new Color(211, 211, 211));
		lblTitleHeaderMessage.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		lblTitleHeaderMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitleHeaderMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTitleHeaderMessage.setOpaque(true);
		lblTitleHeaderMessage.setFont(new Font("Segoe UI", Font.BOLD, 14));
		getContentPane().add(lblTitleHeaderMessage, "cell 0 0 2 1,grow");
		
		table = new JTable();
		JScrollPane tableContainer = new JScrollPane(table);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setCellSelectionEnabled(false);
		table.setFillsViewportHeight(true);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		table.setRowHeight(30);
		getContentPane().add(tableContainer, "cell 0 1 2 1,grow");
		
		btnIgnoreWarnings = new JButton("Ignore warnings (some functions will be disabled)");
		btnIgnoreWarnings.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnIgnoreWarnings.setBackground(new Color(176, 196, 222));
		getContentPane().add(btnIgnoreWarnings, "cell 0 2,growx");
		
		btnRefreshNCheck = new JButton("Refresh and check again for dependencies");
		btnRefreshNCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btnRefreshNCheck.setBackground(new Color(176, 196, 222));
		getContentPane().add(btnRefreshNCheck, "cell 1 2,growx");
		
		if(!parentStarterFrame.LAUNCHER.criticalDependenciesOK) {
			// if critical problem then disable window closing possibility
			disableContinueAvailable();
		}
		
		updateDependencyIssueTable();
		
		addHandlers();
		
		thisPanel.setBounds(120, 120, 700, 250 );
		
		logger.info(this.getClass().getName() + " Started");
	}
	
	private void addHandlers() {
		
		// default close operation(same as reset) 
		thisPanel.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		            
	        	logger.debug("Closing Frame: " + thisPanel.getClass().getName());	        	

		    }
		});
		
	}
	
	private void disableContinueAvailable() {
		thisPanel.setDefaultCloseOperation(EXIT_ON_CLOSE);
		thisPanel.setAlwaysOnTop(true);
		parentStarterFrame.setEnabled(false);
	}
	
	private void enableContinueAvailable() {
		thisPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		thisPanel.setAlwaysOnTop(false);
		parentStarterFrame.setEnabled(true);
	}
	
	private DefaultTableModel getTableModel() {
		// TODO define custom table class
		DefaultTableModel model = new DefaultTableModel(null, columnNames) {
			@Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return false;
		    }
		};
		
		table.setGridColor(Color.DARK_GRAY);
		table.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		table.setBackground(Color.WHITE);
		table.getTableHeader().setFont(new Font("Segoe UI Black", Font.BOLD, 14));
		table.getTableHeader().setVisible(true);
		return model;
	}
	
	private void updateDependencyIssueTable() {
		// first empty the table
//		table.removeAll();
		table.setModel(getTableModel());
		
		for(DependencyModel currentDependency : dependeciesMissing) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.addRow(new Object[]{currentDependency.dependencyName, currentDependency.isCritical, ""});
		}
	}
}
