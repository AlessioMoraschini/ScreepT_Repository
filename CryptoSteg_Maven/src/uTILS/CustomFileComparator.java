package uTILS;

import java.io.File;
import java.util.Comparator;

/**
 * 
 * @author Alessio Moraschini
 * 
 * This class is created to compare files by name using the ordinal number, from 0 to n
 *
 * takes the number from filenames of type "number+separator+asodauuaibibb.extension"
 *
 *	if first letter is not a numeric value then it calculates it as 0
 */
public class CustomFileComparator implements Comparator<File> {

	String nameA,nameB;
	String separator;
	
	public CustomFileComparator(String separ) {
		
		separator = separ;
	}
	
	@Override
	public int compare(File fileA, File fileB) {
		nameA = fileA.getName();
		nameB = fileB.getName();
		
		String[] a = nameA.split(separator);
		String[] b = nameB.split(separator);
		
		Integer A;
		Integer B;
		
		try {
			A = Integer.valueOf(a[0]);
		}catch(NumberFormatException e) {
			A=0;
		}
		try {
			B = Integer.valueOf(b[0]);
		}catch(NumberFormatException e1) {
			B=0;
		}
		
		return A.compareTo(B);
	}

}
