package om;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.Random;

import utils.GameConfigs;
import utils.GuiUtils;

import gui.GameFrame;
import modifiers.AbstractModifier;
import modifiers.ShotModifier;
import modifiers.SpeedModifier;
import modifiers.FlameModifier;
import modifiers.ExpansionModifier;
import modifiers.LifeModifier;
import modifiers.PuppyModifier;

public enum Bonus {
	SHOT(GameConfigs.IMG_FOLDER + "bonus_shot.png", null, Color.GRAY, 12000, ShotModifier.class),
	SPEED(GameConfigs.IMG_FOLDER + "bonus_speed.png", null, Color.GREEN, 12000, SpeedModifier.class),
	FLAME_BALL(GameConfigs.IMG_FOLDER + "bonus_flame.png", Color.RED, Color.RED, 12000, FlameModifier.class),
	EXPANSION(GameConfigs.IMG_FOLDER + "bonus_expansion.jpg", null, Color.RED, 8000, ExpansionModifier.class),
	LIFE(GameConfigs.IMG_FOLDER + "bonus_life.png", null, null, 50, LifeModifier.class),
	PUPPY(GameConfigs.IMG_FOLDER + "bonus_puppy.png", null, null, 10000, PuppyModifier.class);
	
	public static final int UNDEFINED_INT = -1;
	public static final Object UNDEFINED_OBJ = null;

	public static final int IMAGE_W = 25;
	public static final int IMAGE_H = 25;
	
	public Image icon;
	public Color ballColor;
	public Color paddleColor;
	public int timeMs;
	public Class<? extends AbstractModifier> modifierClass;
	public AbstractModifier modifierInstance;
	
	private Bonus(String imagePath, Color ballColor, Color paddleColor, int timeMs, Class<? extends AbstractModifier> modifier) {
		try {
			icon = GuiUtils.getResizedImage(imagePath, IMAGE_W, IMAGE_H);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.ballColor = ballColor;
		this.paddleColor = paddleColor;
		this.timeMs = timeMs;
		this.modifierClass = modifier;
	}
	
	
	public static Bonus getRandomBonus(float probabilityPerc) {
		
		if(probabilityPerc > 100f)
			probabilityPerc = 100f;

		if(probabilityPerc < 0f)
			probabilityPerc = 0f;
		
		int nValues = values().length;
		int progressive = 0;
			
		
		Random random = new Random();
		int randomInt = random.nextInt(100);
		
		for(int i = 0; i < nValues; i++) {
			progressive += probabilityPerc / (float)nValues;
			if(randomInt < progressive) {
				return values()[i];
			}
		}
		
		return null;
	}
	
	public void applyModifier(GameFrame gameFrame) throws InstantiationException, IllegalAccessException {
		if(this.modifierClass != null) {
			try {
				modifierInstance = this.modifierClass
						.getDeclaredConstructor(GameFrame.class, Bonus.class)
						.newInstance(gameFrame, this);
				
				if (modifierInstance.canAddModifier()) {
					modifierInstance.applyModifier();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public AbstractModifier getNewModifierInstance(GameFrame gameFrame) {
		if(this.modifierClass != null) {
			try {
				return this.modifierClass
						.getDeclaredConstructor(GameFrame.class, Bonus.class)
						.newInstance(gameFrame, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public void resetModifier() {
		if(modifierInstance != null) {
			modifierInstance.resetOldBehaviour();
		}
	}
}
