package nibylandia.ecorp.networkutils.exception;

/**
 * Exception denoting a not found status response.
 */
public class NotFoundStatusException extends NonOkStatusException {
	private static final long serialVersionUID = -1431483326670884649L;

	/**
	 * Creates the exception.
	 * @param code The code returned by the server.
	 */
	public NotFoundStatusException(int code) {
		super(code);
	}
}
