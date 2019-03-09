package uTILS.om;

import java.util.LinkedList;

public class LimitedList <T>{
	
	private LinkedList<T> list;
	private int maxSize;
	
	// constructor
	
	public <T>LimitedList(int maxSize){
		this.maxSize = maxSize;
		list = new LinkedList();
	}

	// methods 
	
	public boolean add(T element) {
		boolean condition = list.size() > maxSize;
		boolean maxReached = (condition) ? true : false;
		
		if(condition && !list.isEmpty()) {
			list.removeFirst();
		}
		
		list.add(element);
		
		return maxReached;
	}

	// getters & setters
	
	public LinkedList<T> getList() {
		return list;
	}

	public void setList(LinkedList<T> list) {
		this.list = list;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	public String toString() {
		return "LimitedVector [list=" + list + ", maxSize=" + maxSize + "]";
	}
	
}
