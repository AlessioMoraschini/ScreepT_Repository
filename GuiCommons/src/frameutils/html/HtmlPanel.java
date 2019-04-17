package frameutils.html;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;

public class HtmlPanel extends JPanel{
	
	// TextFileWriter logger creation
	static Logger logger = Logger.getLogger(HtmlPanel.class);
		
	private JPanel headerPanel;
	private JScrollPane mainScrollPane;
	private JEditorPane mainHtmlEditorPane;
	
	public HtmlPanel(int headerHeight) {
		
		logger.info(this.getClass().getName() + " Starting...");
		
		setBorder(new LineBorder(Color.GRAY, 2, true));
		setBackground(Color.BLACK);
		setLayout(new MigLayout("", "[100px][100px,grow][100px]", "[:"+headerHeight+"px:"+headerHeight+"px][100px,grow]"));
		
		headerPanel = new JPanel();
		headerPanel.setBorder(new LineBorder(Color.GRAY, 2));
		headerPanel.setBackground(Color.DARK_GRAY);
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		if (headerHeight > 0) {
			add(headerPanel, "cell 0 0 3 1,grow");
			add(mainScrollPane, "cell 0 1 3 1,grow");
		}else {
			add(mainScrollPane, "cell 0 0 3 2,grow");
		}
		
		mainHtmlEditorPane = new JEditorPane();
		mainHtmlEditorPane.setEditable(false);
		mainScrollPane.setViewportView(mainHtmlEditorPane);
		
		logger.info(this.getClass().getName() + " Initialized!");
	}

	public void loadHtml(String pathToHtml) {
		HtmlLoader.loadHtmlInEditPane(mainHtmlEditorPane, pathToHtml);
	}
	
	public JEditorPane getMainHtmlEditorPane() {
		return mainHtmlEditorPane;
	}

	public void setMainHtmlEditorPane(JEditorPane mainHtmlEditorPane) {
		this.mainHtmlEditorPane = mainHtmlEditorPane;
	}

	public JPanel getHeaderPanel() {
		return headerPanel;
	}

	public void setHeaderPanel(JPanel headerPanel) {
		this.headerPanel = headerPanel;
	}

	public JScrollPane getMainScrollPane() {
		return mainScrollPane;
	}

	public void setMainScrollPane(JScrollPane mainScrollPane) {
		this.mainScrollPane = mainScrollPane;
	}
	
}
