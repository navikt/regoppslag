package no.nav.regoppslag.consumer.pdl.to;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@Builder
public class KontaktinformasjonForDoedsbo {

	private Skifteform skifteform;
	private LocalDate attestutstedelsesdato;
	private PersonSomKontakt personSomKontakt;
	private AdvokatSomKontakt advokatSomKontakt;
	private OrganisasjonSomKontakt organisasjonSomKontakt;
	private KontaktAdresse adresse;
	private Metadata metadata;

	public enum Skifteform {
		OFFENTLIG, ANNET
	}

	@Data
	@Builder
	public static class PersonSomKontakt {
		private LocalDate foedselsdato;
		private Personnavn personnavn;
		private String identifikasjonsnummer;
	}

	@Data
	public static class Personnavn {
		@ToString.Exclude
		private String fornavn;
		@ToString.Exclude
		private String mellomnavn;
		@ToString.Exclude
		private String etternavn;
	}

	@Data
	@Builder
	public static class AdvokatSomKontakt {
		private Personnavn personnavn;
		private String organisasjonsnavn;
		private String organisasjonsnummer;
	}

	@Data
	@Builder
	public static class OrganisasjonSomKontakt {
		private Personnavn kontaktperson;
		private String organisasjonsnavn;
		private String organisasjonsnummer;
	}

	@Data
	public static class KontaktAdresse {
		private String adresselinje1;
		private String adresselinje2;
		private String poststedsnavn;
		private String postnummer;
		private String landkode;
	}
}
