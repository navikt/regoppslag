package no.nav.regoppslag.consumer.ereg.support;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import no.nav.regoppslag.util.DateDeserializer;

import java.util.Date;

@Data
public class Bruksperiode {
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date fom;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date tom;

}
