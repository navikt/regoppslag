package no.nav.regoppslag.nais.selftest.support;

import org.springframework.util.StopWatch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by T133804 on 15.08.2017.
 */
public abstract class AbstractSelftest {
	protected String name;
	protected String description;
	protected String address;
	protected Ping.Type type;
	
	public AbstractSelftest(Ping.Type type, String name, String description) {
		this.type = type;
		this.name = name;
		this.description = description;
	}
	
	public AbstractSelftest(Ping.Type type, String name, String address, String description) {
		this.type = type;
		this.name = name;
		this.address = address;
		this.description = description;
	}
	
	protected abstract void doCheck() throws Exception;
	
	protected Ping.Type getType() {
		return type;
	}
	
	protected String getName() {
		return name;
	}
	
	protected String getAddress() {
		return address;
	}
	
	protected String getDescription() {
		return description;
	}
	
	/**
	 * Override to false if test should return warning on failure
	 */
	protected boolean isVital() {
		return true;
	}
	
	protected boolean canPing() {
		return true;
	}
	
	public SelftestCheck check() {
		Callable<SelftestCheck> callable = () -> {
			SelftestCheck check = new SelftestCheck();
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			if (canPing()) {
				try {
					doCheck();
				} catch (Exception e) {
					@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
					Throwable rootCause = getRootCause(e);
					check.setErrorMessage(rootCause.getMessage());
					check.setStackTrace(getStackTraceAsString(e));
					check.setResult(isVital() ? Result.ERROR : Result.WARNING);
				}
			} else {
				check.setResult(Result.UNPINGABLE);
			}
			stopWatch.stop();
			check.setResponseTime(stopWatch.getTotalTimeMillis());
			check.setDescription(getDescription());
			check.setEndpoint(getName());
			check.setAddress(getAddress());
			check.setType(getType());
			return check;
			
		};
		return TimeLimitedCodeBlock.runWithTimeout(callable, 10, TimeUnit.SECONDS, "check");
	}
	
	private static Throwable getRootCause(Throwable throwable) {
		Throwable cause;
		while ((cause = throwable.getCause()) != null) {
			throwable = cause;
		}
		
		return throwable;
	}
	
	private static String getStackTraceAsString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));//NOSONAR
		return stringWriter.toString();
	}
	
}
