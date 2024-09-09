package no.nav.regoppslag.treg002;

import lombok.extern.slf4j.Slf4j;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import no.nav.regoppslag.exceptions.RegOppslagIkkeFunnetException;
import no.nav.regoppslag.exceptions.RegOppslagSecurityException;
import no.nav.regoppslag.exceptions.RegOppslagTechnicalException;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.exceptions.UkjentAdressePersonErDoedException;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.rreg003.AdresseMapper;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.treg002.Treg002AdresseMapper.mapAdresseTilTreg002Adresse;
import static no.nav.regoppslag.util.DomainConstants.SERVICE_CODE_TREG002;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@Slf4j
public class HentMottakerOgAdresseService {

	private final AdresseMapper adresseMapper;
	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;

	public static final String TREG002_FUNK_FEIL = "TREG002 Funksjonell feil: {}";

	public HentMottakerOgAdresseService(AdresseMapper adresseMapper,
										PdlGraphQLConsumer pdlGraphQLConsumer,
										MapPDLResponse mapPDLResponse,
										EregConsumer eregConsumer,
										OrganisasjonEregMapper organisasjonEregMapper) {
		this.adresseMapper = adresseMapper;
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	public HentMottakerOgAdresseResponse hentMottakerOgAdresseInfo(HentMottakerOgAdresseRequest request) throws RegOppslagSecurityException {
		try {
			return PERSON.name().equals(request.getType()) ? hentMottakerOgAdresseForPerson(request) : hentMottakerOgAdresseForOrg(request);
		} catch (Exception e) {
			logAndRethrowException(e);
		}

		return null;
	}

	private HentMottakerOgAdresseResponse hentMottakerOgAdresseForPerson(HentMottakerOgAdresseRequest request) {
		var person = pdlGraphQLConsumer.hentPerson(request.getIdentifikator());

		PdlMottakerInfo pdlMottakerInfo = mapPDLResponse.mapHentPerson(person, SERVICE_CODE_TREG002);

		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(pdlMottakerInfo.getNavn())
				.adresse(mapAdresseTilTreg002Adresse(adresseMapper.mapFraPdl(pdlMottakerInfo)))
				.build();
	}

	private HentMottakerOgAdresseResponse hentMottakerOgAdresseForOrg(HentMottakerOgAdresseRequest request) {
		Organisasjon organisasjon = eregConsumer.hentOrganisasjon(request.getIdentifikator());
		MottakerTo mottakerTo = organisasjonEregMapper.map(request.getIdentifikator(), organisasjon, SERVICE_CODE_TREG002);

		return HentMottakerOgAdresseResponse.builder()
				.identifikator(request.getIdentifikator())
				.navn(mottakerTo.getMottaker().getNavn())
				.adresse(mapAdresseTilTreg002Adresse(adresseMapper.map(mottakerTo)))
				.build();
	}

	private void logAndRethrowException(Exception e) throws RegOppslagSecurityException {
		if (e instanceof UkjentAdressePersonErDoedException) {
			log.info("TREG002: {}", e.getMessage());
			throw (UkjentAdressePersonErDoedException) e;
		} else if (e instanceof RegOppslagSecurityException) {
			log.warn(TREG002_FUNK_FEIL, e.getMessage());
			throw (RegOppslagSecurityException) e;
		} else if (e instanceof RegOppslagFunctionalException funErr) {
			log.warn(TREG002_FUNK_FEIL, funErr.getMessage());
			if (NOT_FOUND.equals(funErr.getHttpStatusCode())) {
				throw new RegOppslagIkkeFunnetException(e.getLocalizedMessage(), e, funErr.getHttpStatusCode());
			}
			throw new RegoppslagIllegalArgumentException(e.getLocalizedMessage(), e, funErr.getHttpStatusCode());
		} else if (e instanceof UkjentAdresseException err) {
			log.warn(TREG002_FUNK_FEIL, err.getMessage());
			throw err;
		} else {
			log.error("TREG002 Teknisk feil: {}", e.getMessage(), e);
			throw new RegOppslagTechnicalException(format("Teknisk feil: feilmelding=%s", e.getMessage()), e);
		}
	}
}
