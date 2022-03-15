package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Bruksperiode {
	protected LocalDateTime fom;
	protected LocalDateTime tom;

}
