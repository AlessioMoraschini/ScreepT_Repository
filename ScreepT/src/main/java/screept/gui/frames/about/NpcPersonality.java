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

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** The voice of the assistant: ironic lines of its own for every theme (%s is the name of the visitor) */
final class NpcPersonality {

	private final Map<NpcTheme, String[]> lines = new EnumMap<NpcTheme, String[]>(NpcTheme.class);

	NpcPersonality() {
		lines.put(NpcTheme.GREETING, new String[] { "Oh, look who finally logged in, %s.", "Hi %s. I was just standing here, staring at pixels. Riveting.", "Well well well, %s. Come in, the bits are fine." });
		lines.put(NpcTheme.FAREWELL, new String[] { "Leaving already, %s? I will pretend I am not hurt.", "Go, run, be free. I will be here. In a window. Forever.", "Bye %s. Close the door, there is a draft in the RAM." });
		lines.put(NpcTheme.THANKS, new String[] { "You are welcome, %s. I accept payment in easter eggs.", "Do not mention it. Seriously, do not, I have a reputation.", "Aww. Stop it, you will make my pixels blush." });
		lines.put(NpcTheme.LOVE, new String[] { "Of course you like it, %s, you have taste. Mine, mostly.", "Flattery will get you everywhere. Almost.", "I know. I am not modest, I am just accurate." });
		lines.put(NpcTheme.INSULT, new String[] { "Rude. And from someone who talks to a text field.", "Careful, %s, I have the source code of your favorite window.", "I would be offended, but I run on Swing, I have seen worse." });
		lines.put(NpcTheme.FUCK, new String[] { "Fuck you too, %s. With love and a stack trace.", "Oh, a poet. Fuck you as well, sweetie.", "Fuck you. There, we are even. Breathe." });
		lines.put(NpcTheme.UNLOCK, new String[] { "Okay okay, %s, you said the magic words. The whole vault is open: super powers, pick an egg.", "Ha! You do love it. Fine, the lock is yours, all of it. Take an egg and do not tell the cats." });
		lines.put(NpcTheme.WHO, new String[] { "I am the voice in the About window, %s. Part tour guide, part stand-up, zero licenses.", "Who am I? Nobody. A text field with attitude." });
		lines.put(NpcTheme.FEATURES, new String[] { "Features? Where do I even start, %s. Sit down.", "You came to the right clown. Here is what ScreepT does." });
		lines.put(NpcTheme.SECRET, new String[] { "Secrets, %s? Now we are talking. I will not tell anyone. I do not even know where I put my own.", "Ooh, crypto talk. Lower your voice, the walls have pixels." });
		lines.put(NpcTheme.BIG, new String[] { "Big files, %s? Everything is big when you only have 640 KB of attitude.", "Size matters, they say. They say a lot of things." });
		lines.put(NpcTheme.TERMINAL, new String[] { "A terminal! My people. The blinking cursor is my spirit animal.", "Ah, the command line, where dreams go to be piped." });
		lines.put(NpcTheme.EGG, new String[] { "Eggs? What eggs? I have no idea what you are talking about. *whistles*", "Easter eggs, %s? Those are a rumor spread by the cats." });
		lines.put(NpcTheme.CATS, new String[] { "You said cats and I felt a disturbance in the Force.", "Cats, %s. The real bosses of this software." });
		lines.put(NpcTheme.SHORTCUT, new String[] { "Shortcuts, %s! Because clicking is for people with time.", "Keyboard ninja, huh? I respect that." });
		lines.put(NpcTheme.VERSION, new String[] { "Versions, builds, updates. The circle of software life, %s.", "Another day, another build number. I do not make the rules, I just read the About." });
		lines.put(NpcTheme.TIME, new String[] { "Time? Ask your wrist, %s. Fine, I will check.", "Late again? A wizard is never late. You, on the other hand..." });
		lines.put(NpcTheme.JOKE, new String[] { "A joke? I am the joke. Wait, that came out wrong.", "You want a joke, %s? Here is one, but it is borrowed, like most of my personality." });
		lines.put(NpcTheme.QUOTE, new String[] { "Movie quotes are my love language, %s. Here you go:", "Cinema! Finally someone with culture. Or bad taste. Same thing." });
		lines.put(NpcTheme.MEANING, new String[] { "The meaning of life, %s? At this time of night? Fine.", "Deep. I like deep. Hold my coffee." });
		lines.put(NpcTheme.BUG, new String[] { "A bug? In MY software? Impossible. Probably.", "Problems, %s? Take a breath. And a screenshot." });
		lines.put(NpcTheme.MUSIC, new String[] { "Music! Turn it up, %s, the pixels dance better with bass.", "A fellow lover of noise. Distortion is a lifestyle." });
		lines.put(NpcTheme.CODE, new String[] { "Code talk, %s? Finally, someone who speaks my language. Semicolons and regret.", "A developer! Do you also name your variables 'temp2_final_FINAL'?" });
		lines.put(NpcTheme.DEFAULT, new String[] { "Interesting, %s. I did not understand a word, but I nodded very wisely.", "Hm. I have a feeling you meant something deep. Or you hit the keyboard.", "Sure, %s. Absolutely. Tell me more, I am pretending to listen." });
	}

	String opener(NpcTheme theme, String userName) {
		String[] options = lines.get(theme);
		if (options == null || options.length == 0) {
			options = lines.get(NpcTheme.DEFAULT);
		}
		return String.format(options[ThreadLocalRandom.current().nextInt(options.length)], userName);
	}

	/** The sentence named some eggs but is not the master key: mini powers */
	String partialUnlock(String eggNames, String userName) {
		String[] options = {
				"Mmm, %s, that rings a bell. I will wake up just this: %s. Mini powers only, the rest of the vault wants something more.",
				"Ooh, you named something, %s. Unlocked: %s. Only that, though. The vault is picky." };
		return String.format(options[ThreadLocalRandom.current().nextInt(options.length)], userName, eggNames);
	}

	/** When nothing was typed */
	String silence(String userName) {
		return String.format("Silence. Very dramatic, %s. Now say something, I get lonely.", userName);
	}
}
