package utility.time;

public class AmTimer {

	private volatile boolean running;
	private volatile boolean paused;
	private volatile boolean executed;
	private volatile long msRemaining;
	private volatile long msLength;
	private volatile int sleepInterval;
	private Runnable action;
	
	public AmTimer(Runnable action, long msLength) {
		this.running = false;
		this.executed = false;
		this.msRemaining = msLength;
		this.msLength = msLength;
		
		this.action = action;
		
		sleepInterval = (int) msLength / 4;
	}
	
	public void waitAndExecute() {
		if(running || action == null) {
			return;
		}
		
		long startMsTime = System.currentTimeMillis();
		
		new Thread(() -> {
			executed = false;
			running = true;
			
			while (msRemaining > 0) {
				
				try {
					if(!paused) {
						// remaining = total - (elapsed)
						msRemaining = msLength - (System.currentTimeMillis() - startMsTime);
						System.out.println("AmTIMER: Action NOT PAUSED");

					} else {
						System.out.println("AmTIMER: Action PAUSED");
					}
					
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("AmTIMER: Executing Action");
			action.run();
			
			executed = true;
			running = false;
			
		}).start();
	}


	public boolean isExecuted() {
		return executed;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isRunning() {
		return running;
	}

	public long getMsLength() {
		return msLength;
	}

	public void setMsLength(long msLength) {
		this.msLength = msLength;
	}

	public Runnable getAction() {
		return action;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	public long getMsRemaining() {
		return msRemaining;
	}
	
}
