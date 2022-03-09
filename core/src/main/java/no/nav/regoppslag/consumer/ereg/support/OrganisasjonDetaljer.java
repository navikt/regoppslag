package no.nav.regoppslag.consumer.ereg.support;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import no.nav.regoppslag.util.DateDeserializer;

import java.util.Date;
import java.util.List;

@Data
public class OrganisasjonDetaljer {
	protected List<Ansatte> ansatte;
	protected Organisasjon dublettAv;
	protected List<Organisasjon> dubletter;
	protected List<Enhetstype> enhetstyper;
	protected List<Epostadresse> epostadresser;
	protected List<Formaal> formaal;
	protected List<Forretningsadresse> forretningsadresser;
	protected List<Hjemlandregister> hjemlandregistre;
	protected List<Internettadresse> internettadresser;
	protected String maalform;
	protected List<Telefonnummer> mobiltelefonnummer;
	protected List<Naering> naeringer;
	protected List<Navn> navn;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date opphoersdato;
	protected List<Postadresse> postadresser;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date registreringsdato;
	protected List<MVA> registrertMVA;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date sistEndret;
	protected List<Status> statuser;
	@JsonDeserialize(using = DateDeserializer.class)
	protected Date stiftelsesdato;
	protected List<Telefonnummer> telefaksnummer;
	protected List<Telefonnummer> telefonnummer;
	protected List<UnderlagtHjemlandLovgivningForetaksform> underlagtHjemlandLovgivningForetaksform;
}
