/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package utility.log;

import static java.lang.System.out;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import om.ConsoleColours;
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
	
	private boolean loggerEnabled;

	public SafeLogger(Class<?> claSS) {
		this(claSS, true);
	}

	public SafeLogger(Class<?> claSS, boolean enabled) {
		reset(claSS);
		loggerEnabled = enabled;
	}
	
	public void reset(Class<?> claSS) {
		try {
			Class<?> loggerClass = Class.forName(loggerClassName);
			Method instancer = loggerClass.getMethod("getLogger", Class.class);
			instancer.setAccessible(true);
			this.logger = instancer.invoke(logger, claSS);
			
			loggerAvailable = true;
		} catch (Throwable throwable) {
			out.println("Cannot initialize logger!");
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
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("info", message);
			} else {
				out.println(INFO + message);
			} 
		}
	}

	public void warn(String message) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("warn", message);
			} else {
				out.println(WARN + message);
			} 
		}
	}
	
	public void warn(String message, Throwable throwable) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("warn", message, throwable);
			} else {
				out.println(
						ConsoleColours.ANSI_RED + WARN + message + throwable.getClass().getName() + getLinedThrowable(throwable, ConsoleColours.ANSI_YELLOW));
			} 
		}
	}

	public void debug(Object obj) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("debug", obj.toString());
			} else {
				out.println(DEBUG + obj.toString());
			} 
		}
	}

	public void debug(String message) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("debug", message);
			} else {
				out.println(DEBUG + message);
			} 
		}
	}

	public void error(String message, Throwable throwable) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("error", message, throwable);
			} else {
				out.println(
						ConsoleColours.ANSI_RED + ERROR + message + throwable.getClass().getName() + getLinedThrowable(throwable, ConsoleColours.ANSI_YELLOW));
			} 
		}
	}

	public void error(String message) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("error", message);
			} else {
				out.println(ERROR + message);
			} 
		}
	}

	public void error(Throwable throwable) {
		if (loggerEnabled) {
			if (loggerAvailable) {
				invokeLoggerMethod("error", throwable.getClass().getCanonicalName(), throwable);
			} else {
				out.println(ERROR + throwable.getClass());
			} 
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
	
	private String getLinedThrowable(Throwable e, String consoleColour) {
		StringBuilder builder = new StringBuilder();
		String colourPrefix = consoleColour != null? consoleColour : "";
		
		builder.append(colourPrefix);
		for(StackTraceElement element : e.getStackTrace()) {
			builder.append("\n\t")
				.append(element.getClassName())
				.append(" : ").append(element.getMethodName())
				.append(" : Line N°").append(element.getLineNumber())
				.append(" : File=").append(element.getFileName());
		}
		builder.append(ConsoleColours.ANSI_RESET);
		
		
		return builder.toString();
	}
	
}
