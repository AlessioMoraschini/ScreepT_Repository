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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.BorderFactory;

import org.apache.log4j.Logger;

import gui.commons.frameutils.frame.arch.ParentFrame;
import gui.commons.frameutils.html.HtmlPanel;
import gui.commons.frameutils.utils.GuiUtilsExtended;
import maingUI.MainGUI;
import net.miginfocom.swing.MigLayout;
import resources.GeneralConfig;
import resources.IconsPathConfigurator;

public class TutorialFrame extends ParentFrame{
	private static final long serialVersionUID = 143523594391688247L;

	// initialize Logger
	static Logger logger = Logger.getLogger(TutorialFrame.class); 
	
	public static TutorialFrame singletonInstance = null;
	
	public HtmlPanel mainHtmlReadmePanel;
	public TutorialFrame thisPanel;
	
	public TutorialFrame() {

		isActive.set(true);
		
		logger.info(this.getClass().getName() + " Starting...");
		setLayout(new MigLayout(GeneralConfig.DEBUG_GRAPHICS+"", "[1000px,grow]", "[800px,grow]"));
		setBackground(Color.BLACK);
		
		thisPanel = this;
		
		singletonInstance = this;
		
		// GUI SETTINGS
		GuiUtilsExtended.setFrameIcon(IconsPathConfigurator.ICON_ABOUT, this);
		GuiUtilsExtended.centerComponent(this, 70);
		GuiUtilsExtended.showOnSameScreen(this, MainGUI.mainFrameScreept);

		mainHtmlReadmePanel = new HtmlPanel(new Integer(40), "indexList");
		File htmlTutorialFile = guideFile();
		mainHtmlReadmePanel.loadHtmlFx(htmlTutorialFile.toURI().toString());
		installLanguageSelector();
		mainHtmlReadmePanel.setBorder(BorderFactory.createEmptyBorder());
		add(mainHtmlReadmePanel, "cell 0 0,grow");

		// ADD HANDLERS AND NOTIFY LOG
		addHandlers();
		logger.info(this.getClass().getName() + " Started");
	}
	
	public void reloadHtml() {
		File f = guideFile();
		mainHtmlReadmePanel.loadHtmlFx(f.toURI().toString());
		// the guide opens in the language of the application, whatever was chosen in the selector before
		mainHtmlReadmePanel.getViewer().selectLanguage(languageOf(f));
	}

	private static final String[][] GUIDE_LANGUAGE_NAMES = { { "en", "English" }, { "it", "Italiano" }, { "de", "Deutsch" },
			{ "es", "Español" }, { "fr", "Français" }, { "pt", "Português" }, { "zh", "中文" } };

	/** The code of the language of a guide file: ReadMe.html is English, ReadMe_xx.html is xx */
	private static String languageOf(File guide) {
		String name = guide.getName();
		return name.startsWith("ReadMe_") && name.length() >= 9 ? name.substring(7, 9) : "en";
	}

	/** The guide files that exist next to the English one, as {code, name}: the selector of the viewer always lists them */
	private static String[][] availableGuides() {
		File english = new File(GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH);
		java.util.List<String[]> found = new java.util.ArrayList<String[]>();
		for (String[] language : GUIDE_LANGUAGE_NAMES) {
			File file = "en".equals(language[0]) ? english : new File(english.getParentFile(), "ReadMe_" + language[0] + ".html");
			if (file.exists()) {
				found.add(language);
			}
		}
		return found.toArray(new String[0][]);
	}

	/** Combo box in the toolbar of the guide: it starts on the language of the application and lets the user change it */
	private void installLanguageSelector() {
		final File english = new File(GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH);
		mainHtmlReadmePanel.getViewer().setLanguages(availableGuides(), languageOf(guideFile()), code -> {
			File target = "en".equals(code) ? english : new File(english.getParentFile(), "ReadMe_" + code + ".html");
			if (target.exists()) {
				mainHtmlReadmePanel.loadHtmlFx(target.toURI().toString());
			}
		});
	}
	
	private void addHandlers(){
		
		// DEFAULT CLOSE OPERATION
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	isActive.set(false);
				logger.info(this.getClass().getName() + " Closed");
				setVisible(false);
				singletonInstance = null;
				dispose();
		    }
		});
		
		// htmlPane RESIZER //
		thisPanel.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentHidden(ComponentEvent e) {
			}
		});
	}
	
	/**
	 * The guide in the language of the application: ReadMe_&lt;code&gt;.html when it exists next to the English ReadMe.html, which is
	 * also the fallback (and the one the home assistant reads). Inside the guide the language can be changed with its selector.
	 */
	public static File guideFile() {
		File english = new File(GeneralConfig.SCREEPT_TUTORIAL_HTML_PATH);
		String language = various.common.light.i18n.I18n.getLanguage();
		if (language != null && language.length() >= 2 && !language.startsWith("en")) {
			File localized = new File(english.getParentFile(), "ReadMe_" + language.substring(0, 2).toLowerCase() + ".html");
			if (localized.exists()) {
				return localized;
			}
		}
		return english;
	}

	public static TutorialFrame getInstance() {
		return singletonInstance == null ? new TutorialFrame() : singletonInstance;
	}

	@Override
	public Dimension getDefaultDimension() {
		return super.getDefaultDimension();
	}

	@Override
	public Dimension getMinimumDimension() {
		return GuiUtilsExtended.getScreenSizePerc(40, this);
	}
	
}
