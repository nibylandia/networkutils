package nibylandia.ecorp.networkutils.exception;

public class MovedStatusException extends NonOkStatusException {
	private final String location;
	public MovedStatusException(int code, String location) {
		super(code);
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
}
