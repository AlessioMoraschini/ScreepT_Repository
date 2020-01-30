package utility;

public class ArrayHelper {

	public static <T> T getLast(T[] source) {
		if(source == null) return null;
		
		return source[source.length - 1];
	}
}
