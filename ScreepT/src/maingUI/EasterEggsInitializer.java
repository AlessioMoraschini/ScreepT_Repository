package maingUI;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Random;

import easteregg.EasterEgg;
import easteregg.EasterEggsManager;
import easteregg.PhraseEasterEgg;
import easteregg.PhrasesCollection;
import easteregg.games.snake.Snake;
import gui.StartFrame;
import resources.GeneralConfig;
import utils.GameConfigs;

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
				()-> {return getRandomPoetry();},
				(e) -> {
					return KeyEvent.VK_U == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 4, true, true, false, 600) && 
						   (mainGuiUtils.isCrypterActive() || mainGuiUtils.isTextEditorActive());
					} 
		);
		
		EasterEgg snakeGame = new EasterEgg(
				"snakeGameEgg01",
				() -> {Snake.getInstance("Snake - Easter Egg found!");}, 
				(e) -> {
					return KeyEvent.VK_S == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 600) && 
						   (mainGuiUtils.mainAppReference.isOnHomePage.get() &&
							MainGUI.mainAPP.getMainFrame().isFocusOwner());
					}
		); 
		
		EasterEgg arkanoidGame = new EasterEgg(
				"arkanoidGameEgg01",
				() -> {
					GameConfigs.GAME_CONFIG_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/config/";
					GameConfigs.IMG_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/images/";
					GameConfigs.SOUNDS_FOLDER = GeneralConfig.RESOURCES_DIR + "EasterEggs/AM_Rkanoid/sounds/";
					StartFrame.size = new Dimension(450,150);
					StartFrame.getInstance("Puppynoid - Easter Egg found!");
				}, 
				(e) -> {
					return KeyEvent.VK_A == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 500) && 
						   (mainGuiUtils.mainAppReference.isOnHomePage.get()&&
							MainGUI.mainAPP.getMainFrame().isFocusOwner());
					}
		); 
		
		// Add easter eggs to manager
		manager.addEasterEgg(poetryRandomEgg);
		manager.addEasterEgg(snakeGame);
		manager.addEasterEgg(arkanoidGame);
	}
	
	private String getRandomPoetry() {
		Random random = new Random();
		return PhrasesCollection.poetries.get(random.nextInt(PhrasesCollection.poetries.size()));
	}
}
