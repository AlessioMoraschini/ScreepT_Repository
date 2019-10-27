package handlers;

import java.util.Arrays;

import dialogutils.JOptionHelper;
import generic.IParametricRunnable;
import generic.ParametricExceptionRunnable;
import media.Sounds;
import resources.GeneralConfig;
import resources.SoundsConfigurator;

public class AWTExceptionHandler implements Thread.UncaughtExceptionHandler{
	
	public static final String ERROR_FILE_REASONS = " reasons could be that file does not exist or contains errors ";
	public static final String CRITICAL_ERROR_MSG = "An unexpected problem caused a critical error: Closing application. \n ";
	public static final String DIALOG_USER_ERR_INFO = "Send the folder LOGS to analyze the problem if you want to signal this problem. You can find more infos on: " + GeneralConfig.WEBSITE + "\n\n";

	IParametricRunnable doAfterError;
	Thread doAfterErrorThread;
	
	public AWTExceptionHandler(IParametricRunnable doAfterError) {
		this.doAfterError = doAfterError;
		registerExceptionHandler();
	}
	
	public void handle(Throwable t) {
		try {
			if (ParametricExceptionRunnable.isCriticalError(t)) {
				Sounds.playSound(SoundsConfigurator.ERROR, 0.6f);
				new JOptionHelper(null).errorScroll(CRITICAL_ERROR_MSG + DIALOG_USER_ERR_INFO + t.getClass().getName(), "Fatal Error", t);
			}else {
				Sounds.playSound(SoundsConfigurator.SOUND_PLING, 0.6f);
				boolean choice = new JOptionHelper(null).errorScroll("An Exception occurred, but I'still here ;) \n\n => PRESS NO IF YOU WANT TO KILL APPLICATION\n\n =>" + DIALOG_USER_ERR_INFO + t.getClass().getName() +Arrays.deepToString(t.getStackTrace()), "An Exception Occurred");
				if(!choice) {
					System.exit(1);
				}
			}
			doAfterError.parametricRun(t);
			
		} catch (Throwable t1) {
			// don't let the exception get thrown out, will cause infinite looping!
		}
	}

	public void registerExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(this);
	    System.setProperty("sun.awt.exception.handler", AWTExceptionHandler.class.getName());
	  }

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		handle(e);
	}
}
