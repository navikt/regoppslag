package no.nav.regoppslag.consumer.ereg.support;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrganisasjonDetaljer {
	protected List<Ansatte> ansatte;
	protected Organisasjon dublettAv;
	protected List<Organisasjon> dubletter;
	protected List<Enhetstype> enhetstyper;
	protected List<Epostadresse> epostadresser;
	protected List<Formaal> formaal;
	protected List<Postadresse> forretningsadresser;
	protected List<Hjemlandregister> hjemlandregistre;
	protected List<Internettadresse> internettadresser;
	protected String maalform;
	protected List<Telefonnummer> mobiltelefonnummer;
	protected List<Naering> naeringer;
	protected List<Navn> navn;
	protected LocalDate opphoersdato;
	protected List<Postadresse> postadresser;
	protected LocalDateTime registreringsdato;
	protected List<MVA> registrertMVA;
	protected LocalDate sistEndret;
	protected List<Status> statuser;
	protected LocalDate stiftelsesdato;
	protected List<Telefonnummer> telefaksnummer;
	protected List<Telefonnummer> telefonnummer;
	protected List<UnderlagtHjemlandLovgivningForetaksform> underlagtHjemlandLovgivningForetaksform;
}
