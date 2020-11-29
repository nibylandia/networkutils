package nibylandia.ecorp.networkutils.exception;

/**
 * Exception denoting a negative status response.
 */
public class NonOkStatusException extends Exception {
	private static final long serialVersionUID = -8329517710874905356L;
	private int code;
	
	/**
	 * Creates the exception.
	 * @param code The code returned by the server.
	 */
	public NonOkStatusException(int code) {
		this.code = code;
	}

	/**
	 * Returns the code returned by the server.
	 * @return The code returned by the server.
	 */
	public int getCode() {
		return code;
	}
}
