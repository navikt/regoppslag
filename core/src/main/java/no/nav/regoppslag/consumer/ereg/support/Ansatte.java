package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Ansatte {
	protected Integer antall;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
