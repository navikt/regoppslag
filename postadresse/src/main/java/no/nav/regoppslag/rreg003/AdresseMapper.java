package no.nav.regoppslag.rreg003;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.navfelles.NorskPostadresse;
import no.nav.dok.brevdata.felles.v1.navfelles.UtenlandskPostadresse;
import no.nav.regoppslag.consumer.ereg.MottakerTo;
import no.nav.regoppslag.consumer.pdl.to.PdlMottakerInfo;
import no.nav.regoppslag.consumer.pdl.to.PostadresseTo;
import no.nav.regoppslag.exceptions.UkjentAdresseException;
import no.nav.regoppslag.service.LandkodeServiceNorsk;
import org.springframework.stereotype.Component;

import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static no.nav.regoppslag.domain.DomainConstants.UNKNOWN_LANDKODE;
import static no.nav.regoppslag.pdl.MapPDLResponse.UKJENT_ADRESSE_REASON_CODE;
import static no.nav.regoppslag.rreg003.PostadresseType.NORSKPOSTADRESSE;
import static no.nav.regoppslag.rreg003.PostadresseType.UTENLANDSKPOSTADRESSE;
import static no.nav.regoppslag.service.LandkodeService.finnLandkode;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@Slf4j
public class AdresseMapper {

	private static final String LANDKODE_NORGE = "NO";
	private static final String NAVN_NORGE = "NORGE";

	private final LandkodeServiceNorsk landkodeServiceNorsk;

	public AdresseMapper(LandkodeServiceNorsk landkodeServiceNorsk) {
		this.landkodeServiceNorsk = landkodeServiceNorsk;
	}

	public Adresse map(MottakerTo mottakerTo) {
		Mottaker mottaker = mottakerTo.getMottaker();

		if (mottaker.getMottakeradresse() instanceof NorskPostadresse norskPostadresse) {
			return Adresse.builder()
					.adresseKilde(mottakerTo.getAdresseKilde())
					.type(NORSKPOSTADRESSE)
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.land(norskPostadresse.getLand())
					.landkode(getLandkode(norskPostadresse.getLand()))
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed()).build();
		} else {
			UtenlandskPostadresse utenlandskPostadresse = (UtenlandskPostadresse) mottaker.getMottakeradresse();

			return Adresse.builder()
					.adresseKilde(mottakerTo.getAdresseKilde())
					.type(UTENLANDSKPOSTADRESSE)
					.adresselinje1(utenlandskPostadresse.getAdresselinje1())
					.adresselinje2(utenlandskPostadresse.getAdresselinje2())
					.adresselinje3(utenlandskPostadresse.getAdresselinje3())
					.land(landkodeServiceNorsk.finnLand(getLandkode(utenlandskPostadresse.getLand())))
					.landkode(getLandkode(utenlandskPostadresse.getLand())).build();
		}
	}

	public Adresse mapFraPdl(PdlMottakerInfo mottaker) {
		if (POSTADRESSE_INNLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			PostadresseTo norskPostadresse = mottaker.getPostadresse();

			return Adresse.builder()
					.adresseKilde(norskPostadresse.getAdressekilde())
					.type(NORSKPOSTADRESSE)
					.adresselinje1(norskPostadresse.getAdresselinje1())
					.adresselinje2(norskPostadresse.getAdresselinje2())
					.adresselinje3(norskPostadresse.getAdresselinje3())
					.postnummer(norskPostadresse.getPostnummer())
					.poststed(norskPostadresse.getPoststed())
					.land(isNotBlank(norskPostadresse.getLandkode()) ? landkodeServiceNorsk.finnLand(norskPostadresse.getLandkode()) : NAVN_NORGE)
					.landkode(isNotBlank(norskPostadresse.getLandkode()) ? norskPostadresse.getLandkode() : LANDKODE_NORGE)
					.build();
		} else if (POSTADRESSE_UTLAND.equals(mottaker.getPostadresse().getAdresseType())) {
			PostadresseTo utenlandskPostadresse = mottaker.getPostadresse();

			return Adresse.builder()
					.adresseKilde(utenlandskPostadresse.getAdressekilde())
					.type(UTENLANDSKPOSTADRESSE)
					.adresselinje1(isNotBlank(utenlandskPostadresse.getAdresselinje1()) ? utenlandskPostadresse.getAdresselinje1() : null)
					.adresselinje2(isNotBlank(utenlandskPostadresse.getAdresselinje2()) ? utenlandskPostadresse.getAdresselinje2() : null)
					.adresselinje3(isNotBlank(utenlandskPostadresse.getAdresselinje3()) ? utenlandskPostadresse.getAdresselinje3() : null)
					.land(landkodeServiceNorsk.finnLand(utenlandskPostadresse.getLandkode()))
					.landkode(utenlandskPostadresse.getLandkode())
					.build();
		}

		throw new UkjentAdresseException("RREG003: Kunne ikke mappe postadresse for postadresseType", UKJENT_ADRESSE_REASON_CODE);
	}

	private String getLandkode(String land) {
		String landkode = finnLandkode(land);

		if (landkode == null) {
			log.info("TREG002 Mottaker har ingen landkode registert. Setter landkode til \"{}\"", UNKNOWN_LANDKODE);
			return UNKNOWN_LANDKODE;
		}

		return landkode;
	}
}
