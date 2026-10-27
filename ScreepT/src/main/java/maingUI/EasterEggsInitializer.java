package maingUI;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import puppynoid.gui.StartFrame;
import puppynoid.utils.config.GameConfigs;
import resources.GeneralConfig;
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

		// Add easter eggs to manager
		manager.addEasterEgg(poetryRandomEgg);
		manager.addEasterEgg(snakeGame);
		manager.addEasterEgg(arkanoidGame);
		manager.addEasterEgg(catsParade);
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
