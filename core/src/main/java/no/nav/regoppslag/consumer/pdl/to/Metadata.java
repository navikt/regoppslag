package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;


@Data
@Builder
public class Metadata {
	private String opplysningsId;
	private String master;
	private List<Endring> endringer;

	public boolean isKildePdl() {
		return PDL.name().equalsIgnoreCase(master);
	}

	public boolean isKildeFreg() {
		return FREG.name().equalsIgnoreCase(master);
	}
}
