package no.nav.regoppslag.consumer.personv3.support;

import no.nav.dok.metaforcemal.jaxb2.gen.Mottaker;
import no.nav.dok.metaforcemal.jaxb2.gen.NorskPostadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postboksadresse;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
public class PersonV3Mapper {
	public void map(Bruker person, Mottaker mottaker) {
		//Spraakkode?
		mottaker.setKortNavn(person.getPersonnavn().getSammensattNavn());
		if (person.getPersonnavn().getMellomnavn() == null) {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn());
		} else {
			mottaker.setNavn(person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getMellomnavn() + " " + person.getPersonnavn().getEtternavn());
		}
		NorskPostadresse norskPostadresse = new NorskPostadresse();
		if ( person.getGjeldendePostadressetype() != null && "BOSTEDSADRESSE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getBostedsadresse() != null) {
			if (person.getBostedsadresse().getStrukturertAdresse() instanceof Gateadresse) {
				Gateadresse gateadresse = (Gateadresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Matrikkeladresse) {
				Matrikkeladresse matrikkeladresse = (Matrikkeladresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
			}
			else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Postboksadresse) {
				Postboksadresse postboksadresse = (Postboksadresse) person.getBostedsadresse().getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(postboksadresse.getPostboksnummer());
			}
		} else if ( person.getGjeldendePostadressetype() != null && "POSTADRESSE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getPostadresse().getUstrukturertAdresse() != null) {
			norskPostadresse.setAdresselinje1(person.getPostadresse().getUstrukturertAdresse().getAdresselinje1());
			norskPostadresse.setAdresselinje2(person.getPostadresse().getUstrukturertAdresse().getAdresselinje2());
			norskPostadresse.setAdresselinje3(person.getPostadresse().getUstrukturertAdresse().getAdresselinje3());
		} else if ( person.getGjeldendePostadressetype() != null && "MIDLERTIDIG_POSTADRESSE_UTLAND".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getMidlertidigPostadresse() != null) {
			MidlertidigPostadresseUtland midlertidigPostadresseUtland = (MidlertidigPostadresseUtland) person.getMidlertidigPostadresse();
			if (midlertidigPostadresseUtland.getUstrukturertAdresse() != null) {
				norskPostadresse.setAdresselinje1(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje1());
				norskPostadresse.setAdresselinje2(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje2());
				norskPostadresse.setAdresselinje3(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje3());
			}
		} else if ( person.getGjeldendePostadressetype() != null && "MIDLERTIDIG_POSTADRESSE_NORGE".equals(person.getGjeldendePostadressetype().getKodeverksRef()) && person.getMidlertidigPostadresse() != null) {
			if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Gateadresse) {
				Gateadresse gateadresse = (Gateadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn()).orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer().toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
			} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Matrikkeladresse) {
				Matrikkeladresse matrikkeladresse = (Matrikkeladresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
			} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Postboksadresse) {
				Postboksadresse postboksadresse = (Postboksadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
				norskPostadresse.setAdresselinje1(postboksadresse.getPostboksnummer());
			}
		}
		mottaker.setAdresse(norskPostadresse);
	}
}
