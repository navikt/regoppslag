package no.nav.regoppslag.consumer.pdl.map;

import no.nav.regoppslag.consumer.pdl.to.PDLHentNavnResponse;
import no.nav.regoppslag.consumer.pdl.to.PersonNavn;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class HentNavnMapperTest {

	private static final String FORNAVN = "Fornavn";
	private static final String MELLOMNAVN = "Mellomnavn";
	private static final String ETTERNAVN = "Etternavn";
	private static final String FULLT_NAVN = "Fornavn Mellomnavn Etternavn";
	private static final String FORNAVN_ETTERNAVN = "Fornavn Etternavn";

	@ParameterizedTest
	@MethodSource
	void skalMappePDLHentNavnResponse(PDLHentNavnResponse response, String expected) {
		String actual = HentNavnMapper.mapNavn(response);

		assertThat(actual).isEqualTo(expected);
	}

	private static Stream<Arguments> skalMappePDLHentNavnResponse() {
		return Stream.of(
				Arguments.of(createPDLHentNavnResponse(FORNAVN, MELLOMNAVN, ETTERNAVN), FULLT_NAVN),
				Arguments.of(createPDLHentNavnResponse(FORNAVN, null, ETTERNAVN), FORNAVN_ETTERNAVN),
				Arguments.of(createPDLHentNavnResponse("   Fornavn   ", "   Mellomnavn   ", "   Etternavn   "), FULLT_NAVN)
		);
	}

	@Test
	void skalMappeNavnForDoedsbo() {
		PDLHentNavnResponse response = createPDLHentNavnResponse(FORNAVN, MELLOMNAVN, ETTERNAVN);
		String actual = HentNavnMapper.mapNavnForDoedsbo(response);

		assertThat(actual).isEqualTo(FULLT_NAVN);
	}


	@ParameterizedTest
	@MethodSource("pdlHentNavnResponseMedNull")
	void skalKasteRegoppslagIllegalArgumentExceptionHvisResponseErEllerInneholderNull(PDLHentNavnResponse response) {

		assertThatExceptionOfType(RegoppslagIllegalArgumentException.class)
				.isThrownBy(() -> HentNavnMapper.mapNavn(response))
				.withMessage("Personnavn kan ikke være null");
	}

	@ParameterizedTest
	@MethodSource("pdlHentNavnResponseMedNull")
	void skalRetunereNullForDoedsboHvisResponseErEllerInneholderNull(PDLHentNavnResponse response) {

		assertThat(HentNavnMapper.mapNavnForDoedsbo(response)).isNull();
	}

	private static Stream<Arguments> pdlHentNavnResponseMedNull() {
		PDLHentNavnResponse pdlHentNavnResponse = new PDLHentNavnResponse(null, null);
		PDLHentNavnResponse.PDLHentPerson pdlHentPerson = new PDLHentNavnResponse.PDLHentPerson(null);
		PDLHentNavnResponse.HentPerson hentPerson = new PDLHentNavnResponse.HentPerson(null);

		return Stream.of(
				null,
				Arguments.of(pdlHentNavnResponse),
				Arguments.of(new PDLHentNavnResponse(pdlHentPerson, null)),
				Arguments.of(new PDLHentNavnResponse(new PDLHentNavnResponse.PDLHentPerson(hentPerson), null)),
				Arguments.of(new PDLHentNavnResponse(new PDLHentNavnResponse.PDLHentPerson(new PDLHentNavnResponse.HentPerson(emptyList())), null))
		);
	}

	private static PDLHentNavnResponse createPDLHentNavnResponse(String fornavn, String mellomnavn, String etternavn) {
		var personNavn = new PersonNavn(fornavn, mellomnavn, etternavn, null);
		var hentPerson = new PDLHentNavnResponse.HentPerson(List.of(personNavn));
		var pdlHentPerson = new PDLHentNavnResponse.PDLHentPerson(hentPerson);

		return new PDLHentNavnResponse(pdlHentPerson, null);
	}

}