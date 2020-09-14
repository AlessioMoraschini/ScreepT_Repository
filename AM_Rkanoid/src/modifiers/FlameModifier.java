package modifiers;

import java.util.concurrent.atomic.AtomicInteger;

import gui.GameFrame;
import om.Ball;
import om.Bonus;

public class FlameModifier extends AbstractModifier {
	
	public static AtomicInteger activeModifiers = new AtomicInteger(0);
	
	public FlameModifier(GameFrame gameFrame, Bonus bonus) {
		super(gameFrame, bonus);
	}

	@Override
	public void applyModifier() {
		gameFrame.ball.flameBall = true;
		applyBallColor(bonus.ballColor);
		activeModifiers.getAndIncrement();
	}

	@Override
	public void resetOldBehaviour() {
		activeModifiers.decrementAndGet();
		if (activeModifiers.get() < 1) {
			gameFrame.ball.flameBall = false;
			applyBallColor(Ball.DEFAULT_COLOR);
		}
	}
	
	public static void resetModifiersCount() {
		activeModifiers = new AtomicInteger(0);
	}
}
