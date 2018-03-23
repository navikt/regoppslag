package no.nav.regoppslag.nais.selftest.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by T133804 on 15.08.2017.
 */
public final class SelftestResponse {
	
	private String application;
	private String version;
	private String node;
	private LocalDateTime timestamp = LocalDateTime.now();
	private List<SelftestCheck> checks = new ArrayList<>();
	
	public String getApplication() {
		return application;
	}
	
	public void setApplication(String application) {
		this.application = application;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getNode() {
		return node;
	}
	
	public void setNode(String node) {
		this.node = node;
	}
	
	@JsonIgnore
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	
	@JsonProperty("timestamp")
	public String getTimestampText() {
		return timestamp.toString();
	}
	
	@JsonProperty("aggregateResultText")
	public Result getAggregateResult() {
		Result result = Result.OK;
		for (SelftestCheck check : checks) {
			result = result.ordinal() < check.getResult().ordinal() ? check.getResult() : result;
		}
		return result;
	}
	
	@JsonProperty("aggregateResult")
	public Integer getAggregateResultVal() {
		return getAggregateResult().auraCode;
	}
	
	public List<SelftestCheck> getChecks() {
		return ImmutableList.copyOf(this.checks);
	}
	
	public String getAggregateResponseTime() {
		long time = 0;
		for (SelftestCheck check : checks) {
			time += check.getResponseTime();
		}
		return time + " ms";
	}
	
	@Override
	public String toString() {
		return "SelftestResponse{" +
				"result=" + getAggregateResult() +
				", checks=" + checks +
				'}';
	}
	
	public boolean isError() {
		return getAggregateResult() == Result.ERROR;
	}
	
	public void addCheck(SelftestCheck check) {
		this.checks.add(check);
	}
	
}



