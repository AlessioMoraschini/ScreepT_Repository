package om;

public class SelectionDTO {

	public String text;
	public int start = 0;
	public Integer end = null;
	public String selected;
	
	public SelectionDTO(int start, Integer end) {
		this.start = start;
		this.end = end;
		this.text = "";
	}

	public SelectionDTO(int start, Integer end, String text) {
		this(start, end);
		this.text = text;
	}

	public SelectionDTO(int start, Integer end, String text, String selected) {
		this(start, end, text);
		this.selected = selected;
	}

	@Override
	public String toString() {
		return "SelectionDTO [start=" + start + ", end=" + end + "]";
	}
	
}
