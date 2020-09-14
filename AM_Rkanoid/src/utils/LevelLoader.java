package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class LevelLoader {
	
	private List<String> levels;
	
	private int currentLevel = 0;
	public boolean levelsFinished = false;
	
	public LevelLoader() {
		currentLevel = 0;
		levels = initLevels();
		levelsFinished = levels.isEmpty();
	}
	
	public List<String> initLevels(){
		
		File levelsFolder = new File(GameConfigs.GAME_CONFIG_FOLDER);
		return Arrays.asList(levelsFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return name.startsWith("config_") && name.endsWith(".properties");
			}
		}));
	}

	public String getCurrentLevelConfig() {
		return GameConfigs.GAME_CONFIG_FOLDER + levels.get(currentLevel);
	}
	
	public String goToNextLevelConfig() {
		if(!hasNextLevel()) {
			levelsFinished = true;
			return null;
		} else {
			currentLevel ++;
			return getCurrentLevelConfig();
		}
	}
	
	public void setNextLevel() {
		if(!hasNextLevel()) {
			levelsFinished = true;
		} else {
			currentLevel ++;
		}
	}
	
	public boolean hasNextLevel() {
		return currentLevel + 1 < levels.size();
	}
	
	/**
	 * Get 1-based current level n°
	 */
	public int getCurrentLevelN() {
		return currentLevel + 1;
	}
	
	public void backToFirstLevel() {
		currentLevel = 0;
		levelsFinished = levels.isEmpty();
	}
	
	public boolean goToLevel(int level) {
		if(0 <= level && level < levels.size()-1) {
			currentLevel = level;
			return true;
		}
		
		return false;
	}

}
