package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.util.Date;

@Data
public class Gyldighetsperiode {
	protected Date fom;
	protected Date tom;
}
