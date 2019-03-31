package testUtils;

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
	private void printSeparator(int n){
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
}
