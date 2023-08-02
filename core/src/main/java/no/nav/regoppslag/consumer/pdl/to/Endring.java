package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Endring {
	LocalDateTime registrert;
	EndringsType type;

	public enum EndringsType {
		OPPRETT, KORRIGER, OPPHOER, ANNULLER
	}
}
