package utility;

import java.util.List;

public class ArrayHelper {

	public static <T> T getLast(T[] source) {
		if(source == null) return null;
		
		return source[source.length - 1];
	}
	
	public static <T> List<T> removeNulls(List<T> sourceList){
		if(sourceList != null) {
			int i = 0;
			for(T element : sourceList) {
				if(element == null)
					sourceList.remove(i);
				i++;
			}
		}
		
		return sourceList;
	}

}
