package cache;

import java.io.File;
import java.util.List;

import cache.impl.FileSearchDTOCache;
import cache.impl.FileSearchResultCache;
import files.om.FileSearchDTO;
import files.search.FileSearchAdvanced;
import files.search.FileSearchAdvanced.SearchType;

public class CacheTest {
	
	private final static String ROOT_FOLDER = "C:\\Users\\xx\\Desktop\\Crypter Test\\Themes";
	private final static String MATCH = "Li";
	private final static long EXPIRY_TIME = 5000;
	
	private static FileSearchResultCache cache =
			new FileSearchResultCache(
				EXPIRY_TIME,
				(input) -> {
					return FileSearchAdvanced.findByMatch((FileSearchDTO)input);
				});
	
	public static void main(String[] args) throws Exception {
		
		cache.setMaxCacheSize(15);
		cache.setDeallocationFactor(0.5f);
		
		new Thread(() ->  {
			for(int i = 0; i < 60; i++) {
				try {
					printList(cache.get(new FileSearchDTOCache(
							new File(ROOT_FOLDER),
							MATCH + i,
							true,
							SearchType.ALL,
							true,
							false)));

					printCacheSize();
					Thread.sleep(400);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
//		new Thread(() ->  {
//			for(int i = 0; i < 60; i++) {
//				try {
//					printList(cache.get(new FileSearchDTOCache(new File(ROOT_FOLDER), MATCH.toLowerCase(), SearchType.ALL, true)));
//					Thread.sleep(1000);
//					printCacheSize();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
		
	}
	
	private static void printCacheSize() {
		System.out.println("Cache size is: " + cache.getCacheSize());
	}
	
	private static void printList(List<File> list) {
		if (list != null) {
			for (File file : list) {
				System.out.println(file);
			} 
		}
	}
	
}
