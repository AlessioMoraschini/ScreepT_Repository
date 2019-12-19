package util;

import static java.lang.System.out;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
/**
 * This class is meant to provide a safe way to log either to system out or using your log4j logger, using reflection.
 * if no logger is found and safeLogger is enabled then logging messages will be logged. (by default logging is enabled)
 * 
 * @author Alessio Moraschini
 *
 */
public class SafeLogger {
	
	private Object logger = null;
	private boolean loggerAvailable = false;
	
	private static final String loggerClassName = "org.apache.log4j.Logger";
	
	private static final String ERROR = "ERROR: ";
	private static final String WARN = "WARNING: ";
	private static final String INFO = "INFO: ";
	private static final String DEBUG = "DEBUG: ";
	
	private boolean loggerEnabled = true;

	public SafeLogger(Class<?> claSS) {
		reset(claSS);
	}
	
	public void reset(Class<?> claSS) {
		try {
			Class<?> loggerClass = Class.forName(loggerClassName);
			Method instancer = loggerClass.getMethod("getLogger", Class.class);
			instancer.setAccessible(true);
			this.logger = instancer.invoke(logger, claSS);
			
			loggerAvailable = true;
		} catch (Throwable throwable) {
			out.println("Cannot initialize logger!\n" + Arrays.deepToString(throwable.getStackTrace()) + "\n\n");
			loggerAvailable = false;
		}
	}
	
	public void invokeLoggerMethod(String method, String msg) {
		try {
			
			Method instancer = logger.getClass().getMethod(method, Object.class);
			instancer.setAccessible(true);
			instancer.invoke(logger, msg);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			loggerAvailable = false;
		}
	}

	public void invokeLoggerMethod(String method, String msg, Throwable throwable) {
		try {
			
			Method instancer = logger.getClass().getMethod(method, Object.class, Throwable.class);
			instancer.setAccessible(true);
			instancer.invoke(logger, msg, throwable);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			loggerAvailable = false;
		}
	}
	
	public void info(String message) {
		if (loggerAvailable) {
			invokeLoggerMethod("info", message);
		} else {
			out.println(INFO + message);
		}
	}

	public void warn(String message) {
		if (loggerAvailable) {
			invokeLoggerMethod("warn", message);
		} else {
			out.println(WARN + message);
		}
	}
	
	public void warn(String message, Throwable throwable) {
		if (loggerAvailable) {
			invokeLoggerMethod("warn", message, throwable);
		} else {
			out.println(WARN + message + Arrays.deepToString(throwable.getStackTrace()));
		}
	}

	public void debug(String message) {
		if (loggerAvailable) {
			invokeLoggerMethod("debug", message);
		} else {
			out.println(DEBUG + message);
		}
	}

	public void error(String message, Throwable throwable) {
		if (loggerAvailable) {
			invokeLoggerMethod("error", message, throwable);
		} else {
			out.println(ERROR + message + Arrays.deepToString(throwable.getStackTrace()));
		}
	}

	public void error(String message) {
		if (loggerAvailable) {
			invokeLoggerMethod("error", message);
		} else {
			out.println(ERROR + message);
		}
	}

	public boolean isLoggerEnabled() {
		return loggerEnabled;
	}

	public void setLoggerEnabled(boolean loggerEnabled) {
		this.loggerEnabled = loggerEnabled;
	}

	public Object getLogger() {
		return logger;
	}

	public void setLogger(Object logger) {
		this.logger = logger;
	}

	public boolean isLoggerAvailable() {
		return loggerAvailable;
	}

	public void setLoggerAvailable(boolean loggerAvailable) {
		this.loggerAvailable = loggerAvailable;
	}

	public static String getLoggerclassname() {
		return loggerClassName;
	}
	
}
