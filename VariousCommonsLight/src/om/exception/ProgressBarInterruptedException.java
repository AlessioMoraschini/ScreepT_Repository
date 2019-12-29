package om.exception;

public class ProgressBarInterruptedException extends Exception{
	private static final long serialVersionUID = -6742493294534645837L;

	public ProgressBarInterruptedException(String msg){
		super(msg);
	}

	public ProgressBarInterruptedException(String msg, Exception exc){
		super(msg);
		this.initCause(exc);
	}

}
