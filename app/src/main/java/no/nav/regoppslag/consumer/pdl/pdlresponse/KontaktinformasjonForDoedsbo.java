package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@Builder
public class KontaktinformasjonForDoedsbo {

	private Skifteform skifteform;
	private LocalDate attestutstedelsesdato;
	private PersonSomKontakt personSomKontakt;
	private AdvokatSomKontakt advokatSomKontakt;
	private OrganisasjonSomKontakt organisasjonSomKontakt;
	private KontaktAdresse adresse;

	public enum Skifteform {
		OFFENTLIG, ANNET
	}

	@Data
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
	public static class AdvokatSomKontakt {
		private Personnavn personnavn;
		private String organisasjonsnavn;
		private String organisasjonsnummer;
	}

	@Data
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

	public String getFulltnavn(Personnavn personnavn) {
		return (nonNull(personnavn)) ? personnavn.getFornavn() + " " +
				(isBlank(personnavn.getMellomnavn()) ? "" : personnavn.getMellomnavn() + " ") +
				personnavn.getEtternavn() : null;
	}
}
