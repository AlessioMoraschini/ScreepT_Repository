package utility;

import java.util.List;
import java.util.Random;

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
	
	public static <T> T getRandomElement(T[] array, Float probabilityPerc) {
		
		if(array == null || array.length == 0) {
			return null;
		}
			
		if(probabilityPerc == null) {
			probabilityPerc = 100f;
		}
		
		if(probabilityPerc > 100f)
			probabilityPerc = 100f;

		if(probabilityPerc < 0f)
			probabilityPerc = 0f;
		
		int nValues = array.length;
		int progressive = 0;
			
		
		Random random = new Random();
		int randomInt = random.nextInt(100);
		
		for(int i = 0; i < nValues; i++) {
			progressive += probabilityPerc / (float)nValues;
			if(randomInt < progressive) {
				return array[i];
			}
		}
		
		return null;
	}

	public static <T> T getRandomElement(List<T> list, Float probabilityPerc) {
		
		if(list == null || list.isEmpty()) {
			return null;
		}
		
		if(probabilityPerc == null) {
			probabilityPerc = 100f;
		}
		
		if(probabilityPerc > 100f)
			probabilityPerc = 100f;
		
		if(probabilityPerc < 0f)
			probabilityPerc = 0f;
		
		int nValues = list.size();
		int progressive = 0;
		
		
		Random random = new Random();
		int randomInt = random.nextInt(100);
		
		for(int i = 0; i < nValues; i++) {
			progressive += probabilityPerc / (float)nValues;
			if(randomInt < progressive) {
				return list.get(i);
			}
		}
		
		return null;
	}

}
