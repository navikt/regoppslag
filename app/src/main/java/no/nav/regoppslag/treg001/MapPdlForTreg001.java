package no.nav.regoppslag.treg001;


import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.Person;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.dok.brevdata.felles.v1.simpletypes.AktoerType;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.regoppslag.consumer.organisasjonv4.OrganisasjonV4Consumer;
import no.nav.regoppslag.consumer.organisasjonv4.support.OrganisasjonV4Mapper;
import no.nav.regoppslag.consumer.pdl.PdlGraphQLConsumer;
import no.nav.regoppslag.consumer.pdl.map.MapPDLResponse;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import no.nav.regoppslag.service.LandkodeService;
import no.nav.regoppslag.treg001.to.MottakerTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.Objects.isNull;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.metrics.MetricLabels.SERVICE_CODE_TREG001;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Component
public class MapPdlForTreg001 {

	private final PdlGraphQLConsumer pdlGraphQLConsumer;
	private final MapPDLResponse mapPDLResponse;
	private final LandkodeService landkodeService;
	private final OrganisasjonV4Consumer organisasjonV4Consumer;
	private final OrganisasjonV4Mapper organisasjonV4Mapper;

	@Inject
	public MapPdlForTreg001(PdlGraphQLConsumer pdlGraphQLConsumer,
							MapPDLResponse mapPDLResponse, LandkodeService landkodeService, OrganisasjonV4Consumer organisasjonV4Consumer,
							OrganisasjonV4Mapper organisasjonV4Mapper) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
		this.mapPDLResponse = mapPDLResponse;
		this.landkodeService = landkodeService;
		this.organisasjonV4Consumer = organisasjonV4Consumer;
		this.organisasjonV4Mapper = organisasjonV4Mapper;
	}

	public Mottaker getMottakerFraPdl(String tema, Mottaker mottaker) {
		//Skal elementet berikes?
		if (mottaker.isBerik()) {
			if (AktoerType.PERSON.equals(mottaker.getTypeKode())) {
				final String id = mottaker.getId();
				PdlMottakerInfo hentPerson = mapPDLResponse.mapHentPerson(
						pdlGraphQLConsumer.hentPerson(mottaker.getId(), tema), SERVICE_CODE_TREG001);
				Mottaker mottakerFraPdl = mapAdresseFraPdl(hentPerson);
				mottaker.setKortNavn(mottakerFraPdl.getKortNavn());
				mottaker.setNavn(mottakerFraPdl.getNavn());
				mottaker.setMottakeradresse(mottakerFraPdl.getMottakeradresse());
			} else {
				Organisasjon organisasjon = organisasjonV4Consumer.hentOrganisasjon(mottaker.getId());
				MottakerTo mottakerTo = organisasjonV4Mapper.map(mottaker.getId(), organisasjon, SERVICE_CODE_TREG001);
				mottaker.setId(mottaker.getId());
				mottaker.setMottakeradresse(mottakerTo.getMottaker().getMottakeradresse());
				mottaker.setKortNavn(mottakerTo.getMottaker().getKortNavn());
				mottaker.setNavn(mottakerTo.getMottaker().getNavn());
			}
		}
		return mottaker;
	}

	public Mottaker mapAdresseFraPdl(PdlMottakerInfo pdlMottakerInfo) {
		Mottaker mottaker = new Person();

		if (isNull(pdlMottakerInfo.getPostadresse()) || isBlank(pdlMottakerInfo.getPostadresse().getAdresseType())) {
			throw new RegoppslagIllegalArgumentException("Mottaker adresse kan ikke bli null", BAD_REQUEST);
		}
		mottaker.setKortNavn(pdlMottakerInfo.getKortNavn());
		mottaker.setNavn(pdlMottakerInfo.getNavn());

		PostadresseTo postadresse = pdlMottakerInfo.getPostadresse();

		if (isNotBlank(postadresse.getAdresseType()) && POSTADRESSE_INNLAND.equalsIgnoreCase(postadresse.getAdresseType())) {
			NorskPostadresse norskPostadresse = new NorskPostadresse();
			norskPostadresse.setLand(isNotBlank(postadresse.getLandkode()) ? landkodeService.finnLandnavn(postadresse.getLandkode()) : null);
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

}
