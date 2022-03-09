package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class NAVSpesifikkInformasjon {
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
	protected Boolean erIA;
}
