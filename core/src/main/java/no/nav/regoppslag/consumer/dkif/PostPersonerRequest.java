package no.nav.regoppslag.consumer.dkif;

import lombok.Builder;

import java.util.List;

@Builder
public class PostPersonerRequest {

	public List<String> personidenter;
}
