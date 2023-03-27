package no.nav.regoppslag.consumer.ereg;

import lombok.Builder;
import lombok.Data;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;

@Data
@Builder
public class MottakerTo {
	private AdresseKildeCode adresseKilde;
	private Mottaker mottaker;
	private String spraakKode;
}
