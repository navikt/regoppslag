package no.nav.regoppslag.nais.selftest.support;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	private String stackTrace;
	private Result result = Result.OK;
	private Long responseTime;
	private Ping.Type type;

	@JsonProperty("resultText")
	public Result getResult() {
		return result;
	}
	
	public void setResult(Result result) {
		this.result = result;
	}
	
	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}
	
	public Ping.Type getType() {
		return type;
	}
	
	public void setType(Ping.Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "SelftestCheck{" +
				"result=" + result +
				", endpoint='" + endpoint + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
