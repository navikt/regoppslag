package no.nav.regoppslag.consumer.personv3.support;

import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToNorskpostadresse;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static no.nav.regoppslag.metrics.PrometheusLabels.ADRESSETYPE;
import static no.nav.regoppslag.metrics.PrometheusLabels.PERSONV3_MAPPER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_LAND;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.PrometheusLabels.UKJENT_POSTSTED;
import static no.nav.regoppslag.metrics.PrometheusMetrics.getConsumerId;
import static no.nav.regoppslag.metrics.PrometheusMetrics.requestCounter;
import static org.apache.commons.lang.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Doedsdato;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseNorge;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Postboksadresse;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.StedsadresseNorge;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Component
@Slf4j
public class PersonV3Mapper {
	public static final String BOSTEDSADRESSE = "BOSTEDSADRESSE";
	public static final String POSTADRESSE = "POSTADRESSE";
	public static final String MIDLERTIDIG_POSTADRESSE_UTLAND = "MIDLERTIDIG_POSTADRESSE_UTLAND";
	public static final String MIDLERTIDIG_POSTADRESSE_NORGE = "MIDLERTIDIG_POSTADRESSE_NORGE";
	public static final String UKJENT_ADRESSE = "UKJENT_ADRESSE";

	@Inject
	private final PostnummerService postnummerService;
	@Inject
	private final LandkodeService landkodeService;

	private static final Pattern pattern = Pattern.compile("(\\d{4})");

	private static final String LAND_NORGE = "Norge";

	public PersonV3Mapper(PostnummerService postnummerService, LandkodeService landkodeService) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
	}

	public String getSakspartNavn(Bruker person) {
		if (person.getPersonnavn().getMellomnavn() == null) {
			return person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn();
		} else {
			return person.getPersonnavn().getFornavn() + " " + person.getPersonnavn()
					.getMellomnavn() + " " + person.getPersonnavn().getEtternavn();
		}
	}

	public Mottaker map(Bruker person, String serviceCode) throws RegOppslagFunctionalException {
		Date now = Date.from(Instant.now());

		if (person.getDoedsdato() != null && now.after(person.getDoedsdato().getDoedsdato().toGregorianCalendar().getTime())) {
			throw new RegOppslagFunctionalException("Personen er registrert som død.");
		}

		Mottaker mottaker = new Person();

		mottaker.setSpraakkode(getSpraakkode(person));
		mottaker.setKortNavn(getMottakerKortNavn(person));
		mottaker.setNavn(getMottakerNavn(person));

		Postadresse postadresse = mapAdresse(person);

		incrementFunctionalMetrics(person, postadresse, serviceCode);

		validateAdresse(person, postadresse, serviceCode);

		if (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand())) {
			NorskPostadresse norskPostadresse = mapPostadresseToNorskpostadresse(postadresse);
			mottaker.setMottakeradresse(norskPostadresse);
		} else {
			UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}

		return mottaker;

	}

	private Spraakkode getSpraakkode(Bruker person) {
		if (person.getMaalform() != null) {
			if ("NO".equalsIgnoreCase(person.getMaalform().getValue())) {
				return Spraakkode.NB;
			} else {
				return Spraakkode.valueOf(person.getMaalform().getValue());
			}
		}
		return null;
	}

	private Postadresse mapAdresse(Bruker person) throws RegOppslagFunctionalException {
		if (person.getGjeldendePostadressetype() != null) {
			if (BOSTEDSADRESSE.equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getBostedsadresse() != null) {
				return mapBostedadresse(person);
			} else if (POSTADRESSE.equals(person.getGjeldendePostadressetype().getValue()) && person.getPostadresse()
					.getUstrukturertAdresse() != null) {
				return mapPostadresse(person);
			} else if (MIDLERTIDIG_POSTADRESSE_UTLAND.equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getMidlertidigPostadresse() != null) {
				return mapMidlertidigUtland(person);
			} else if (MIDLERTIDIG_POSTADRESSE_NORGE.equals(person.getGjeldendePostadressetype()
					.getValue()) && person.getMidlertidigPostadresse() != null) {
				return mapMidlertidigNorge(person);
			}
		}

		return Postadresse.builder().build();
	}

	private String getMottakerKortNavn(Bruker person) {
		return person.getPersonnavn().getSammensattNavn();
	}

	private String getMottakerNavn(Bruker person) {

		if (person.getPersonnavn().getMellomnavn() == null) {
			return person.getPersonnavn().getFornavn() + " " + person.getPersonnavn().getEtternavn();
		} else {
			return person.getPersonnavn().getFornavn() + " " + person.getPersonnavn()
					.getMellomnavn() + " " + person.getPersonnavn().getEtternavn();
		}
	}

	private void incrementFunctionalMetrics(Bruker person, Postadresse postadresse, String serviceCode) {

		requestCounter.labels(serviceCode, PERSONV3_MAPPER, ADRESSETYPE, getConsumerId(), person.getGjeldendePostadressetype() == null ? "Ukjent" : person
				.getGjeldendePostadressetype()
				.getValue()).inc();

		if (isBlank(postadresse.getPostnummer()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_POSTNUMMER, getConsumerId(), UKJENT_POSTNUMMER).inc();
		}

		if (isBlank(postadresse.getPoststed()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_POSTSTED, getConsumerId(), UKJENT_POSTSTED).inc();
		}

		if (postadresse.getLand() == null) {
			requestCounter.labels(serviceCode, PERSONV3_MAPPER, UKJENT_LAND, getConsumerId(), UKJENT_LAND).inc();
		}

	}

	private void validateAdresse(Bruker person, Postadresse postadresse, String serviceCode) throws RegOppslagFunctionalException {

		if (person.getGjeldendePostadressetype()!=null && UKJENT_ADRESSE.equals(person.getGjeldendePostadressetype().getValue())) {
			throw new RegOppslagFunctionalException(serviceCode + " Kunne ikke mappe postadresse for mottaker fordi gjeldendePostadressetype=UKJENT_ADRESSE", "Person har ukjent postadresse");
		}

		if (isBlankPostadresse(postadresse)) {
			throw new RegOppslagFunctionalException(String.format("Ugyldig postadresse. Adresse mangler adresselinje1, postnummer, poststed og land. GjeldenePostadresseType=%s", person
					.getGjeldendePostadressetype() == null ? "Ukjent" : person.getGjeldendePostadressetype()
					.getValue()), "Ugyldig postadresse");
		}
	}

	private boolean isBlankPostadresse(Postadresse postadresse) {
		return isBlank(postadresse.getAdresselinje1()) && isBlank(postadresse.getLand()) && isBlank(postadresse.getPostnummer()) && isBlank(postadresse
				.getPoststed());
	}

	private Postadresse mapBostedadresse(Bruker person) {
		Postadresse postadresse = Postadresse.builder().build();

		if (person.getBostedsadresse().getStrukturertAdresse() instanceof Gateadresse) {
			Gateadresse gateadresse = (Gateadresse) person.getBostedsadresse().getStrukturertAdresse();
			postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
		} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Matrikkeladresse) {
			Matrikkeladresse matrikkeladresse = (Matrikkeladresse) person.getBostedsadresse().getStrukturertAdresse();
			postadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
		} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof Postboksadresse) {
			Postboksadresse postboksadresse = (Postboksadresse) person.getBostedsadresse().getStrukturertAdresse();
			postadresse.setAdresselinje1("Postboks " + postboksadresse.getPostboksnummer());
		}

		if (person.getBostedsadresse().getStrukturertAdresse() instanceof StedsadresseNorge) {
			StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) person.getBostedsadresse().getStrukturertAdresse();
			if (stedsadresseNorge.getPoststed() != null) {
				postadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
				postadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getValue()));
			}
		} else if (person.getBostedsadresse().getStrukturertAdresse() instanceof PostboksadresseNorsk) {
			PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) person.getBostedsadresse()
					.getStrukturertAdresse();
			if (postboksadresseNorsk.getPoststed() != null) {
				postadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
				postadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
			}
		}
		if (person.getBostedsadresse().getStrukturertAdresse().getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(person.getBostedsadresse()
					.getStrukturertAdresse()
					.getLandkode()
					.getValue()));
		}

		return postadresse;
	}

	private Postadresse mapPostadresse(Bruker person) {
		Postadresse postadresse = Postadresse.builder().build();

		postadresse.setAdresselinje1(person.getPostadresse().getUstrukturertAdresse().getAdresselinje1());
		postadresse.setAdresselinje2(person.getPostadresse().getUstrukturertAdresse().getAdresselinje2());
		postadresse.setAdresselinje3(person.getPostadresse().getUstrukturertAdresse().getAdresselinje3());

		if (person.getPostadresse().getUstrukturertAdresse().getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(person.getPostadresse()
					.getUstrukturertAdresse()
					.getLandkode()
					.getValue()));
		}

		String postnummer = getPostnummerFromAdresselinje(person.getPostadresse().getUstrukturertAdresse().getAdresselinje4());
		if (postnummer != null && LAND_NORGE.equals(postadresse.getLand())) {
			postadresse.setPostnummer(postnummer);
			postadresse.setPoststed(postnummerService.finnPoststed(postnummer));
		}
		return postadresse;
	}

	private String getPostnummerFromAdresselinje(String adresselinje) {
		if (adresselinje == null) {
			return null;
		}

		Matcher matcher = pattern.matcher(adresselinje);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return null;
		}
	}

	private Postadresse mapMidlertidigUtland(Bruker person) {
		Postadresse postadresse = Postadresse.builder().build();

		MidlertidigPostadresseUtland midlertidigPostadresseUtland = (MidlertidigPostadresseUtland) person.getMidlertidigPostadresse();
		if (midlertidigPostadresseUtland.getUstrukturertAdresse() != null) {
			postadresse.setAdresselinje1(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje1());
			postadresse.setAdresselinje2(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje2());
			postadresse.setAdresselinje3(midlertidigPostadresseUtland.getUstrukturertAdresse().getAdresselinje3());

			String postnummer = getPostnummerFromAdresselinje(midlertidigPostadresseUtland.getUstrukturertAdresse()
					.getAdresselinje4());
			if (postnummer != null) {
				postadresse.setPostnummer(postnummer);
			}
		}
		if (midlertidigPostadresseUtland.getUstrukturertAdresse().getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(midlertidigPostadresseUtland.getUstrukturertAdresse()
					.getLandkode()
					.getValue()));
		}

		return postadresse;
	}

	private Postadresse mapMidlertidigNorge(Bruker person) {
		Postadresse postadresse = Postadresse.builder().build();

		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Gateadresse) {
			Gateadresse gateadresse = (Gateadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse();
			postadresse.setAdresselinje1(Optional.ofNullable(gateadresse.getGatenavn())
					.orElse("") + " " + Optional.ofNullable(gateadresse.getHusnummer() == null ? null : gateadresse.getHusnummer()
					.toString()).orElse("") + Optional.ofNullable(gateadresse.getHusbokstav()).orElse(""));
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Matrikkeladresse) {
			Matrikkeladresse matrikkeladresse = (Matrikkeladresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			postadresse.setAdresselinje1(matrikkeladresse.getEiendomsnavn());
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof Postboksadresse) {
			Postboksadresse postboksadresse = (Postboksadresse) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			postadresse.setAdresselinje1("Postboks " + postboksadresse.getPostboksnummer());
		}

		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof StedsadresseNorge) {
			StedsadresseNorge stedsadresseNorge = (StedsadresseNorge) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			if (stedsadresseNorge.getPoststed() != null) {
				postadresse.setPostnummer(stedsadresseNorge.getPoststed().getValue());
				postadresse.setPoststed(postnummerService.finnPoststed(stedsadresseNorge.getPoststed().getValue()));
			}
		} else if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse() instanceof PostboksadresseNorsk) {
			PostboksadresseNorsk postboksadresseNorsk = (PostboksadresseNorsk) ((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse();
			if (postboksadresseNorsk.getPoststed() != null) {
				postadresse.setPostnummer(postboksadresseNorsk.getPoststed().getValue());
				postadresse.setPoststed(postnummerService.finnPoststed(postboksadresseNorsk.getPoststed().getValue()));
			}
		}

		if (((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse()).getStrukturertAdresse().getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(((MidlertidigPostadresseNorge) person.getMidlertidigPostadresse())
					.getStrukturertAdresse()
					.getLandkode()
					.getValue()));
		}

		return postadresse;
	}

}