package no.nav.brevbestilling.exceptions;

/**
 * Created by T133804 on 15.08.2017.
 */
public class SelftestTimeoutException extends RuntimeException {
	
	private static final long serialVersionUID = 3511453941981351597L;
	
	public SelftestTimeoutException(String s, Throwable t) {
		super(s, t);
	}
	
}

