package mouseutils;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
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
	
	private volatile boolean ListenerRemoved = false;
	
	public MousePointerUtils(Runnable onClickAction, MousePointerCaller owner) {
		this.onClickAction = (onClickAction != null)? onClickAction : getVoidAction();
		this.owner = owner;
		
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

	public AWTEventListener addMouseListener() {
		ListenerRemoved = false;
		AWTEventListener listener = new AWTEventListener() {
			
			@Override
			public void eventDispatched(AWTEvent event) {
				
				if (event instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)event;
					if(me.getButton() == MouseEvent.BUTTON1) {
						logger.debug(MouseInfo.getPointerInfo().getLocation() + " | ");
						logger.debug(event);
						
						Point currentPos = MouseInfo.getPointerInfo().getLocation();
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
					
					Point currentPos = MouseInfo.getPointerInfo().getLocation();
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
	
	public boolean isListenerRemoved() {
		return (ListenerRemoved);
	}
	
	public void removeMouseListener() {
		ListenerRemoved = true;
		Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener);
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
