package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Epostadresse {
	protected String adresse;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
