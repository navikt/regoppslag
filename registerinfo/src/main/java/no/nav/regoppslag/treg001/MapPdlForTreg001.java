package no.nav.regoppslag.treg001;


import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.consumer.digdirkrr.DigitalKontaktinformasjon;
import no.nav.regoppslag.consumer.dokmet.DokmetConsumer;
import no.nav.regoppslag.consumer.ereg.EregConsumer;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.ereg.support.Organisasjon;
import no.nav.regoppslag.consumer.ereg.support.OrganisasjonEregMapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.pdl.MapPDLResponse;
import no.nav.regoppslag.treg001.support.SpraakKodeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.isNull;
import static no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType.PERSON;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.service.LandkodeService.finnLandnavn;
import static no.nav.regoppslag.util.DomainConstants.SERVICE_CODE_TREG001;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Component
public class MapPdlForTreg001 {

	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final DokmetConsumer dokmetConsumer;
	private final DigitalKontaktinformasjon digitalKontaktinformasjon;
	private final SpraakKodeMapper spraakKodeMapper;
	private final EregConsumer eregConsumer;
	private final OrganisasjonEregMapper organisasjonEregMapper;

	private static final String LAND_NORGE = "Norge";

	public MapPdlForTreg001(PdlGraphQLConsumer pdlGraphQLConsumer, MapPDLResponse mapPDLResponse,
							DokmetConsumer dokmetConsumer,
							DigitalKontaktinformasjon digitalKontaktinformasjon,
							EregConsumer eregConsumer,
							OrganisasjonEregMapper organisasjonEregMapper) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.dokmetConsumer = dokmetConsumer;
		this.digitalKontaktinformasjon = digitalKontaktinformasjon;
		this.spraakKodeMapper = new SpraakKodeMapper();
		this.eregConsumer = eregConsumer;
		this.organisasjonEregMapper = organisasjonEregMapper;
	}

	public Mottaker getMottakerFraPdl(Mottaker mottaker, String dokumenttypeId) {
		//Skal elementet berikes?
		if (mottaker.isBerik()) {
			if (PERSON.equals(mottaker.getTypeKode())) {
				var person = pdlGraphQLConsumer.hentPerson(mottaker.getId());
				PdlMottakerInfo hentPerson = mapPDLResponse.mapHentPerson(person, SERVICE_CODE_TREG001);

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
			norskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? finnLandnavn(postadresse.getLandkode()) : LAND_NORGE);
			norskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			norskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			norskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			norskPostadresse.setPostnummer(postadresse.getPostnummer());
			norskPostadresse.setPoststed(postadresse.getPoststed());
			mottaker.setMottakeradresse(norskPostadresse);
		} else if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_UTLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			UtenlandskPostadresse utenlandskPostadresse = new UtenlandskPostadresse();
			utenlandskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? finnLandnavn(postadresse.getLandkode()) : null);
			utenlandskPostadresse.setAdresselinje1(postadresse.getAdresselinje1());
			utenlandskPostadresse.setAdresselinje2(postadresse.getAdresselinje2());
			utenlandskPostadresse.setAdresselinje3(postadresse.getAdresselinje3());
			mottaker.setMottakeradresse(utenlandskPostadresse);
		}
		return mottaker;
	}

	private Spraakkode getSpraakkode(SpraakKodeMapper spraakKodeMapper, Mottaker mottaker, String dokumenttypeId, String spraak) {
		log.info("Henter språkinfo for mottaker. dokumentTypeId={}", dokumenttypeId);
		//Sjekker språket på malen opp mot mottakers preferanser
		List<SpraakInfoTo> sprakinfos = dokmetConsumer.hentDokumenttypeInfoSpraak(dokumenttypeId);

		if (sprakinfos == null || sprakinfos.isEmpty()) {
			log.warn("Finner ikke språkinfo i DOKMET for dokumenttypeid={}.", dokumenttypeId);
		}

		return spraakKodeMapper.getSpraakKode(mottaker, spraak, sprakinfos);
	}

}
