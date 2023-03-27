package no.nav.regoppslag.consumer.norg2.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhetNavn {
	private String enhetNr;
	private String navn;
	private String status;
}
