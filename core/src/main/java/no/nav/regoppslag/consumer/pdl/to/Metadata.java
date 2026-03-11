package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Comparator.comparing;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.FREG;
import static no.nav.regoppslag.consumer.pdl.to.InformasjonKilde.PDL;


@Value
@Builder
@AllArgsConstructor
public class Metadata {
	String opplysningsId;
	String master;
	List<Endring> endringer;

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
				.getFirst().getRegistrert();
	}
}
