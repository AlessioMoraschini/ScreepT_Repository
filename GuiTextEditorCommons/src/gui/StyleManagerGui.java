package gui;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rtextarea.RTextScrollPane;

import files.CustomFileFilters;
import gui.selection.ColorSelectionElement;
import gui.selection.ColorSelector;
import utils.RsyntaxUtils;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import om.SupportedFileTextEditor;
import utility.SafeLogger;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;

public class StyleManagerGui extends JFrame {
	private static final long serialVersionUID = 510759366291391824L;
	
	SafeLogger logger = new SafeLogger(StyleManagerGui.class);
	
	public Dimension defaultDimension = new Dimension(1400, 750);
	
	public static Font defaultBtnFont = new Font("Segoe UI", Font.PLAIN, 15);
	
	public static StyleManagerGui singletonInstance;
	
	private File styleFilesFolder;
	public List<ColorStyleScheme> styles;
	
	public RTextScrollPane scrollPane;
	public RSyntaxTextArea testTextArea;
	
	private JLabel lblSyntax;
	private JLabel lblStyle;
	
	private JComboBox<SupportedFileTextEditor> comboSyntax;
	private JComboBox<ColorStyleScheme> comboStyle;
	
	private JScrollPane scrollPaneElements;
	private JPanel panelElements;
	private ColorSelector[] tokenSelectors;
	
	public StyleManagerGui(File sourceFolder) {
		singletonInstance = this;
		tokenSelectors = new ColorSelector[TokenTypes.DEFAULT_NUM_TOKEN_TYPES];
		
		getContentPane().setBackground(Color.LIGHT_GRAY);
		getContentPane().setLayout(new MigLayout("", "[60px:n:70px][550px:n:550px][grow][59.00][grow]", "[35px:n:35px][5px:n][:85%:85%,grow][-112.00px:n][10%]"));
		
		lblStyle = new JLabel("Style");
		lblStyle.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblStyle, "cell 0 0,alignx center");
		
		comboStyle = new JComboBox<ColorStyleScheme>();
		comboStyle.setCursor(GuiUtils.CURSOR_HAND);
		comboStyle.setFont(defaultBtnFont);
		getContentPane().add(comboStyle, "cell 1 0 2 1,grow");
		
		lblSyntax = new JLabel("Syntax");
		lblSyntax.setFont(new Font("Segoe UI", Font.BOLD, 15));
		getContentPane().add(lblSyntax, "cell 3 0,alignx center");
		
		comboSyntax = new JComboBox<SupportedFileTextEditor>();
		comboSyntax.setCursor(GuiUtils.CURSOR_HAND);
		comboSyntax.setFont(defaultBtnFont);
		getContentPane().add(comboSyntax, "cell 4 0,grow");
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.DARK_GRAY);
		getContentPane().add(separator, "cell 0 1 5 1,growx");
		
		scrollPaneElements = new JScrollPane();
		scrollPaneElements.getVerticalScrollBar().setUnitIncrement(25);
		scrollPaneElements.getHorizontalScrollBar().setUnitIncrement(25);
		scrollPaneElements.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		scrollPaneElements.setBackground(Color.GRAY);
		getContentPane().add(scrollPaneElements, "cell 0 2 2 1,grow");
		
		panelElements = new JPanel(new GridLayout(TokenTypes.DEFAULT_NUM_TOKEN_TYPES, 1));
		panelElements.setBackground(Color.GRAY);
		scrollPaneElements.setViewportView(panelElements);
		
		testTextArea = new RSyntaxTextArea();
		scrollPane = new RTextScrollPane();
		scrollPane.setViewportView(testTextArea);
		getContentPane().add(scrollPane, "cell 2 2 3 1,grow");
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBackground(Color.DARK_GRAY);
		getContentPane().add(separator_1, "cell 0 3 5 1,growx");
		styleFilesFolder = sourceFolder;

		setTitle("Style Scheme Manager");
		
		setMinimumSize(defaultDimension);
		setPreferredSize(defaultDimension);
		
		addHandlers();
		
		SwingUtilities.invokeLater(()->{
			initComboStyles();
			initComboSyntax();
			
			try {
				initGuiFromStyle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		GuiUtils.centerComponent(this);
		setVisible(true);
	}
	
	private void addHandlers() {
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	singletonInstance = null;
	        	dispose();
		    }
		});
		
		comboStyle.addActionListener((e)->{
			try {
				testTextArea.setCursor(GuiUtils.CURSOR_WAIT);
				styles.get(comboStyle.getSelectedIndex()).loadThemeFromFile().apply(testTextArea);
				initGuiFromStyle();
			} catch (Exception e1) {
				logger.error("Cannot load style into text area!", e1);
				
			} finally {
				testTextArea.setCursor(GuiUtils.CURSOR_DEFAULT);
			}
		});
		
		comboSyntax.addActionListener((e)->{
			try {
				testTextArea.setCursor(GuiUtils.CURSOR_WAIT);
				SupportedFileTextEditor selected = (SupportedFileTextEditor)comboSyntax.getSelectedItem();
				testTextArea.setSyntaxEditingStyle(selected.syntax);
				initGuiFromStyle();
			} catch (Exception e1) {
				logger.error("Cannot load syntax into text area!", e1);
			
			} finally {
				testTextArea.setCursor(GuiUtils.CURSOR_DEFAULT);
			}
		});
	}
	
	public void initComboStyles() {
		styles = retrieveXmlStyles();
		comboStyle.setModel(new DefaultComboBoxModel(styles.toArray()));
		comboStyle.setSelectedIndex(0);
	}
	
	public void initComboSyntax() {
		comboSyntax.setModel(new DefaultComboBoxModel(SupportedFileTextEditor.values()));
		comboSyntax.setSelectedIndex(0);
	}
	
	private void initGuiFromStyle() throws IllegalArgumentException, IllegalAccessException {
		panelElements.removeAll();
		SyntaxScheme scheme = testTextArea.getSyntaxScheme();
		Field[] fields = TokenTypes.class.getFields();
		
		int i = 0;
		for (Field field : fields) {
			if (i < TokenTypes.DEFAULT_NUM_TOKEN_TYPES) {
				Style style = scheme.getStyle(i);
				tokenSelectors[i] = new ColorSelector(new ColorSelectionElement(style.foreground, style.background, field.getName()), () -> {
					try {
						final int index = getTokenIndexByName(field.getName());
						Style areaStyle = testTextArea.getSyntaxScheme().getStyle(index);
						areaStyle.background = tokenSelectors[index].elementOm.background;
						areaStyle.foreground = tokenSelectors[index].elementOm.foreground;

					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				panelElements.add(tokenSelectors[i]);
			}
			i++;
		}
		
		SwingUtilities.invokeLater(()->{
			scrollPane.revalidate();
			scrollPane.repaint();
			testTextArea.repaint();
		});
	}
	
	public List<ColorStyleScheme> retrieveXmlStyles() {
		
		ArrayList<ColorStyleScheme> schemes = new ArrayList<>();
		
		if(styleFilesFolder != null && styleFilesFolder.exists() && styleFilesFolder.isDirectory()) {
			
			File[] xmlSchemes = styleFilesFolder.listFiles(
					CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY, "xml", false));
			
			for(File scheme : xmlSchemes) {
				schemes.add(new ColorStyleScheme(scheme));
			}
		}
		
		return schemes;
	}
	
	public Dimension getDefaultDimension() {
		return defaultDimension;
	}

	public void setDefaultDimension(Dimension defaultDimension) {
		this.defaultDimension = defaultDimension;
	}

	public File getStyleFilesFolder() {
		return styleFilesFolder;
	}

	public void setStyleFilesFolder(File styleFilesFolder) {
		this.styleFilesFolder = styleFilesFolder;
	}

	public List<ColorStyleScheme> getStyles() {
		return styles;
	}

	public void setStyles(List<ColorStyleScheme> styles) {
		this.styles = styles;
	}


	public class ColorStyleScheme {
		public List<ColorSelectionElement> styleElements;
		public File styleSource;
		
		public ColorStyleScheme(File source) {
			styleSource = source;
		}
		
		public Theme loadThemeFromFile() {
			return RsyntaxUtils.readAndLoadThemeFromXmlFile(null, styleSource);
		}

		@Override
		public String toString() {
			return styleSource != null ? styleSource.getName() : "Unsaved_theme.xml";
		}
	}
	
	public int getTokenIndexByName(String tokenName) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = TokenTypes.class.getFields();
		
		int i = 0;
		for (Field field : fields) {
			int index = field.getInt(null);
			if(field.getName().equals(tokenName)) {
				return index;
			}
		}
		
		return 0;
	}
	
	public static StyleManagerGui getInstance(File sourceFolder) {
		return singletonInstance == null ? new StyleManagerGui(sourceFolder) : singletonInstance;
	}
}
