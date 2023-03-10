package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Navn {
	protected Bruksperiode bruksperiode;
	protected Gyldighetsperiode gyldighetsperiode;
	protected String navnelinje1;
	protected String navnelinje2;
	protected String navnelinje3;
	protected String navnelinje4;
	protected String navnelinje5;
	/**
	 * Sammensatt av navnelinje1, navnelinje2, navnelinje3, navnelinje4 og navnelinje5 av API
	 */
	protected String sammensattnavn;
}
