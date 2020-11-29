package nibylandia.ecorp.networkutils.exception;

/**
 * Exception denoting a moved status response.
 */
public class MovedStatusException extends NonOkStatusException {
	private static final long serialVersionUID = 4950769670430683460L;
	private final String location;
	
	/**
	 * Creates the exception.
	 * @param code The code returned by the server.
	 * @param location Target location as reported by the server.
	 */
	public MovedStatusException(int code, String location) {
		super(code);
		this.location = location;
	}
	
	/**
	 * Returns the target location as reported by the server.
	 * @return The target location as reported by the server.
	 */
	public String getLocation() {
		return location;
	}
}
