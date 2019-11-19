package exception;

public class ProgressBarInterruptedException extends RuntimeException{
	private static final long serialVersionUID = -6742493294534645837L;

	public ProgressBarInterruptedException(String msg){
		super(msg);
	}

	public ProgressBarInterruptedException(String msg, Exception exc){
		super(msg);
		this.initCause(exc);
	}

}
