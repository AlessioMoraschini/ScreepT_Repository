package reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {

	public static List<Field> retrieveAllFields(Class srcClass) {
		List<Field> fields = Arrays.asList(srcClass.getDeclaredFields());
        return fields;
	}
	
	/**
	 * Search all fields in src class, and filter only the ones that are of generic type T
	 * @param srcClass
	 * @param filterType
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static <T,E> List<T> retrieveFieldsByType(E parentObjInstance, Class<T> filterType) throws IllegalArgumentException, IllegalAccessException {
		List<Field> fields = retrieveAllFields(parentObjInstance.getClass());
		List<T> result = new ArrayList<>();
		
		for(Field i_field : fields) {
			i_field.setAccessible(true);
//			boolean testVar = i_field.getType().toString().contains("ComboBox");
			boolean testVar = false; // USE TRUE THIS IN DEBUG ONLY, LIKE EXAMPLE HERE OVER
			if(testVar || filterType.isAssignableFrom(i_field.getType())) {
				T positiveMatch = filterType.cast(i_field.get(parentObjInstance));
				result.add(positiveMatch);
			}
		}
        
        return result;
	}
}
