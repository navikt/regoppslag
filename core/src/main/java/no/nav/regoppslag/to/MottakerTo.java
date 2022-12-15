package no.nav.regoppslag.to;

import lombok.Builder;
import lombok.Data;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.regoppslag.consumer.pdl.to.AdresseKildeCode;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class MottakerTo {
	private AdresseKildeCode adresseKilde;
	private Mottaker mottaker;
	private String spraakKode;
}
