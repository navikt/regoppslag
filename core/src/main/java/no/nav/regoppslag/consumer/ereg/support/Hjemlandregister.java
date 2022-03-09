package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Hjemlandregister {
	protected String navn1;
	protected String navn2;
	protected String navn3;
	protected Postadresse postadresse;
	protected String registernummer;
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
}
