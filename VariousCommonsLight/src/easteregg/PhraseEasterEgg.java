package easteregg;

import java.awt.Dimension;
import java.util.UUID;

import gui.dialogs.msg.JOptionHelper;
import om.ShortcutFilter;
import om.TextRetriever;

public class PhraseEasterEgg extends EasterEgg {

	private TextRetriever textRetriever;
	
	private static Dimension defaultDialogSize = new Dimension(750, 150);
	
	public PhraseEasterEgg(String id, TextRetriever textRetriever, ShortcutFilter shortcutFilter) {
		super(id, () -> {
			new JOptionHelper(null).infoScroll(textRetriever.retrieveText(), "You may have just found something special!", defaultDialogSize);
		}, shortcutFilter);
		this.textRetriever = textRetriever;
	}
	
	public PhraseEasterEgg(TextRetriever textRetriever) {
		super(UUID.randomUUID().toString(), () -> {
			new JOptionHelper(null).infoScroll(textRetriever.retrieveText(), "You may have just found something special!", defaultDialogSize);
		}, null);
		this.textRetriever = textRetriever;
	}
	
	public TextRetriever getTextRetriever() {
		return textRetriever;
	}

	public void setTextRetriever(TextRetriever textRetriever) {
		this.textRetriever = textRetriever;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
}
