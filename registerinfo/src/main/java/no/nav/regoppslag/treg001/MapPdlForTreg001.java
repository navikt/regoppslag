package no.nav.regoppslag.treg001;


import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokmet.Tkat020DokumenttypeInfo;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Component
public class MapPdlForTreg001 {

	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final LandkodeService landkodeService;
	private final Tkat020DokumenttypeInfo tkat020DokumenttypeInfo;
	private final DigitalKontaktinformasjon digitalKontaktinformasjon;
	private final SpraakKodeMapper spraakKodeMapper;
	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;

	private static final String LAND_NORGE = "Norge";

	@Autowired
	public MapPdlForTreg001(PdlGraphQLConsumer pdlGraphQLConsumer, MapPDLResponse mapPDLResponse,
							LandkodeService landkodeService,
							Tkat020DokumenttypeInfo tkat020DokumenttypeInfo,
							DigitalKontaktinformasjon digitalKontaktinformasjon,
							EregConsumer eregConsumer,
							OrganisasjonEregMapper organisasjonEregMapper) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.landkodeService = landkodeService;
		this.tkat020DokumenttypeInfo = tkat020DokumenttypeInfo;
		this.digitalKontaktinformasjon = digitalKontaktinformasjon;
		this.spraakKodeMapper = new SpraakKodeMapper();
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	public Mottaker getMottakerFraPdl(String tema, Mottaker mottaker, String dokumenttypeId) {
		//Skal elementet berikes?
		if (mottaker.isBerik()) {
			if (AktoerType.PERSON.equals(mottaker.getTypeKode())) {
				PdlMottakerInfo hentPerson = mapPDLResponse.mapHentPerson(
						pdlGraphQLConsumer.hentPerson(mottaker.getId(), tema), SERVICE_CODE_TREG001, tema);
				Mottaker mottakerFraPdl = mapAdresseFraPdl(hentPerson);
				mottaker.setKortNavn(isBlank(mottakerFraPdl.getKortNavn()) ? mottakerFraPdl.getNavn() : mottakerFraPdl.getKortNavn());
				mottaker.setNavn(mottakerFraPdl.getNavn());
				mottaker.setMottakeradresse(mottakerFraPdl.getMottakeradresse());
				Spraakkode spraakkode = getSpraakkode(spraakKodeMapper, mottaker, dokumenttypeId, digitalKontaktinformasjon.hentSpraak(mottaker.getId(), false));
				mottaker.setSpraakkode(spraakkode);
			} else {
				Organisasjon organisasjon = eregConsumer.hentOrganisasjon(mottaker.getId());
				MottakerTo mottakerTo = organisasjonEregMapper.map(mottaker.getId(), organisasjon, SERVICE_CODE_TREG001);
				mottaker.setId(mottaker.getId());
				mottaker.setMottakeradresse(mottakerTo.getMottaker().getMottakeradresse());
				mottaker.setKortNavn(mottakerTo.getMottaker().getKortNavn());
				mottaker.setNavn(mottakerTo.getMottaker().getNavn());
				Spraakkode spraakkode = getSpraakkode(spraakKodeMapper, mottaker, dokumenttypeId, mottakerTo.getSpraakKode());
				mottaker.setSpraakkode(spraakkode);
			}
		}

		return mottaker;
	}

	public Mottaker mapAdresseFraPdl(PdlMottakerInfo pdlMottakerInfo) {
		Mottaker mottaker = new Person();

		if (isNull(pdlMottakerInfo.getPostadresse()) || isBlank(pdlMottakerInfo.getPostadresse().getAdresseType())) {
			throw new RegoppslagIllegalArgumentException("Mottakeradresse kan ikke bli null", BAD_REQUEST);
		}
		mottaker.setKortNavn(pdlMottakerInfo.getKortNavn());
		mottaker.setNavn(pdlMottakerInfo.getNavn());

		PostadresseTo postadresse = pdlMottakerInfo.getPostadresse();

		if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_INNLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			NorskPostadresse norskPostadresse = new NorskPostadresse();
			norskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? landkodeService.finnLandnavn(postadresse.getLandkode()) : LAND_NORGE);
			norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			norskPostadresse.setPostnummer(postadresse.getPostnummer());
			norskPostadresse.setPoststed(postadresse.getPoststed());
			mottaker.setMottakeradresse(norskPostadresse);
		} else if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_UTLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
			utenlandskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? landkodeService.finnLandnavn(postadresse.getLandkode()) : null);
			utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}
		return mottaker;
	}

	private Spraakkode getSpraakkode(SpraakKodeMapper spraakKodeMapper, Mottaker mottaker, String dokumenttypeId, String spraak) {
		log.info(format("Henter språkinfo for mottaker. dokumentTypeId=%s", dokumenttypeId));
		//Sjekker språket på malen opp mot mottakers preferanser
		List<SpraakInfoTo> sprakinfos = tkat020DokumenttypeInfo.hentDokumenttypeInfoSpraak(dokumenttypeId);
		if (sprakinfos == null || sprakinfos.isEmpty()) {
			log.warn(format("Finner ikke språkinfo i DOKMET for dokumenttypeid=%s.", dokumenttypeId));
		}
		return spraakKodeMapper.getSpraakKode(mottaker, spraak, sprakinfos);
	}

}
