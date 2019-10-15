package textareautil;

public class SelectionDTO {

	public int start = 0;
	public Integer end = null;
	
	public SelectionDTO(int start, Integer end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public String toString() {
		return "SelectionDTO [start=" + start + ", end=" + end + "]";
	}
	
}
