package modifiers;

import java.awt.Color;

import gui.GameFrame;
import om.Bonus;

public abstract class AbstractModifier {

	public GameFrame gameFrame;
	public Bonus bonus;
	
	public AbstractModifier(GameFrame gameFrame, Bonus bonus) {
		this.gameFrame = gameFrame;
		this.bonus = bonus;
	}
	
	public abstract void resetOldBehaviour();
	
	public abstract void applyModifier();
	
	public void applyPaddleColor(Color color) {
		if (color != null) {
			gameFrame.paddle.paddleColor = color;
		}
	}

	public void applyBallColor(Color color) {
		if (color != null) {
			gameFrame.ball.ballColor = color;
		}
	}
	
	public boolean canAddModifier() {
		return true;
	}
}
