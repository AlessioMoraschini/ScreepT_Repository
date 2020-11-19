package stackoverflow;

import java.util.ArrayList;
import java.util.List;

public class RecursiveOverflow {
	
	List<Object> list;

	public void heapOverflow() {
		list = new ArrayList<Object>();
		for(int i = 0; i < 50; i++) {
			RecursiveOverflow a = new RecursiveOverflow();
			list.add(a);
			a.heapOverflow();
		}
	}
	
	public static void stackOverflow(int a) {
		stackOverflow(a + 1);
	}
	

	public static void main(String[] args) {
		new RecursiveOverflow().stackOverflow(1);
	}
}
