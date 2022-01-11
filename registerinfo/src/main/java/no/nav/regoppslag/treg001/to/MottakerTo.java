package no.nav.regoppslag.treg001.to;

import lombok.Builder;
import lombok.Data;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
public class MottakerTo {
	private Mottaker mottaker;
	private String spraakKode;
}
