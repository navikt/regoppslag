package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Status {
	protected String kode;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
