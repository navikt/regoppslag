package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder
@AllArgsConstructor
public class KontaktinformasjonForDoedsbo {

	Skifteform skifteform;
	LocalDate attestutstedelsesdato;
	PersonSomKontakt personSomKontakt;
	AdvokatSomKontakt advokatSomKontakt;
	OrganisasjonSomKontakt organisasjonSomKontakt;
	KontaktAdresse adresse;
	Metadata metadata;

	public enum Skifteform {
		OFFENTLIG,
		ANNET
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class PersonSomKontakt {
		LocalDate foedselsdato;
		Personnavn personnavn;
		String identifikasjonsnummer;
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class Personnavn {
		@ToString.Exclude
		String fornavn;
		@ToString.Exclude
		String mellomnavn;
		@ToString.Exclude
		String etternavn;

		public String getFulltnavn() {
			return Stream.of(getFornavn(), getMellomnavn(), getEtternavn())
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(" "))
					.trim();
		}
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class AdvokatSomKontakt {
		Personnavn personnavn;
		String organisasjonsnavn;
		String organisasjonsnummer;
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class OrganisasjonSomKontakt {
		Personnavn kontaktperson;
		String organisasjonsnavn;
		String organisasjonsnummer;
	}

	@Value
	@Builder
	@AllArgsConstructor
	public static class KontaktAdresse {
		String adresselinje1;
		String adresselinje2;
		String poststedsnavn;
		String postnummer;
		String landkode;
	}
}
