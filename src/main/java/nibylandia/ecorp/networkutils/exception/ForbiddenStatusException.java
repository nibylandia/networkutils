package nibylandia.ecorp.networkutils.exception;

/**
 * Exception denoting a forbidden status response.
 */
public class ForbiddenStatusException extends NonOkStatusException {
	private static final long serialVersionUID = 3083974162434715808L;

	/**
	 * Creates the exception.
	 * @param code The code returned by the server.
	 */
	public ForbiddenStatusException(int code) {
		super(code);
	}
}
