package ca.digitalcave.moss.jsp.photos.exception;

public class UnauthorizedException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnauthorizedException(String message) {
		super(message);
	}
}