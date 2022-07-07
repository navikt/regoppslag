package no.nav.regoppslag.consumer.digdirkrr;

import lombok.Builder;

import java.util.List;

@Builder
public class PostPersonerRequest {

	public List<String> personidenter;
}
