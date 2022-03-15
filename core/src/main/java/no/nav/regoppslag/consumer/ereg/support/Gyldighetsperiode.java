package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class Gyldighetsperiode {
	protected LocalDate fom;
	protected LocalDate tom;
}
