package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
public class Endring {
	LocalDateTime registrert;
	EndringsType type;

	public enum EndringsType {
		OPPRETT, KORRIGER, OPPHOER, ANNULLER
	}
}
