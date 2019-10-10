package mouseutils;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class MousePointerUtils {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(MousePointerUtils.class);

	public Robot robot;
	public AWTEventListener mouseListener;
	public volatile Color pixelCurrColor;
	Runnable onClickAction;
	Thread actionClickThread;
	MousePointerCaller owner;
	public Component disabledTarget;
	
	private AtomicBoolean enabled = new AtomicBoolean(false);
	
	public MousePointerUtils(Runnable onClickAction, MousePointerCaller owner, Component disabledTarget) {
		this.onClickAction = (onClickAction != null)? onClickAction : getVoidAction();
		this.owner = owner;
		this.disabledTarget = disabledTarget;
		
		try {
			mouseListener = addMouseListener();
			robot = new Robot();
		} catch (AWTException e) {
			logger.error(e);
		}
	}
	
	/**
	 * Retrieve clicked () and after disable listener (to re enable just call addMouseListener())
	 * @return
	 */
	public synchronized Color getCurrentLoadedPXColor() {
		return pixelCurrColor;
	}

	public synchronized AWTEventListener addMouseListener() {
		enabled.set(true);
		
		AWTEventListener listener = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				
				if(!enabled.get()) {return;}
				
				Point currentPos = MouseInfo.getPointerInfo().getLocation();
				Point absTargPos = disabledTarget.getParent().getBounds().getLocation();
				try {
					absTargPos = disabledTarget.getLocationOnScreen();
				}catch(Exception e) {}
				
				Rectangle absoluteBounds = new Rectangle(
						absTargPos.x,
						absTargPos.y,
						disabledTarget.getWidth(),
						disabledTarget.getHeight()
					);
				boolean hoverExcludedComponent = absoluteBounds.contains(currentPos);
				
				if(hoverExcludedComponent) {return;}
				
				if (event instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)event;
					if(me.getButton() == MouseEvent.BUTTON1) {
						logger.debug(MouseInfo.getPointerInfo().getLocation() + " | ");
						logger.debug(event);
						
						pixelCurrColor = robot.getPixelColor(currentPos.x, currentPos.y);
						
						actionClickThread = new Thread(onClickAction);
						actionClickThread.start();
						
						try {
							actionClickThread.join();
						} catch (InterruptedException e) {
							logger.error(this.getClass(), e);
						}
					}
				} else if (event instanceof FocusEvent) {
					if(!owner.hasFocus.get()) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if(owner instanceof Component) {
									((Component)owner).requestFocus();
									if(owner instanceof JFrame) {
										((JFrame)owner).toFront();
									}
								}
							}
						});
					}
					
					logger.debug(MouseInfo.getPointerInfo().getLocation() + " | ");
					logger.debug(event);
					
					pixelCurrColor = robot.getPixelColor(currentPos.x, currentPos.y);
					
					actionClickThread = new Thread(onClickAction);
					actionClickThread.start();
					
					try {
						actionClickThread.join();
					} catch (InterruptedException e) {
						logger.error(this.getClass(), e);
					}
				} 
//				Thread listenerRemover = new Thread(removeMouseListenerRunnable());
//				listenerRemover.start();
			}
		};
		
		Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
		
		return listener;
	}
	
	public synchronized boolean isEnabled() {
		return (enabled.get());
	}
	
	public synchronized void removeMouseListener() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener);
		enabled.set(false);
	}

	public AtomicBoolean getEnabled() {
		return enabled;
	}

	public void setEnabled(AtomicBoolean enabled) {
		this.enabled = enabled;
	}

	public Runnable removeMouseListenerRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				removeMouseListener();
			}
		};
	}
	
	
	/**
	 * Do nothing - useful as default
	 * @return
	 */
	private Runnable getVoidAction() {
		return new Runnable() {
			@Override
			public void run() {
			}
		};
	}

	public Runnable getOnClickAction() {
		return onClickAction;
	}

	public void setOnClickAction(Runnable onClickAction) {
		this.onClickAction = onClickAction;
	}
	
}
