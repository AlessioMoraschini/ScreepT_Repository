package testUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

/**
 * This class prints output in an easy way to system.out console
 * @author amoraschini
 *
 */
public class Printer {

	// local lazy printer
	public static void print(String S){
		System.out.print(S);
	}
	
	// local lazy printer
	public static void printl(String S){
		System.out.println(S);
	}
	
	// print a line separator
	public static void printSeparator(int n){
		int curr = 0;
		print("\n");
		while (curr < n) {
			print("#");
			curr++;
		}
	}
	
    // pretty print a map
    public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() 
				+ " <=> Value : " + entry.getValue());
        }
    }
    
    // pretty print an array
    public static <K extends Object> void printArray(K[] array){
    	int i = 0;
    	for(K element : array){
    		printl(element.getClass().getTypeName() + "["+i+"]" + element.toString());
    		i++;
    	}
    }
    
    /**
     * Print an array with n elements for each line till the end of array's content
     * @param nForLine
     * @param array
     */
	private <T> void printArrayLines(int nForLine, T[] array) {
		int progr = 0;
		while(progr+nForLine < array.length) {
			printl(Arrays.toString(Arrays.copyOfRange(array, progr, progr+nForLine)));
			progr += nForLine;
		}
		printl(Arrays.toString(Arrays.copyOfRange(array, progr, array.length-1)));
	}
    
	/** 
	 * disable or enable 
	**/
	private boolean printEnabled(boolean DEBUG) {
        if (!DEBUG) {
            System.setOut(new PrintStream(new OutputStream() {
                public void close() {}
                public void flush() {}
                public void write(byte[] b) {}
                public void write(byte[] b, int off, int len) {}
                public void write(int b) {
                }
            }));
        }
        return DEBUG;
	}
	
	/**
	 * Reflection-based to string to get superclass fields
	 */
	public static String reflectionToString(Object object, boolean recursive) {
        if (object == null) return "null";

        Class<?> clazz = object.getClass();
        StringBuilder sb = new StringBuilder(clazz.getSimpleName()).append(" {");

        while (clazz != null && !clazz.equals(Object.class)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    try {
                        f.setAccessible(true);
                        sb.append(f.getName()).append(" = ").append(f.get(object)).append(",");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!recursive) {
                break;
            }
            clazz = clazz.getSuperclass();
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.append("}").toString();
    }
}
