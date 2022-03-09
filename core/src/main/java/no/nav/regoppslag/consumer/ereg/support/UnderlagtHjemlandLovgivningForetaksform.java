package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class UnderlagtHjemlandLovgivningForetaksform {
	protected String beskrivelseHjemland;
	protected String beskrivelseNorge;
	protected String foretaksform;
	protected String landkode;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
