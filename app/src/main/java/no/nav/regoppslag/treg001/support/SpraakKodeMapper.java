package no.nav.regoppslag.treg001.support;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dok.brevdata.felles.v1.navfelles.Mottaker;
import no.nav.dok.brevdata.felles.v1.simpletypes.Spraakkode;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.exceptions.IngenGyldigEnumVerdiForSpraakKodeException;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
			if (mottakerHarIkkeSkandinaviskSpraak(mottakerSpraakKode)) {
				return Spraakkode.EN;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "NB")) {
				//Har bruker satt nynorsk, men malen finnes på bokmål
				return Spraakkode.NB;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "NN")) {
				//Malen finnes på nynorsk
				return Spraakkode.NN;
			} else if (malInneholderSpraak(spraakInfoMalDokkat, "EN")) {
				//Malen finnes på engelsk
				return Spraakkode.EN;
			} else {

				//når alt annet feiler
				return Spraakkode.NB;
			}
		}

	}

	private Spraakkode getMaalFormNaarMottakerIkkeHarSattMaalform(Mottaker mottaker, List<SpraakInfoTo> spraakInfoMal) {
		if (spraakInfoMal == null) {
			return Spraakkode.NB;
		} else {
			if (malInneholderSpraak(spraakInfoMal, mottaker.getSpraakkode() == null ? null : mottaker.getSpraakkode().name())) {
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
