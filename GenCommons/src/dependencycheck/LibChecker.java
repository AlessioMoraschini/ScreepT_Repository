package dependencycheck;

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
	
	/**
	 * check if a class of given tipe can be instantiated, for example givin "java.util.Date"
	 * @param fullClassPath EX."java.util.Date"
	 * @return true if class instantiation is ok, false if not
	 */
	public static boolean checkClass(String fullClassPathName) {
		try {
			Class<?> clazz = Class.forName(fullClassPathName);
			Object date = clazz.newInstance();
			return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (ClassNotFoundException e) {
			return exceptionLogRetFalse(e);
		} catch (InstantiationException e) {
			return exceptionLogRetFalse(e);
		} catch (IllegalAccessException e) {
			return exceptionLogRetFalse(e);
		}
	}
	
	public static <T> boolean checkMethod(T parentClass, String methodName, Object... params) {
		try {
			  parentClass.getClass().getMethod(methodName, params.getClass());
			  return true;
		} catch (SecurityException e) { 
			return exceptionLogRetFalse(e);
		} catch (NoSuchMethodException e) { 
			return exceptionLogRetFalse(e);
		}
	}
	
	public static <T> boolean checkMethodNExecute(T parentClassInstance, String methodName, Object... params) {
		try {
			  Method method = parentClassInstance.getClass().getMethod(methodName, params.getClass());
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
		logger.error(e);
		return false;
	}
}
