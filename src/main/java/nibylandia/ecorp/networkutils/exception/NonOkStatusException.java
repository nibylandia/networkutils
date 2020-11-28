package nibylandia.ecorp.networkutils.exception;

public class NonOkStatusException extends Exception {
	private int code;
	
	public NonOkStatusException(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
