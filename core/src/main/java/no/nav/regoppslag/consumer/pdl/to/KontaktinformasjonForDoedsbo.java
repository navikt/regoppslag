package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@AllArgsConstructor
public class KontaktinformasjonForDoedsbo {

	private Skifteform skifteform;
	private LocalDate attestutstedelsesdato;
	private PersonSomKontakt personSomKontakt;
	private AdvokatSomKontakt advokatSomKontakt;
	private OrganisasjonSomKontakt organisasjonSomKontakt;
	private KontaktAdresse adresse;
	private Metadata metadata;

	public enum Skifteform {
		OFFENTLIG,
		ANNET
	}

	@Data
	@Builder
	@AllArgsConstructor
	public static class PersonSomKontakt {
		private LocalDate foedselsdato;
		private Personnavn personnavn;
		private String identifikasjonsnummer;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Personnavn {
		@ToString.Exclude
		private String fornavn;
		@ToString.Exclude
		private String mellomnavn;
		@ToString.Exclude
		private String etternavn;

		public String getFulltnavn() {
			return Stream.of(getFornavn(), getMellomnavn(), getEtternavn())
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(" "))
					.trim();
		}
	}

	@Data
	@Builder
	@AllArgsConstructor
	public static class AdvokatSomKontakt {
		private Personnavn personnavn;
		private String organisasjonsnavn;
		private String organisasjonsnummer;
	}

	@Data
	@Builder
	@AllArgsConstructor
	public static class OrganisasjonSomKontakt {
		private Personnavn kontaktperson;
		private String organisasjonsnavn;
		private String organisasjonsnummer;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class KontaktAdresse {
		private String adresselinje1;
		private String adresselinje2;
		private String poststedsnavn;
		private String postnummer;
		private String landkode;
	}
}
