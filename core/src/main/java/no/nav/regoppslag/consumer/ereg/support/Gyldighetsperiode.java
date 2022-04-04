package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Gyldighetsperiode {
	protected LocalDate fom;
	protected LocalDate tom;
}
