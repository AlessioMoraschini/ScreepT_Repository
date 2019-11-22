package generic;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import amfileutils.om.FileNamed;


public class LimitedConcurrentList <T>{
	
	private Vector<T> list;
	private int maxSize;
	
	// constructor
	
	public LimitedConcurrentList(int maxSize){
		this.maxSize = maxSize;
		list = new Vector<T>();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	// methods 
	
	public boolean add(T element) {
		boolean maxReached = list.size() > maxSize;
		
		synchronized (list) {
			if (maxReached && !list.isEmpty()) {
				list.remove(0);
			}
			list.add(element);
		}
		return maxReached;
	}

	public boolean addAll(Collection<T> elements) {
		boolean maxReached = list.size() > maxSize;
		
		synchronized (list) {
			for (T element : elements) {
				if (maxReached && !list.isEmpty()) {
					list.remove(0);
				}
				list.add(element);
			}
		}
		return maxReached;
	}

	public boolean addIfNotPresent(Collection<T> elements) {
		boolean added = false;
		
		synchronized (list) {
			for (T element : elements) {
				if (!list.contains(element)) {
					list.add(element);
				}
			}
		}
		return added;
	}

	public void addNreplaceToTopIfPresent(Collection<T> elements) {
		
		synchronized (list) {
			for (T element : elements) {
				if (list.contains(element)) {
					list.remove(element);
					list.add(element);
				} else {
					list.add(element);
				}
					
			}
		}
	}

	public void addNreplaceToTopIfPresent(T element) {
		
		synchronized (list) {
			if (list.contains(element)) {
				list.remove(element);
				list.add(element);
			} else {
				list.add(element);
			}
		}
	}
	
	public boolean addLast(T element) {
		boolean condition = list.size() > maxSize;
		boolean maxReached = (condition) ? true : false;
		
		synchronized (list) {
			if (condition && !list.isEmpty()) {
				list.remove(0);
			}
			list.add(element);
		}
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
		
		synchronized (list) {
			while (list.contains(target)) {
				if (list != null && !list.isEmpty()) {
					list.remove(target);
				}
			}
		}
		return list.size();
	}

	/** removes all occurrences returning new size after deleting
	 * 
	 * @param target the element to remove
	 * @return the size after removal
	 */
	public int removeAllOccurrencesByEquals(T target) {
		Vector<T> toRemove = new Vector<T>();
		synchronized (list) {
			for (T current : list) {
				if (current.equals(target)) {
					toRemove.add(current);
				}
			}
			
			for(T rmi : toRemove) {
				list.remove(rmi);
			}
		}
		return list.size();
	}

	/** Last index of with search based on equals method to find last match
	 * 
	 * @param target the element to remove
	 * @return -1 if not found, last index in the other case
	 */
	public int lastIndexOfByEquals(T target) {
		int lastIndex = -1;
		Iterator<T> iterator = list.iterator();
		int i = 0;
		while(iterator.hasNext()) {
			T currentEl = iterator.next();
			if (currentEl.equals(target)) {
				lastIndex = i;
			}
			i++;
		}
		
		return lastIndex;
	}
	
	public T getLast() {
		if(list != null && !list.isEmpty()) {
			int size = list.size();
			return list.get(size-1);
		}else return null;
	}
	
	/**
	 * Returns list of strings separated by separator. Incase of file list it uses absolutePath
	 * @param separator
	 * @return
	 */
	public String toCharSeparatedString(String separator) {
		if(list == null) {return "";}
		StringBuilder builder = new StringBuilder();
		for(T curr : list) {
			try {
				if (getLast().equals(curr)) {
					separator = "";
					// Avoid last separator after file
				}
				if (curr instanceof File) {
					builder.append(((File) curr).getAbsolutePath()).append(separator);
				} else if (curr instanceof FileNamed) {
					builder.append(((FileNamed) curr).mFile.getAbsolutePath()).append(separator);
				} else {
					builder.append(curr).append(separator);
				} 
			} catch (Exception e) {
			}
		}

		return builder.toString();
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
