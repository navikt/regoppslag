package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_INNLAND;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.POSTADRESSE_UTLAND;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Value
@Builder
@AllArgsConstructor
public class PostadresseTo {
	AdresseKildeCode adressekilde;
	String adresseType;
	String adresselinje1;
	String adresselinje2;
	String adresselinje3;
	String postnummer;
	String poststed;
	String landkode;

	public boolean erInnland() {
		return POSTADRESSE_INNLAND.equalsIgnoreCase(adresseType);
	}

	public boolean erUtland() {
		return POSTADRESSE_UTLAND.equalsIgnoreCase(adresseType);
	}

	public boolean erKomplettForDistribusjon() {
		return (erInnland() && isNotBlank(postnummer)) ||
				(erUtland() && isNotBlank(adresselinje1) && isNotBlank(landkode));
	}
}
