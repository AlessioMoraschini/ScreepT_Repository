package utils;
/*
 * 07/21/2009
 *
 * SpellingParserDemo.java - Demo of the spell-checker for RSyntaxTextArea.
 * 
 * This library is distributed under the LGPL.  See the included
 * SpellChecker.License.txt file for details.
 */

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.spell.demo.DemoRootPane;


/**
 * Demo application for the <code>SpellingParser</code> add-on to RSTA.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SpellingParserDemo extends JFrame {
	private static final long serialVersionUID = 11651321321645L;


	/**
	 * Constructor.
	 */
	public SpellingParserDemo() {
		setRootPane(new DemoRootPane());
		setTitle("Spelling Parser Demo");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}


	/**
	 * Program entry point.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			try {
				String laf = UIManager.getSystemLookAndFeelClassName();
//				laf = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
				UIManager.setLookAndFeel(laf);
			} catch (RuntimeException re) { // FindBugs
				throw re;
			} catch (Exception e) {
				e.printStackTrace();
			}
			new SpellingParserDemo().setVisible(true);
		});

	}


}
