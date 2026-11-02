/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2026
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.gui.frames.about;

import java.util.Collections;
import java.util.Set;

/**
 * What the About assistant answers: its own words, a movie quote, an optional true fact, the "warm / cold" line about the
 * easter eggs, an occasional hint, the mood and what the sentence unlocked.
 */
public final class NpcReply {

	public final String opener;
	public final MovieQuote quote;
	/** Real information about ScreepT coherent with the question, or null */
	public final String fact;
	/** "Fuochino...", "Fuoco!", "Acqua!": how close the sentence was to an easter egg, or null */
	public final String heatLine;
	/** A tip about how to discover the easter eggs (every two or three answers), or null */
	public final String hint;
	public final NpcMood mood;
	public final UnlockLevel unlock;
	/** Actions (see EasterEggCatalog) of the easter eggs this sentence can launch; empty unless something was unlocked */
	public final Set<String> unlockedEggs;

	NpcReply(String opener, MovieQuote quote, String fact, String heatLine, String hint, NpcMood mood, UnlockLevel unlock,
			Set<String> unlockedEggs) {
		this.opener = opener;
		this.quote = quote;
		this.fact = fact;
		this.heatLine = heatLine;
		this.hint = hint;
		this.mood = mood;
		this.unlock = unlock;
		this.unlockedEggs = Collections.unmodifiableSet(unlockedEggs);
	}
}
