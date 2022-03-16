package no.nav.regoppslag.consumer.ereg.support;

import com.esotericsoftware.minlog.Log;
import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.map.Postadresse;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.metrics.MicrometerMetrics;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.service.PostnummerService;
import no.nav.regoppslag.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SemistrukturertAdresse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToNorskpostadresse;
import static no.nav.regoppslag.consumer.map.PostadresseMapper.mapPostadresseToUtenlandskadresse;
import static no.nav.regoppslag.metrics.MetricLabels.LAND;
import static no.nav.regoppslag.metrics.MetricLabels.ORGANISASJONV4_MAPPER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTNUMMER;
import static no.nav.regoppslag.metrics.MetricLabels.UKJENT_POSTSTED;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class OrganisasjonEregMapper {

	public static final String ADRESSELINJE_1 = "adresselinje1";
	public static final String ADRESSELINJE_2 = "adresselinje2";
	public static final String ADRESSELINJE_3 = "adresselinje3";
	public static final String ADRESSE_3_SPLIT_1 = "Adresse 3 split 1";
	public static final String POSTNR = "postnr";
	public static final String POSTSTED = "poststed";
	public static final String LANDKODE_NORGE = "NO";

	private final LandkodeService landkodeService;
	private final PostnummerService postnummerService;
	private final MicrometerMetrics metrics;

	private static final String LAND_NORGE = "Norge";

	@Inject
	public OrganisasjonEregMapper(PostnummerService postnummerService, LandkodeService landkodeService, MicrometerMetrics metrics) {
		this.landkodeService = landkodeService;
		this.postnummerService = postnummerService;
		this.metrics = metrics;
	}

	public String getSakspartNavn(Organisasjon wsOrganisasjon) {
		OrganisasjonDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();
		Navn organisasjonsnavn = findValidOrgNavn(orgDet)
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + wsOrganisasjon.getOrganisasjonsnummer(), NOT_FOUND));

		return organisasjonsnavn.getNavnelinje1().trim();
		//return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavn()).getNavnelinje(), " ").trim();
	}


	public MottakerTo map(String orgNummer, Organisasjon wsOrganisasjon, String serviceCode) {
		Mottaker mottaker = new no.nav.dok.brevdata.felles.v1.navfelles.Organisasjon();

		OrganisasjonDetaljer orgDet = wsOrganisasjon.getOrganisasjonDetaljer();

		mottaker.setKortNavn(mapOrganisasjonKortnavn(wsOrganisasjon));
		mottaker.setNavn(mapOrganisasjonNavn(wsOrganisasjon));
		no.nav.regoppslag.consumer.map.Postadresse postadresse;
		try {
			postadresse = mapAdresse(orgNummer, orgDet);
		} catch (RegOppslagFunctionalException e) {
			log.info(String.format("Mapping av adresse feilet for orgnummer: %s", wsOrganisasjon.getOrganisasjonsnummer()));
			throw e;
		}

		//todo look at new metrics
		//incrementFunctionalMetrics(postadresse, serviceCode);

		if (LAND_NORGE.equals(postadresse.getLand()) || postadresse.getLand() == null) {
			NorskPostadresse norskPostadresse = mapPostadresseToNorskpostadresse(postadresse);
			mottaker.setMottakeradresse(norskPostadresse);
		} else {
			UtenlandskPostadresse utenlandskPostadresse = mapPostadresseToUtenlandskadresse(postadresse);
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}

		return MottakerTo.builder().mottaker(mottaker).spraakKode(getSpraakKodeAsString(orgDet)).build();
	}

	//todo look at new metrics
	/*
	private void incrementFunctionalMetrics(no.nav.regoppslag.consumer.map.Postadresse postadresse, String serviceCode) {
		if (isBlank(postadresse.getPoststed()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTSTED, UKJENT_POSTSTED);
		}
		if (isBlank(postadresse.getPostnummer()) && (LAND_NORGE.equals(postadresse.getLand()) || isBlank(postadresse.getLand()))) {
			metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, UKJENT_POSTNUMMER, UKJENT_POSTNUMMER);
		}
		metrics.meter(serviceCode, ORGANISASJONV4_MAPPER, LAND, postadresse.getLand() == null ? "Ukjent" : postadresse.getLand());
	}
	 */


	private no.nav.regoppslag.consumer.map.Postadresse mapAdresse(String orgNummer, OrganisasjonDetaljer orgDet) {
		if (orgDet.getOpphoersdato() != null && LocalDate.now().isAfter(orgDet.getOpphoersdato())) {
			String message = String.format("Organisasjon har opphørt, opphørsdato=%s orgnr=%s", new SimpleDateFormat("dd/MM/yyyy").format(orgDet.getOpphoersdato()), orgNummer);
			throw new RegOppslagIkkeFunnetException(message, "Organisasjon har opphørt", NOT_FOUND);
		}

		no.nav.regoppslag.consumer.ereg.support.Postadresse activeAddress = selectActiveAddress(orgDet.getPostadresser(), orgDet.getForretningsadresser())
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige adresser funnet for orgnummer=" + orgNummer, NOT_FOUND));

		no.nav.regoppslag.consumer.map.Postadresse postadresse = no.nav.regoppslag.consumer.map.Postadresse.builder().build();
		//if (activeAddress instanceof SemistrukturertAdresse) {
		//	SemistrukturertAdresse semistrukturertAdresse = (SemistrukturertAdresse) activeAddress;
		//	postadresse = settAdresseledd(semistrukturertAdresse);
		//	if (postadresse.getPostnummer() != null) {
		//		postadresse.setPoststed(postnummerService.finnPoststed(postadresse.getPostnummer()));
		//	}
		//} else {
		//	Gateadresse gateadresse = (Gateadresse) activeAddress;
		//	postadresse.setAdresselinje1(String.format("%s %s%s", gateadresse.getGatenavn(), gateadresse.getHusnummer(), gateadresse.getHusbokstav()));
		//	postadresse.setPostnummer(gateadresse.getPoststed().getKodeRef());
		//	postadresse.setPoststed(postnummerService.finnPoststed(gateadresse.getPoststed().getKodeRef()));
		//}
		//todo fix
		/*
		Log.info(activeAddress.getAdresselinje2());
		postadresse.setPostnummer(activeAddress.getPostnummer());
		if(activeAddress.getPostnummer() != null) {
			postadresse.setPoststed(postnummerService.finnPoststed(activeAddress.getPostnummer()));
		}
		postadresse.setAdresselinje1(activeAddress.getAdresselinje1());
		postadresse.setAdresselinje2(activeAddress.getAdresselinje2());
		postadresse.setAdresselinje3(activeAddress.getAdresselinje3());
		*/
		postadresse = settAdresseledd(activeAddress);


		return postadresse;
	}

	private String getSpraakKodeAsString(OrganisasjonDetaljer orgDet) {

		if (orgDet.getMaalform() != null) {
			return orgDet.getMaalform();
		}
		return null;
	}

	private String mapOrganisasjonKortnavn(Organisasjon wsOrganisasjon) {
		return wsOrganisasjon.getNavn().getNavnelinje1().trim();
		//return StringUtils.collectionToDelimitedString(((UstrukturertNavn) wsOrganisasjon.getNavn()).getNavnelinje(), " ").trim();
	}

	private String mapOrganisasjonNavn(Organisasjon orgDet) {
		Navn organisasjonsnavn = findValidOrgNavn(orgDet.getOrganisasjonDetaljer())
				.orElseThrow(() -> new RegOppslagIkkeFunnetException("Ingen gyldige organisasjonsnavn funnet for orgnummer=" + orgDet.getOrganisasjonsnummer(), NOT_FOUND));

		return organisasjonsnavn.getNavnelinje1().trim();
		//return StringUtils.collectionToDelimitedString(((UstrukturertNavn) organisasjonsnavn.getNavnelinje1()).getNavnelinje(), " ").trim();
	}

	private Optional<Navn> findValidOrgNavn(OrganisasjonDetaljer orgDet) {
		return orgDet.getNavn().stream()
				.filter((org)-> isValidGyldighetsAndBruksPeriode(org.getGyldighetsperiode(), org.getBruksperiode()))
				.findFirst();
	}

	private boolean isValidGyldighetsAndBruksPeriode(Gyldighetsperiode gyldighetsperiode, Bruksperiode bruksperiode) {
		final LocalDateTime nowTime = LocalDateTime.now();
		final LocalDate nowDate = LocalDate.now();

		LocalDate fomGyldig = gyldighetsperiode.getFom();
		LocalDate tomGyldig = gyldighetsperiode.getTom() == null ? null : gyldighetsperiode.getTom();
		LocalDateTime tomBruk = bruksperiode.getTom() == null ? null : bruksperiode.getTom();

		return fomGyldig.isBefore(nowDate)
				&& (tomGyldig == null || tomGyldig.isAfter(nowDate))
				&& (tomBruk == null || tomBruk.isAfter(nowTime));
	}

	// Postadresse skal overstyre forretningsadresse dersom den finnes
	private Optional<no.nav.regoppslag.consumer.ereg.support.Postadresse> selectActiveAddress(List<no.nav.regoppslag.consumer.ereg.support.Postadresse> postadresse, List<no.nav.regoppslag.consumer.ereg.support.Postadresse> forretningsadresse) {
		// Stream.of er basert på array så rekkefølgen er ordered, gyldige postadresse vil bli funnet før forretningsadresse
		return Stream.of(
						selectGyldigPostAdresse(postadresse), selectGyldigPostAdresse(forretningsadresse))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private Optional<no.nav.regoppslag.consumer.ereg.support.Postadresse> selectGyldigPostAdresse(List<no.nav.regoppslag.consumer.ereg.support.Postadresse> adresser) {
		if(adresser == null){
			return Optional.empty();
		}
		return adresser.stream().filter(this::isValidPostAdresse).findAny();
	}

	private boolean isValidPostAdresse(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		boolean isValidGeografiskAdresse = isValidGyldighetsAndBruksPeriode(adresse.getGyldighetsperiode(), adresse.getBruksperiode()) && landkodeIsNotNull(adresse);
		if (isValidGeografiskAdresse && landkodeIsNorge(adresse)) {
			isValidGeografiskAdresse = containsPostnummer(adresse);
		}
		return isValidGeografiskAdresse;
	}

	private boolean landkodeIsNorge(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		return adresse.getLandkode().equals(LANDKODE_NORGE);
	}

	private boolean landkodeIsNotNull(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {
		return adresse.getLandkode() != null && adresse.getLandkode() != null;
	}

	private boolean containsPostnummer(no.nav.regoppslag.consumer.ereg.support.Postadresse adresse) {

		if(adresse.getPostnummer() != null) {
			return true;
		}else if(adresse.getPoststed() != null){
			return true;
		}else{
			return false;
		}
		/*
		if (adresse instanceof SemistrukturertAdresse) {
			return ((SemistrukturertAdresse) adresse).getAdresseledd()
					.stream()
					.anyMatch(nva -> POSTNR.equals(nva.getNoekkel().getKodeRef()) && isNotEmpty(nva.getVerdi()));
		} else if (adresse instanceof Gateadresse) {
			return ((Gateadresse) adresse).getPoststed() != null;
		} else {
			return false;
		}
		*/
	}

	//todo se på dette

	private no.nav.regoppslag.consumer.map.Postadresse settAdresseledd(no.nav.regoppslag.consumer.ereg.support.Postadresse eregAdresse) {
		no.nav.regoppslag.consumer.map.Postadresse postadresse = Postadresse.builder().build();

		if (eregAdresse.getLandkode().equals(LANDKODE_NORGE)) {
			postadresse.setPostnummer(eregAdresse.getPostnummer());
			postadresse.setPoststed(eregAdresse.getPoststed());
			if(eregAdresse.getPostnummer() != null && eregAdresse.getPoststed() == null) {
				postadresse.setPoststed(postnummerService.finnPoststed(eregAdresse.getPostnummer()));
			}
			postadresse.setAdresselinje1(eregAdresse.getAdresselinje1());
			postadresse.setAdresselinje2(eregAdresse.getAdresselinje2());
			postadresse.setAdresselinje3(eregAdresse.getAdresselinje3());
		} else {
			postadresse.setPostnummer(eregAdresse.getPostnummer());
			postadresse.setPoststed(eregAdresse.getPoststed());
			postadresse.setAdresselinje1(eregAdresse.getAdresselinje1());
			postadresse.setAdresselinje2(eregAdresse.getAdresselinje2());
			postadresse.setAdresselinje3(eregAdresse.getAdresselinje3());
		}

		if (eregAdresse.getLandkode() != null) {
			postadresse.setLand(landkodeService.finnLandnavn(eregAdresse.getLandkode()));
		}
		/*
		semistrukturertAdresse.getAdresseledd().forEach(nokkel -> {
			if (ADRESSELINJE_1.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje1(nokkel.getVerdi());
			} else if (ADRESSELINJE_2.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje2(nokkel.getVerdi());
			} else if (ADRESSELINJE_3.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje3(nokkel.getVerdi());
			} else if (ADRESSE_3_SPLIT_1.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setAdresselinje3(nokkel.getVerdi());
			} else if (POSTNR.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setPostnummer(nokkel.getVerdi());
			} else if (POSTSTED.equals(nokkel.getNoekkel().getKodeRef())) {
				postadresse.setPoststed(nokkel.getVerdi());
			}
		});
		 */
		return postadresse;
	}
}
