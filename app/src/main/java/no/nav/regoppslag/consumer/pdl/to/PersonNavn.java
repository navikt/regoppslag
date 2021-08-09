package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonNavn {
	private String fornavn;
	private String mellomnavn;
	private String etternavn;
	private String forkortetNavn;
}
