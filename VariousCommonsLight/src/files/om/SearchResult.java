package files.om;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import utility.string.StringWorker;

public class SearchResult {

	public File ownerFile;
	
	// Left: line number :: right: list of caret positions of results
	public Map<Integer, ArrayList<Match>> resultLines;
	
	public SearchResult(File ownerFile) {
		this.ownerFile = ownerFile;
		resultLines = new HashMap<>();
	}
	
	
	public void resetResults() {
		resultLines.clear();
	}
	
	public void addMatch(Integer line, int caretMatchStart, int caretMatchEnd) {
		if (resultLines.get(line) != null) {
			resultLines.get(line).add(new Match(caretMatchStart, caretMatchEnd));
		} else {
			ArrayList<Match> item = new ArrayList<>();
			item.add(new Match(caretMatchStart, caretMatchEnd));
			resultLines.put(line, item);
		}
	}
	
	public void reorderByLine() {
		resultLines = new TreeMap<>(resultLines);
	}
	
	public class Match {
		int start;
		int end;
		
		public Match(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "Match [start=" + start + ", end=" + end + "]";
		}
	}

	@Override
	public String toString() {
		return "SearchResult [ownerFile=" + ownerFile + ", resultLines=" + StringWorker.getMapToString(resultLines) + "]";
	}
	
}
