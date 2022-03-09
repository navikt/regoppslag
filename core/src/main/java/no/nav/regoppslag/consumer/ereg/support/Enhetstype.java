package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Enhetstype {
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
	protected String enhetstype;
}
