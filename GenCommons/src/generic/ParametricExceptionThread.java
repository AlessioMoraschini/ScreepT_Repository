package generic;

public class ParametricExceptionThread extends Thread implements IParametricRunnable{
	
	
	public ParametricExceptionThread(Runnable runnable) {
		super(runnable);
	}
	
	/**
	 * Run runnable only if this is not a critical exception (an error)
	 */
	@Override
	public void parametricRun(Throwable throwable) {
		this.start();
	}
	
	public static boolean isCriticalError(Throwable t) {
		boolean exception = t instanceof Exception;
		return (!exception);
	}
}
