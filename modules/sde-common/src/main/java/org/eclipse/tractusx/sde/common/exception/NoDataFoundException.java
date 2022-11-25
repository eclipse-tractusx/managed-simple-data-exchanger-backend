package org.eclipse.tractusx.sde.common.exception;

public class NoDataFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoDataFoundException(String exceptionstr) {
		super(exceptionstr);
	}
}