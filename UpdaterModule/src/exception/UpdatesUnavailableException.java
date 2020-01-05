package exception;

public class UpdatesUnavailableException extends Exception {
	private static final long serialVersionUID = 949927883559988397L;

	public UpdatesUnavailableException() {
		super();
	}
	
	public UpdatesUnavailableException(String message) {
		super(message);
	}
}
