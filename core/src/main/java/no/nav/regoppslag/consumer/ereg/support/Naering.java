package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Naering {
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
	protected Boolean hjelpeenhet;
	protected String naeringskode;
}
