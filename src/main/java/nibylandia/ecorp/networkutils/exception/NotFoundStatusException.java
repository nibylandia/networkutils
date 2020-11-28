package nibylandia.ecorp.networkutils.exception;

public class NotFoundStatusException extends NonOkStatusException {
	public NotFoundStatusException(int code) {
		super(code);
	}
}
