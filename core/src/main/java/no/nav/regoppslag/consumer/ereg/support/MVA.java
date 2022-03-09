package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class MVA {
	protected Boolean registrertIMVA;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
