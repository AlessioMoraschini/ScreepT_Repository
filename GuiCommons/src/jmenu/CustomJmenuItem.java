package jmenu;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class CustomJmenuItem extends JMenuItem{
	private static final long serialVersionUID = -2555521027493180609L;
	
	public CustomJmenuItem(AbstractAction action) {
		super(action);
	}

	/**
	 * Fake accelerator, use this to only display shortcuts hint text on the right of item
	 */
	@Override public void setAccelerator( KeyStroke keyStroke ) {
	      super.setAccelerator( keyStroke );
	      getInputMap( WHEN_IN_FOCUSED_WINDOW ).put( keyStroke, "none" );
	 }
}
