package amfileutils;

import java.io.File;
import java.io.FileFilter;

public class CustomFileFilters {

	public static final String ALL_FOUND = "ALL";
	public static final String ALL_FOUND_HIDDEN = "ALLH";
	public static final String FILE_ONLY = "FILE_ONLY";
	public static final String FILE_ONLY_HIDDEN = "FILE_ONLYH";
	public static final String DIR_ONLY = "DIR_ONLY";
	public static final String DIR_ONLY_HIDDEN = "DIR_ONLYH";
	
	public static FileFilter customTypeFilter(String filterType) {
		return new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				
				// first check if file exists
				if(!pathname.exists()) {
					return false;
				}
				
				// now filter for type choosen
				if(filterType.equals(ALL_FOUND)) {
					return true;
				}else if(filterType.equals(ALL_FOUND_HIDDEN) && pathname.isHidden()){
					return true;
				}else if(filterType.equals(FILE_ONLY) && pathname.isFile()) {
					return true;
				}else if(filterType.equals(FILE_ONLY_HIDDEN) && pathname.isFile() && pathname.isHidden()) {
					return true;
				}else if(filterType.equals(DIR_ONLY) && pathname.isDirectory()) {
					return true;
				}else if(filterType.equals(DIR_ONLY_HIDDEN) && pathname.isDirectory() && pathname.isHidden()) {
					return true;
				}else {
					// IF NO MATCH FOUND
					return false;
				}
			}
		};
	}
}
