package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder
public class Endring {

	public enum endringsType {
		OPPRETT, KORRIGER, OPPHOER, ANNULLER
	}

	@Getter
	LocalDateTime registrert;

	@Getter
	endringsType type;
}
