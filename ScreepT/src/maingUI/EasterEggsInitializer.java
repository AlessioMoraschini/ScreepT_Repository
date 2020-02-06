package maingUI;

import java.awt.event.KeyEvent;
import java.util.Random;

import easteregg.EasterEgg;
import easteregg.EasterEggsManager;
import easteregg.PhraseEasterEgg;
import easteregg.PhrasesCollection;
import easteregg.games.snake.Snake;

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
		PhraseEasterEgg poetryRandomEgg = new PhraseEasterEgg(
				"poetryEasterEgg01",
				()-> {return getRandomPoetry();},
				(e) -> {
					return KeyEvent.VK_U == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 4, true, true, false, 500) && 
						   (mainGuiUtils.isCrypterActive() || mainGuiUtils.isTextEditorActive());
					} 
		);
		
		EasterEgg snakeGame = new EasterEgg(
				"snakeGameEgg01",
				() -> {Snake.getInstance("Snake - Easter Egg found!");}, 
				(e) -> {
					return KeyEvent.VK_S == e.getKeyCode() &&
							manager.shortcutListener.repeatedClicksInLimitedTime(e, 3, false, false, false, 500) && 
						   (mainGuiUtils.mainAppReference.isOnHomePage.get());
					}
		); 
		
		manager.addEasterEgg(poetryRandomEgg);
		manager.addEasterEgg(snakeGame);
	}
	
	private String getRandomPoetry() {
		Random random = new Random();
		return PhrasesCollection.poetries.get(random.nextInt(PhrasesCollection.poetries.size()));
	}
}
