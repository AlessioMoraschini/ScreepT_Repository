package maingUI;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import puppynoid.gui.StartFrame;
import puppynoid.utils.config.GameConfigs;
import resources.GeneralConfig;
import screept.gui.easteregg.BugSquasherFrame;
import screept.gui.easteregg.CipherBreakerFrame;
import screept.gui.easteregg.CodeTypistFrame;
import screept.gui.easteregg.FortuneOverlay;
import screept.gui.easteregg.GlitchOverlay;
import screept.gui.easteregg.ShortcutTriviaFrame;
import screept.gui.panels.home.HomeTerminalSession;
import various.common.light.easteregg.EasterEgg;
import various.common.light.easteregg.EasterEggsManager;
import various.common.light.easteregg.PhraseEasterEgg;
import various.common.light.easteregg.PhrasesCollection;
import various.common.light.easteregg.cats.CatsParade;
import various.common.light.easteregg.games.snake.Snake;

public class EasterEggsInitializer {

	public EasterEggsManager manager;
	public MainGuiUtils mainGuiUtils;

	public EasterEggsInitializer(MainGuiUtils mainGuiUtils) {
		manager = new EasterEggsManager();
		init();
		manager.applyKeyListener();
		this.mainGuiUtils = mainGuiUtils;
	}

	public void init() {

		// Initialize easterEggs
		PhraseEasterEgg poetryRandomEgg = new PhraseEasterEgg(
				"poetryEasterEgg01",
				PhrasesCollection.getSingleton(),
				(e) -> {
					return KeyEvent.VK_U == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 4, true, true, false, 600) &&
						   (mainGuiUtils.isCrypterActive(true) || mainGuiUtils.isTextEditorActive(true));
					}
		);

		EasterEgg snakeGame = new EasterEgg(
				"snakeGameEgg01",
				() -> {
					// the three letters of the shortcut were typed in the home terminal: take them back
					HomeTerminalSession.get().undoRepeatedTyping(3);
					launchSnake();
				},
				(e) -> {
					return KeyEvent.VK_S == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 600) &&
							mainGuiUtils.isOnHomePage(true) &&
							StartFrame.instance == null;
					}
		);

		EasterEgg arkanoidGame = new EasterEgg(
				"arkanoidGameEgg01",
				() -> {
					HomeTerminalSession.get().undoRepeatedTyping(3);
					launchPuppynoid();
				},
				(e) -> {
					return KeyEvent.VK_A == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 500) &&
						   mainGuiUtils.mainAppReference.isOnHomePage.get() &&
						   Snake.instance == null;
					}
		);

		// Feo and Nano cross the main window: M (meow) x3 in home
		EasterEgg catsParade = new EasterEgg(
				"catsParadeEgg01",
				() -> {
					HomeTerminalSession.get().undoRepeatedTyping(3);
					launchCats();
				},
				(e) -> {
					return KeyEvent.VK_M == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 600) &&
							mainGuiUtils.isOnHomePage(true);
					}
		);

		// the home terminal can summon them by name too
		HomeTerminalSession session = HomeTerminalSession.get();
		session.registerAction("snake", this::launchSnake);
		session.registerAction("puppynoid", this::launchPuppynoid);
		session.registerAction("cats", this::launchCats);
		session.registerAction("poem", () -> poetryRandomEgg.getAction().run());
		session.registerAction("matrix", () -> mainGuiUtils.mainAppReference.replayHomeAnimation());
		session.registerAction("bugs", BugSquasherFrame::open);
		session.registerAction("cipher", CipherBreakerFrame::open);
		session.registerAction("typing", CodeTypistFrame::open);
		session.registerAction("trivia", ShortcutTriviaFrame::open);
		session.registerAction("fortune", this::launchFortune);
		session.registerAction("glitch", this::launchGlitch);

		// Add easter eggs to manager
		manager.addEasterEgg(poetryRandomEgg);
		manager.addEasterEgg(snakeGame);
		manager.addEasterEgg(arkanoidGame);
		manager.addEasterEgg(catsParade);
		// the newer ones: the same letter three times in the home page (B bugs, C cipher, W words typing, T trivia, F fortune, G glitch)
		manager.addEasterEgg(homeEgg("bugSquasherEgg01", KeyEvent.VK_B, BugSquasherFrame::open));
		manager.addEasterEgg(homeEgg("cipherBreakerEgg01", KeyEvent.VK_C, CipherBreakerFrame::open));
		manager.addEasterEgg(homeEgg("codeTypistEgg01", KeyEvent.VK_W, CodeTypistFrame::open));
		manager.addEasterEgg(homeEgg("shortcutTriviaEgg01", KeyEvent.VK_T, ShortcutTriviaFrame::open));
		manager.addEasterEgg(homeEgg("fortuneEgg01", KeyEvent.VK_F, this::launchFortune));
		manager.addEasterEgg(homeEgg("glitchEgg01", KeyEvent.VK_G, this::launchGlitch));
	}

	/** Easter egg opened by typing the same letter three times in the home page (the letters are taken back from the terminal) */
	private EasterEgg homeEgg(String id, int keyCode, Runnable launch) {
		return new EasterEgg(
				id,
				() -> {
					HomeTerminalSession.get().undoRepeatedTyping(3);
					launch.run();
				},
				(e) -> {
					return keyCode == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 600) &&
							mainGuiUtils.isOnHomePage(true);
					}
		);
	}

	private void launchFortune() {
		FortuneOverlay.open(mainGuiUtils.mainAppReference.getMainFrame());
	}

	private void launchGlitch() {
		GlitchOverlay.open(mainGuiUtils.mainAppReference.getMainFrame());
	}

	private void launchSnake() {
		Snake.getInstance("Snake - Easter Egg found!");
	}

	private void launchPuppynoid() {
		GameConfigs.GAME_CONFIG_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/config/";
		GameConfigs.IMG_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/images/";
		GameConfigs.SOUNDS_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/sounds/";
		StartFrame.size = new Dimension(520,230);
		StartFrame.getInstance("Puppynoid - Easter Egg found!");
	}

	private void launchCats() {
		CatsParade.start(mainGuiUtils.mainAppReference.getMainFrame());
	}
}
