package generic;

import java.util.Vector;

public class LimitedConcurrentList <T>{
	
	private Vector<T> list;
	private int maxSize;
	
	// constructor
	
	public LimitedConcurrentList(int maxSize){
		this.maxSize = maxSize;
		list = new Vector();
	}

	// methods 
	
	public boolean add(T element) {
		boolean condition = list.size() > maxSize;
		boolean maxReached = (condition) ? true : false;
		
		if(condition && !list.isEmpty()) {
			list.remove(0);
		}
		
		list.add(element);
		
		return maxReached;
	}
	
	public boolean addLast(T element) {
		boolean condition = list.size() > maxSize;
		boolean maxReached = (condition) ? true : false;
		
		if(condition && !list.isEmpty()) {
			list.remove(0);
		}
		
		list.add(element);
		
		return maxReached;
	}
	
	public boolean removeLast() {
		if(list != null && !list.isEmpty()) {
			int size = list.size();
			list.remove(size-1);
			return true;
		}else return false;
	}
	
	/** removes all occurrences returning new size after deleting
	 * 
	 * @param target the element to remove
	 * @return the size after removal
	 */
	public int removeAllOccurrences(T target) {
		
		while (list.contains(target)) {
			if (list != null && !list.isEmpty()) {
				list.remove(target);
			} 
		}
		
		return list.size();
	}
	
	public T getLast() {
		if(list != null && !list.isEmpty()) {
			int size = list.size();
			return list.get(size-1);
		}else return null;
	}

	// getters & setters
	
	public Vector<T> getList() {
		return list;
	}

	public void setList(Vector<T> list) {
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
