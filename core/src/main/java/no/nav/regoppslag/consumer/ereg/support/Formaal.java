package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Formaal {
	protected String formaal;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
