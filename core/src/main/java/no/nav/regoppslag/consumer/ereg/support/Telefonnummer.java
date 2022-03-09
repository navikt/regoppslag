package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Telefonnummer {
	protected String nummer;
	protected String telefontype;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
