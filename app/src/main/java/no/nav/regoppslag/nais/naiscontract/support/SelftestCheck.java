package no.nav.regoppslag.nais.naiscontract.support;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by T133804 on 15.08.2017.
 */
@Getter
@Setter
public class SelftestCheck {
	
	private String endpoint;
	private String address;
	private String description;
	private String errorMessage;
	private String name;
	private String stackTrace;
	private Result result = Result.OK;
	private Long responseTime;
	private Ping.Type type;
	
	@Override
	public String toString() {
		return "SelftestCheck{" +
				"result=" + result +
				", endpoint='" + endpoint + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
