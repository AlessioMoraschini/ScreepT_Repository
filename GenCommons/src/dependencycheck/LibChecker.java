package dependencycheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * This class contains a series of methods to check whenever a given class or method can be instantiated/called,
 * and of course if it is defined. It uses reflection to do so.
 * @author Alessio Moraschini
 *
 */
public class LibChecker {
	
	// mainGUI logger
	static Logger logger = Logger.getLogger(LibChecker.class);	
	static boolean loggerEnabled = true;
	
	/**
	 * check if a class of given tipe can be instantiated, for example givin "java.util.Date"
	 * @param fullClassPath EX."java.util.Date"
	 * @return true if class instantiation is ok, false if not
	 */
	public static<T> boolean checkClassInstantiation(String fullClassPathName, T[] args) {
		try {
			Class<?> clazz = Class.forName(fullClassPathName);
			Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
			Object date = ctor.newInstance();
			return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (ClassNotFoundException e) {
			return exceptionLogRetFalse(e);
		} catch (InstantiationException e) {
			return exceptionLogRetFalse(e);
		} catch (IllegalAccessException e) {
			return exceptionLogRetFalse(e);
		} catch (NoSuchMethodException e) {
			return exceptionLogRetFalse(e);
		} catch (IllegalArgumentException e) {
			return exceptionLogRetFalse(e);
		} catch (InvocationTargetException e) {
			return exceptionLogRetFalse(e);
		}
	}
	
	/**
	 * check if a class of given tipe can be found on classpath, for example givin "java.util.Date"
	 * @param fullClassPath EX."java.util.Date"
	 * @return true if class instantiation is ok, false if not
	 */
	public static boolean checkClassExisting(String fullClassPathName) {
		try {
			Class<?> clazz = Class.forName(fullClassPathName);
			return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (ClassNotFoundException e) {
			return exceptionLogRetFalse(e);
		} 
	}
	
	/**
	 * Check if method of a given class is defined through reflection. If it is and no exception is thrown,
	 * then it returns true, else it returns false. 
	 * This method also writes to log if feature is enabled
	 * 
	 * @param parentClass full path of the class
	 * @param methodName exact name of method to check
	 * @param params the objects representing params for the wanted signature to check
	 * @return true if defined method and class, false in other cases
	 */
	public static <T> boolean checkMethod(String parentClass, String methodName, Class... params) {
		try {
			Class<?> claSS = Class.forName(parentClass);
			claSS.getMethod(methodName, params);
			return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (NoSuchMethodException e) { 
			return exceptionLogRetFalse(e);
		} catch (ClassNotFoundException e) {
			return exceptionLogRetFalse(e);
		}
	}
	

	/**
	 * Check if method of a given class is defined through reflection. If it is and no exception is thrown,
	 * then it returns true, else it returns false. 
	 * This method also writes to log if feature is enabled
	 * 
	 * @param parentClass the given class to check for the method in
	 * @param methodName exact name of method to check
	 * @param params the objects representing params for the wanted signature to check
	 * @return true if defined method and class, false in other cases
	 */
	public static <T> boolean checkMethod(Class<T> parentClass, String methodName, Class... params) {
		try {
			  parentClass.getMethod(methodName, params);
			  return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (NoSuchMethodException e) { 
			return exceptionLogRetFalse(e);
		}
	}
	

	/**
	 * Check if method of a given class is defined through reflection. If it is and no exception is thrown,
	 * then it returns true, else it returns false. 
	 * This method also writes to log if feature is enabled
	 * 
	 * @param parentClass An instance of the given class to check for the method in
	 * @param methodName exact name of method to check
	 * @param params the objects representing params for the wanted signature to check
	 * @return true if defined method and class, false in other cases
	 */
	public static <T> boolean checkMethodNExecute(T parentClassInstance, String methodName, Object... params) {
		try {
			  Method method = parentClassInstance.getClass().getMethod(methodName, params.getClass());
			  method.setAccessible(true);
			  method.invoke(parentClassInstance, params);
			  return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (NoSuchMethodException e) { 
			return exceptionLogRetFalse(e);
		} catch (IllegalAccessException e) {
			return exceptionLogRetFalse(e);
		} catch (IllegalArgumentException e) {
			return exceptionLogRetFalse(e);
		} catch (InvocationTargetException e) {
			return exceptionLogRetFalse(e);
		}
	}
	
	private static boolean exceptionLogRetFalse(Exception e) {
		if (loggerEnabled) {
			e.printStackTrace();
			logger.error(e);
		}
		return false;
	}

	public static boolean isLoggerEnabled() {
		return loggerEnabled;
	}

	public static void setLoggerEnabled(boolean loggerEnabled) {
		LibChecker.loggerEnabled = loggerEnabled;
	}
	
	
}
