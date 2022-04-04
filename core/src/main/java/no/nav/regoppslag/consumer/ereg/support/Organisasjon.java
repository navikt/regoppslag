package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

@Data
public class Organisasjon {
	protected Navn navn;
	protected OrganisasjonDetaljer organisasjonDetaljer;
	protected String organisasjonsnummer;
	protected String type;
}
