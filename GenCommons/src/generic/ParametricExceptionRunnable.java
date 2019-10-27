package generic;

public class ParametricExceptionRunnable extends Thread implements IParametricRunnable{
	
	
	public ParametricExceptionRunnable(Runnable runnable) {
		super(runnable);
	}
	
	/**
	 * Run runnable only if this is not a critical exception (an error)
	 */
	@Override
	public void parametricRun(Throwable throwable) {
		if (isCriticalError(throwable)) {
			this.start();
		}
	}
	
	public static boolean isCriticalError(Throwable t) {
		boolean exception = t instanceof Exception;
		boolean classDefNotF = t instanceof NoClassDefFoundError;
		return (!exception && !classDefNotF);
	}
}
