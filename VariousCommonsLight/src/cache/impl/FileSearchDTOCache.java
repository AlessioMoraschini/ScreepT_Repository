package cache.impl;

import java.io.File;

import cache.arch.IResultCacheInput;
import files.om.FileSearchDTO;
import files.search.FileSearchAdvanced.SearchType;

public class FileSearchDTOCache extends FileSearchDTO implements IResultCacheInput {

	public FileSearchDTOCache(File root, String matchNamePart, boolean exactMatch, SearchType filter, boolean recursiveDeep, boolean ignoreCase) {
		super(root, matchNamePart, exactMatch, filter, recursiveDeep, ignoreCase);
	}

	@Override
	public String getCacheKey() {
		return (root != null ? root.getAbsolutePath() : "") + matchNamePart + exactMatch + filter + recursiveDeep + ignoreCase;
	}
}
