package utility.time;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Benchmarker {

	private Long startTime;
	private Long endTime;
	private boolean concurrent;
	
	private Map<Long, TimeRecord> records;
	
	public Benchmarker(boolean concurrent) {
		reset(concurrent);
	}
	
	public void start() {
		reset(concurrent);
	}
	
	/**
	 * Set start time as current nanosec time
	 */
	private void updateStart(){
		synchronized (startTime) {
			startTime = System.nanoTime();
		}
	}
	
	/**
	 * Set end time as current nanosec time
	 */
	private void updateEnd(){
		synchronized (endTime) {
			endTime = System.nanoTime();
		}
	}
	
	/**
	 * Insert an event with start/end/elapsed information and the given message
	 * @param message
	 * @return
	 */
	public TimeRecord registerEvent(String message) {
		TimeRecord record = null;
		
		synchronized (endTime) {
			long elapsed = getElapsed(false);
			record = new TimeRecord(startTime, endTime, elapsed, message);
			records.put(endTime, record);
		}
		return record;
	}
	
	/**
	 * Get elapsed time in nanoseconds or milliseconds
	 * @param millisec if true return millisec, else return nanosec
	 * @return
	 */
	public long getElapsed(boolean millisec){
		updateEnd();
		int divisor = (millisec)? 1000000 : 1;
		return (endTime-startTime)/divisor;
	}
	
	public void reset(boolean concurrent) {
		updateStart();
		updateEnd();
		records = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
	}

	
	public class TimeRecord {
		
		private long startTime;
		private long endTime;
		private long elapsed;
		private String message;
		
		public TimeRecord(long startTime, long endTime, long elapsed, String message) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.elapsed = elapsed;
			this.message = message;
		}

		@Override
		public String toString() {
			return "TimeRecord [startTime=" + startTime + ", endTime=" + endTime + ", elapsed=" + elapsed + ", message=" + message + "]";
		}
	}
}

