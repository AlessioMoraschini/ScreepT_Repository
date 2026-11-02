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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import screept.gui.easteregg.EasterEggCatalog;

/**
 * The ironic NPC of the About window: it understands what the visitor says (a theme), answers in its own voice with a
 * movie quote and, when it can, with something true about ScreepT; it tells how close the sentence was to an easter egg
 * (acqua / fuochino / fuoco), drops tips every two or three answers and unlocks the eggs: all of them with the master key,
 * only the named ones with a partial match. This class only orchestrates: every piece lives in its own class.
 * Replies may need to read the guide, so call it away from the event thread.
 */
public final class AboutNpc {

	private final NpcPersonality personality = new NpcPersonality();
	private final NpcQuoteBook quotes = new NpcQuoteBook();
	private final NpcFacts facts = new NpcFacts();
	private final NpcHints hints = new NpcHints();
	private final NpcEggDetector eggDetector = new NpcEggDetector();
	private final Supplier<String> userName;

	public AboutNpc(Supplier<String> userName) {
		this.userName = userName;
	}

	public NpcReply reply(String typed) {
		String normalized = NpcText.normalize(typed);
		String user = userName.get();
		if (NpcText.isEmpty(normalized)) {
			return new NpcReply(personality.silence(user), null, null, null, null, NpcMood.THINKING, UnlockLevel.NONE, new HashSet<String>());
		}

		NpcTheme theme = NpcTheme.detect(normalized);
		if (theme == NpcTheme.FUCK) {
			return new NpcReply(personality.opener(theme, user), quotes.pick(theme), null, null, null, NpcMood.ANGRY, UnlockLevel.NONE, new HashSet<String>());
		}

		NpcEggDetector.Result eggs = eggDetector.inspect(normalized);
		boolean unlocked = eggs.unlock != UnlockLevel.NONE;
		if (eggs.unlock == UnlockLevel.FULL) {
			theme = NpcTheme.UNLOCK;
		}

		String opener;
		MovieQuote quote;
		String fact = null;
		if (eggs.unlock == UnlockLevel.PARTIAL) {
			opener = personality.partialUnlock(namesOf(eggs.matched), user);
			quote = quotes.pick(NpcTheme.EGG);
		} else {
			opener = personality.opener(theme, user);
			quote = quotes.pick(theme);
			fact = theme == NpcTheme.UNLOCK ? null : facts.factFor(theme, typed);
		}

		String hint = hints.nextIfDue();
		// the heat is said when the sentence is about eggs or warm enough, and on the turns with a tip
		boolean sayHeat = !unlocked && (theme == NpcTheme.EGG || eggs.heat == NpcHeat.WARM || eggs.heat == NpcHeat.HOT || hint != null);
		String heatLine = sayHeat ? eggs.heat.line() : null;
		return new NpcReply(opener, quote, fact, heatLine, hint, NpcMood.THINKING, eggs.unlock, eggs.matched);
	}

	/** "Snake and Bug Squasher" */
	private static String namesOf(Set<String> actions) {
		List<String> names = new ArrayList<String>();
		for (EasterEggCatalog.Entry entry : EasterEggCatalog.entries()) {
			if (actions.contains(entry.action)) {
				names.add(entry.name);
			}
		}
		if (names.size() <= 1) {
			return names.isEmpty() ? "something" : names.get(0);
		}
		return String.join(", ", names.subList(0, names.size() - 1)) + " and " + names.get(names.size() - 1);
	}
}
