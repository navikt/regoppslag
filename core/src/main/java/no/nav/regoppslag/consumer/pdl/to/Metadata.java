package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Comparator.comparing;
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

	public LocalDateTime getDatoForSisteEndring() {
		if (endringer == null || endringer.isEmpty()) {
			return null;
		}
		return endringer.stream()
				.sorted(comparing(Endring::getRegistrert).reversed()).toList()
				.get(0).getRegistrert();
	}
}
