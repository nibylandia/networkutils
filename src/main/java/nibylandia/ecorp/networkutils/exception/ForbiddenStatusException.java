package nibylandia.ecorp.networkutils.exception;

public class ForbiddenStatusException extends NonOkStatusException {
	public ForbiddenStatusException(int code) {
		super(code);
	}
}
