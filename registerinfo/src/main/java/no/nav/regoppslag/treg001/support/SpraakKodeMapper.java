package no.nav.regoppslag.treg001.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokmet.api.tkat020.SpraakInfoTo;
import no.nav.regoppslag.exceptions.IngenGyldigEnumVerdiForSpraakKodeException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.EN;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NB;
import static no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode.NN;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
public class SpraakKodeMapper {

	public Spraakkode getSpraakKode(Mottaker mottaker, String mottakerSpraakKode, List<SpraakInfoTo> spraakInfoMal) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if (isBlank(mottakerSpraakKode)) {
			return getMaalFormNaarMottakerIkkeHarSattMaalform(mottaker, spraakInfoMal);
		} else { //Bruker har ikke satt språk
			return getMaalFormNaarMottakerHarSattMaalform(mottakerSpraakKode, spraakInfoMal);
		}
	}

	private Spraakkode getMaalFormNaarMottakerHarSattMaalform(String mottakerSpraakKode, List<SpraakInfoTo> spraakInfoMal) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if (spraakInfoMal == null) {
			return NB;
		}

		final String spraakKodeValue = mottakerSpraakKode.toUpperCase();
		//Dersom malen inneholder mottakers prefererte språk, ingen endring
		if (malInneholderSpraak(spraakInfoMal, spraakKodeValue)) {
			return mapToSpraakKode(spraakKodeValue);
		} else {
			//Malen finnes ikke på mottakers prefererte språk
			if (mottakerHarIkkeSkandinaviskSpraak(spraakKodeValue) && malInneholderSpraak(spraakInfoMal, "EN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Språket er ikke skandinavisk. Setter derfor språket til engelsk.", spraakKodeValue);
				return EN;
			} else if (malInneholderSpraak(spraakInfoMal, "NB")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til bokmål.", spraakKodeValue);
				//Fallback til bokmål hvis bokmål finnes
				return NB;
			} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til nynorsk.", spraakKodeValue);
				//Malen finnes på nynorsk
				return NN;
			} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til engelsk.", spraakKodeValue);
				//Malen finnes på engelsk
				return EN;
			} else {
				log.info("Malet inneholder ikke mottakerens prefererte språk {}. Setter språket til bokmål.", spraakKodeValue);
				//når alt annet feiler
				return NB;
			}
		}

	}

	private Spraakkode getMaalFormNaarMottakerIkkeHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			return NB;
		} else {
			String mottakerSpraakFraInput = mottaker.getSpraakkode() == null ? null : mottaker.getSpraakkode().name();
			if (malInneholderSpraak(spraakInfoMal, mottakerSpraakFraInput)) {
				return mottaker.getSpraakkode();
			} else if (malInneholderSpraak(spraakInfoMal, "NB")) {
				return NB;
			} else if (malInneholderSpraak(spraakInfoMal, "NN")) {
				return NN;
			} else if (malInneholderSpraak(spraakInfoMal, "EN")) {
				return EN;
			} else {
				return NB;
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

		return isFalse(Stream.of(NB.name(), NN.name(), "NO", "SV", "DA").anyMatch(spraak -> spraak.equalsIgnoreCase(mottakerSpraak)));
	}

	private Spraakkode mapToSpraakKode(String spraakKodeValue) throws IngenGyldigEnumVerdiForSpraakKodeException {
		if ("NO".equals(spraakKodeValue)) {
			return NB;
		}

		if (Arrays.stream(Spraakkode.values()).anyMatch(spraakkode -> spraakkode.name().equals(spraakKodeValue))) {
			return Spraakkode.valueOf(spraakKodeValue);
		} else {
			throw new IngenGyldigEnumVerdiForSpraakKodeException(String.format("Det finnes ingen SpraakKode Enum for SpraakKode verdi=%s", spraakKodeValue), BAD_REQUEST);
		}
	}
}
