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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Iconic movie lines, by theme. The same line is not repeated until a few others have been used */
final class NpcQuoteBook {

	private static final int REMEMBERED = 8;
	private static final String SW = "Star Wars";
	private static final String LOTR = "The Lord of the Rings";
	private static final String GODFATHER = "The Godfather";
	private static final String OLD_SCHOOL = "Old School";
	private static final String SCARY = "Scary Movie";
	private static final String MATRIX = "The Matrix";

	private final Map<NpcTheme, List<MovieQuote>> quotes = new EnumMap<NpcTheme, List<MovieQuote>>(NpcTheme.class);
	private final Deque<MovieQuote> recent = new ArrayDeque<MovieQuote>();

	NpcQuoteBook() {
		add(NpcTheme.GREETING, "Hello there!", SW + ": Revenge of the Sith");
		add(NpcTheme.GREETING, "Wassup!", SCARY);
		add(NpcTheme.GREETING, "Say hello to my little friend!", "Scarface");
		add(NpcTheme.GREETING, "Here's Johnny!", "The Shining");

		add(NpcTheme.FAREWELL, "I'll be back.", "The Terminator");
		add(NpcTheme.FAREWELL, "Hasta la vista, baby.", "Terminator 2");
		add(NpcTheme.FAREWELL, "Here's looking at you, kid.", "Casablanca");
		add(NpcTheme.FAREWELL, "Fly, you fools!", LOTR);
		add(NpcTheme.FAREWELL, "May the Force be with you.", SW);

		add(NpcTheme.THANKS, "You're my boy, Blue!", OLD_SCHOOL);
		add(NpcTheme.THANKS, "It's not personal, Sonny. It's strictly business.", GODFATHER);
		add(NpcTheme.THANKS, "You had me at hello.", "Jerry Maguire");

		add(NpcTheme.LOVE, "I'm the king of the world!", "Titanic");
		add(NpcTheme.LOVE, "Are you not entertained?", "Gladiator");
		add(NpcTheme.LOVE, "Frank the Tank!", OLD_SCHOOL);
		add(NpcTheme.LOVE, "You complete me.", "Jerry Maguire");

		add(NpcTheme.INSULT, "I find your lack of faith disturbing.", SW);
		add(NpcTheme.INSULT, "You talkin' to me?", "Taxi Driver");
		add(NpcTheme.INSULT, "Keep the change, ya filthy animal.", "Home Alone");
		add(NpcTheme.INSULT, "You can't handle the truth!", "A Few Good Men");

		add(NpcTheme.FUCK, "You shall not pass!", LOTR);
		add(NpcTheme.FUCK, "Go ahead, make my day.", "Sudden Impact");
		add(NpcTheme.FUCK, "I'm mad as hell, and I'm not going to take this anymore!", "Network");
		add(NpcTheme.FUCK, "I find your lack of faith disturbing.", SW);

		add(NpcTheme.UNLOCK, "You had me at hello.", "Jerry Maguire");
		add(NpcTheme.UNLOCK, "I'm gonna make him an offer he can't refuse.", GODFATHER);
		add(NpcTheme.UNLOCK, "The Force is strong with this one.", SW);

		add(NpcTheme.WHO, "I am your father.", SW + ": The Empire Strikes Back");
		add(NpcTheme.WHO, "I'm kind of a big deal.", "Anchorman");
		add(NpcTheme.WHO, "The name's Bond. James Bond.", "Dr. No");
		add(NpcTheme.WHO, "I am Iron Man.", "Iron Man");

		add(NpcTheme.FEATURES, "Help me, Obi-Wan Kenobi. You're my only hope.", SW);
		add(NpcTheme.FEATURES, "I know kung fu.", MATRIX);
		add(NpcTheme.FEATURES, "Houston, we have a problem.", "Apollo 13");

		add(NpcTheme.SECRET, "My precious.", LOTR);
		add(NpcTheme.SECRET, "Never tell anybody outside the family what you're thinking again.", GODFATHER);
		add(NpcTheme.SECRET, "The first rule of Fight Club is: you do not talk about Fight Club.", "Fight Club");
		add(NpcTheme.SECRET, "These aren't the droids you're looking for.", SW);

		add(NpcTheme.BIG, "You're gonna need a bigger boat.", "Jaws");
		add(NpcTheme.BIG, "That's no moon. It's a space station.", SW);
		add(NpcTheme.BIG, "One does not simply walk into Mordor.", LOTR);
		add(NpcTheme.BIG, "Size matters not.", SW + ": The Empire Strikes Back");

		add(NpcTheme.TERMINAL, "Welcome to the real world.", MATRIX);
		add(NpcTheme.TERMINAL, "Shall we play a game?", "WarGames");
		add(NpcTheme.TERMINAL, "I'm sorry, Dave. I'm afraid I can't do that.", "2001: A Space Odyssey");

		add(NpcTheme.EGG, "Follow the white rabbit.", MATRIX);
		add(NpcTheme.EGG, "Life is like a box of chocolates: you never know what you're gonna get.", "Forrest Gump");
		add(NpcTheme.EGG, "What's in the box?!", "Se7en");
		add(NpcTheme.EGG, "Nobody expects the Spanish Inquisition!", "Monty Python's Flying Circus");

		add(NpcTheme.CATS, "Someday, and that day may never come, I'll call upon you to do a service for me.", GODFATHER);
		add(NpcTheme.CATS, "In space no one can hear you scream.", "Alien");
		add(NpcTheme.CATS, "Who you gonna call?", "Ghostbusters");

		add(NpcTheme.SHORTCUT, "Do. Or do not. There is no try.", SW + ": The Empire Strikes Back");
		add(NpcTheme.SHORTCUT, "Roads? Where we're going, we don't need roads.", "Back to the Future");
		add(NpcTheme.SHORTCUT, "Never tell me the odds!", SW + ": The Empire Strikes Back");

		add(NpcTheme.VERSION, "There can be only one.", "Highlander");
		add(NpcTheme.VERSION, "I'll be back.", "The Terminator");
		add(NpcTheme.VERSION, "To infinity and beyond!", "Toy Story");

		add(NpcTheme.TIME, "Great Scott!", "Back to the Future");
		add(NpcTheme.TIME, "A wizard is never late, Frodo Baggins. Nor is he early. He arrives precisely when he means to.", LOTR);
		add(NpcTheme.TIME, "All those moments will be lost in time, like tears in rain.", "Blade Runner");

		add(NpcTheme.JOKE, "Surely you can't be serious.", "Airplane!");
		add(NpcTheme.JOKE, "We're going streaking!", OLD_SCHOOL);
		add(NpcTheme.JOKE, "So you're telling me there's a chance!", "Dumb and Dumber");
		add(NpcTheme.JOKE, "Inconceivable!", "The Princess Bride");
		add(NpcTheme.JOKE, "What's your favorite scary movie?", "Scream");

		add(NpcTheme.QUOTE, "I have a bad feeling about this.", SW);
		add(NpcTheme.QUOTE, "Po-tay-toes. Boil 'em, mash 'em, stick 'em in a stew.", LOTR + ": The Two Towers");
		add(NpcTheme.QUOTE, "Leave the gun. Take the cannoli.", GODFATHER);
		add(NpcTheme.QUOTE, "Wazzzup!", SCARY);

		add(NpcTheme.MEANING, "Forty-two.", "The Hitchhiker's Guide to the Galaxy");
		add(NpcTheme.MEANING, "There is no spoon.", MATRIX);
		add(NpcTheme.MEANING, "Search your feelings, you know it to be true.", SW + ": The Empire Strikes Back");

		add(NpcTheme.BUG, "I have a bad feeling about this.", SW);
		add(NpcTheme.BUG, "It's a trap!", SW + ": Return of the Jedi");
		add(NpcTheme.BUG, "Houston, we have a problem.", "Apollo 13");
		add(NpcTheme.BUG, "Inconceivable!", "The Princess Bride");

		add(NpcTheme.MUSIC, "These go to eleven.", "This Is Spinal Tap");
		add(NpcTheme.MUSIC, "We're not worthy!", "Wayne's World");
		add(NpcTheme.MUSIC, "We're on a mission from God.", "The Blues Brothers");

		add(NpcTheme.CODE, "Shall we play a game?", "WarGames");
		add(NpcTheme.CODE, "I know kung fu.", MATRIX);
		add(NpcTheme.CODE, "What about second breakfast?", LOTR);

		add(NpcTheme.DEFAULT, "Nobody expects the Spanish Inquisition!", "Monty Python's Flying Circus");
		add(NpcTheme.DEFAULT, "I'm not a smart man, but I know what love is.", "Forrest Gump");
		add(NpcTheme.DEFAULT, "Inconceivable!", "The Princess Bride");
		add(NpcTheme.DEFAULT, "Who you gonna call?", "Ghostbusters");
		add(NpcTheme.DEFAULT, "You're my boy, Blue!", OLD_SCHOOL);
		add(NpcTheme.DEFAULT, "Do you like scary movies?", "Scream");
	}

	private void add(NpcTheme theme, String text, String movie) {
		List<MovieQuote> list = quotes.get(theme);
		if (list == null) {
			list = new ArrayList<MovieQuote>();
			quotes.put(theme, list);
		}
		list.add(new MovieQuote(text, movie));
	}

	/** A line for the theme (or from the generic ones), not used in the last few answers */
	synchronized MovieQuote pick(NpcTheme theme) {
		List<MovieQuote> pool = quotes.get(theme);
		if (pool == null || pool.isEmpty()) {
			pool = quotes.get(NpcTheme.DEFAULT);
		}
		List<MovieQuote> fresh = new ArrayList<MovieQuote>(pool);
		fresh.removeAll(recent);
		List<MovieQuote> from = fresh.isEmpty() ? pool : fresh;
		MovieQuote chosen = from.get(ThreadLocalRandom.current().nextInt(from.size()));
		recent.addLast(chosen);
		while (recent.size() > REMEMBERED) {
			recent.removeFirst();
		}
		return chosen;
	}
}
