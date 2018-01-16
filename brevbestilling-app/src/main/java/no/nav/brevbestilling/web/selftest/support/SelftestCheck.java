package no.nav.brevbestilling.web.selftest.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by T133804 on 15.08.2017.
 */
public class SelftestCheck {
	
	private String endpoint;
	private String address;
	private String description;
	private String errorMessage;
	private String stackTrace;
	private Result result = Result.OK;
	private Long responseTime;
	private Ping.Type type;
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getStackTrace() {
		return stackTrace;
	}
	
	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	@JsonProperty("resultText")
	public Result getResult() {
		return result;
	}
	
	public void setResult(Result result) {
		this.result = result;
	}
	
	@JsonProperty("result")
	public Integer getResultVal() {
		return result.auraCode;
	}
	
	@JsonIgnore
	public Long getResponseTime() {
		return responseTime;
	}
	
	public void setResponseTime(Long responseTime) {
		this.responseTime = responseTime;
	}
	
	@JsonProperty("responseTime")
	public String getResponseTimeString() {
		return responseTime + " ms";
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
