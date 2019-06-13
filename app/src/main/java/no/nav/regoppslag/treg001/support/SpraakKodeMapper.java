package no.nav.regoppslag.treg001.support;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.exceptions.IngenGyldigEnumVerdiForSpraakKodeException;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SpraakKodeMapper {

	public Spraakkode getSpraakKode(Mottaker mottaker, String mottakerSpraakKode, List<SpraakInfoTo> spraakInfoMal) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if (StringUtils.isEmpty(mottakerSpraakKode)) {
			return getMaalFormNaarMottakerIkkeHarSattMaalform(mottaker, spraakInfoMal);
		} else { //Bruker har ikke satt språk
			return getMaalFormNaarMottakerHarSattMaalform(mottakerSpraakKode, spraakInfoMal);
		}
	}

	private Spraakkode getMaalFormNaarMottakerHarSattMaalform(String mottakerSpraakKode, List<SpraakInfoTo> spraakInfoMalDokkat) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if (spraakInfoMalDokkat == null) {
			return Spraakkode.NB;
		}

		//Dersom malen inneholder mottakers prefererte språk, ingen endring
		if (malInneholderSpraak(spraakInfoMalDokkat, mottakerSpraakKode)) {
			return mapToSpraakKode(mottakerSpraakKode);
		} else {
			//Malen finnes ikke på mottakers prefererte språk
			if (mottakerHarIkkeSkandinaviskSpraak(mottakerSpraakKode) && malInneholderSpraak(spraakInfoMalDokkat, "EN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Språket er ikke skandinavisk. Setter derfor språket til engelsk.", mottakerSpraakKode);
				return Spraakkode.EN;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "NB")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til bokmål.", mottakerSpraakKode);
				//Fallback til bokmål hvis bokmål finnes
				return Spraakkode.NB;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "NN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til nynorsk.", mottakerSpraakKode);
				//Malen finnes på nynorsk
				return Spraakkode.NN;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "EN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til engelsk.", mottakerSpraakKode);
				//Malen finnes på engelsk
				return Spraakkode.EN;
			} else {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til bokmål.", mottakerSpraakKode);
				//når alt annet feiler
				return Spraakkode.NB;
			}
		}

	}

	private Spraakkode getMaalFormNaarMottakerIkkeHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			return Spraakkode.NB;
		} else {
			String mottakerSpraakFraInput = mottaker.getSpraakkode() == null ? null : mottaker.getSpraakkode().name();
			if (malInneholderSpraak(spraakInfoMal, mottakerSpraakFraInput)) {
				return mottaker.getSpraakkode();
			} else if (malInneholderSpraak(spraakInfoMal, "NB")) {
				return Spraakkode.NB;
			} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
				return Spraakkode.NN;
			} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
				return Spraakkode.EN;
			} else {
				return Spraakkode.NB;
			}
		}
	}


	private boolean malInneholderSpraak(final List<SpraakInfoTo> spraakInfoTos, final String spraak) {
		if ("NO".equalsIgnoreCase(spraak) || "NB".equalsIgnoreCase(spraak)) {
			// NO og NB skal begge bahandles som Bokmål
			return spraakInfoInneholderSpraak(spraakInfoTos, "NO") || spraakInfoInneholderSpraak(spraakInfoTos, "NB");
		} else {
			return spraakInfoInneholderSpraak(spraakInfoTos, spraak);
		}
	}

	private boolean spraakInfoInneholderSpraak(final List<SpraakInfoTo> spraakInfoTos, final String forventetSpraak) {
		return spraakInfoTos.stream().anyMatch(spraakInfo -> spraakInfo.getSpraaklag().equals(forventetSpraak));
	}

	private boolean mottakerHarIkkeSkandinaviskSpraak(String mottakerSpraak) {
		if (StringUtils.isEmpty(mottakerSpraak)) {
			return false;
		}

		return isFalse(Stream.of(Spraakkode.NB.name(), Spraakkode.NN.name(), "NO", "SV", "DA").anyMatch(spraak -> spraak.equalsIgnoreCase(mottakerSpraak)));
	}

	private Spraakkode mapToSpraakKode(String spraakKodeValue) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if ("NO".equals(spraakKodeValue)) {
			return Spraakkode.NB;
		}

		if (Arrays.stream(Spraakkode.values()).anyMatch(spraakkode -> spraakkode.name().equals(spraakKodeValue))) {
			return Spraakkode.valueOf(spraakKodeValue);
		} else {
			throw new IngenGyldigEnumVerdiForSpraakKodeException(String.format("Det finnes ingen SpraakKode Enum for SpraakKode verdi=%s", spraakKodeValue));
		}
	}
}
