package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrganisasjonDetaljer {
	protected List<Postadresse> forretningsadresser;
	protected String maalform;
	protected List<Navn> navn;
	protected LocalDate opphoersdato;
	protected List<Postadresse> postadresser;
	protected LocalDateTime registreringsdato;
	protected LocalDate sistEndret;
	protected LocalDate stiftelsesdato;
}
