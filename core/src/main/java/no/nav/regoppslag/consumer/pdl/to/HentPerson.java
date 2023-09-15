package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.nav.regoppslag.exceptions.RegoppslagIllegalArgumentException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static no.nav.regoppslag.consumer.pdl.to.PDLConstant.PERSONSTATUS_DOED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HentPerson {
	private static final String FORNAVN = "Fornavn";
	private static final String ETTERNAVN = "Etternavn";
	private static final String UKJENT_KILDE = " Kilde Ukjent";
	private static final String ERROR_MELDING = "Feltet %s kan ikke være null eller tomt";

	private List<Adressebeskyttelse> adressebeskyttelse;
	private List<Foedsel> foedsel;
	private List<Doedsfall> doedsfall;
	private List<PersonNavn> navn;
	private List<Kontaktadresse> kontaktadresse;
	private List<Oppholdsadresse> oppholdsadresse;
	private List<Bostedsadresse> bostedsadresse;
	private List<Sikkerhetstiltak> sikkerhetstiltak;
	private List<Folkeregisteridentifikator> folkeregisteridentifikator;
	private List<KontaktinformasjonForDoedsbo> kontaktinformasjonForDoedsbo;
	private List<Folkeregisterpersonstatus> folkeregisterpersonstatus;

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Adressebeskyttelse {
		private Gradering gradering;
	}

	public enum Gradering {
		STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG,
		FORTROLIG, UGRADERT
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PersonNavn {
		@ToString.Exclude
		private String fornavn;
		@ToString.Exclude
		private String mellomnavn;
		@ToString.Exclude
		private String etternavn;
		@ToString.Exclude
		private String forkortetNavn;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Doedsfall {
		private LocalDate doedsdato;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Foedsel {
		private int foedselsaar;
		private LocalDate foedselsdato;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Sikkerhetstiltak {
		private String tiltakstype;
		private String beskrivelse;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Folkeregisteridentifikator {
		@ToString.Exclude
		private String identifikasjonsnummer;
		private String type;
		private String status;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Folkeregisterpersonstatus {
		private String status;  //midlertidig,doed, bosatt
		private String forenkletStatus;
		private Folkeregistermetadata folkeregistermetadata;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Folkeregistermetadata {
		private String kilde;
	}


	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TilrettelagtKommunikasjon {
		private Tegnspraaktolk tegnspraaktolk;
		private Talespraaktolk talespraaktolk;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Tegnspraaktolk {
		private String spraak;
	}

	@Getter
	@Setter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Talespraaktolk {
		private String spraak;
	}

	public String getFulltnavn() {
		return getNavn().stream().filter(Objects::nonNull)
				.map(HentPerson::mapPersonnavn)
				.findFirst().orElseThrow(() -> new RegoppslagIllegalArgumentException(String.format(ERROR_MELDING, "Personnavn"), BAD_REQUEST));

	}

	private static String mapPersonnavn(PersonNavn personNavn) {
		if (isBlank(personNavn.getFornavn()) || isBlank(personNavn.getEtternavn())) {
			throw new RegoppslagIllegalArgumentException(format(ERROR_MELDING, isBlank(personNavn.getFornavn()) ? FORNAVN : ETTERNAVN), BAD_REQUEST);
		}
		return Stream.of(personNavn.getFornavn(), personNavn.getMellomnavn(), personNavn.getEtternavn())
				.map(navn -> isBlank(navn) ? null : navn)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(" "))
				.trim();
	}

	public String getForkortetNavn() {
		return getNavn().stream()
				.map(PersonNavn::getForkortetNavn)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	public LocalDate getFoedselsdato() {
		return getFoedsel().stream()
				.map(Foedsel::getFoedselsdato)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	public String getIdentifikasjonsnummer() {
		return getFolkeregisteridentifikator().stream()
				.filter(Objects::nonNull)
				.map(Folkeregisteridentifikator::getIdentifikasjonsnummer)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	public String getFolkeregisterstatus() {
		return this.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(Folkeregisterpersonstatus::getStatus)
				.filter(Objects::nonNull)
				.findAny().orElse(null);
	}

	public Optional<LocalDate> getDoedsdato() {
		if (this.getDoedsfall() == null) {
			return Optional.empty();
		}
		return this.getDoedsfall().stream()
				.map(Doedsfall::getDoedsdato)
				.filter(Objects::nonNull)
				.findAny();
	}

	public boolean isDoed() {
		return getDoedsdato().isPresent() &&
				PERSONSTATUS_DOED.equals(this.getFolkeregisterstatus());
	}

	public Bostedsadresse getBostedsadresse() {
		if (bostedsadresse == null) {
			return null;
		}
		return bostedsadresse.stream()
				.filter(Objects::nonNull)
				.filter(Bostedsadresse::isNotExpired)
				.findAny()
				.orElse(null);
	}

	public String getFolkeregistermetadataKilde() {
		return this.getFolkeregisterpersonstatus().stream()
				.filter(Objects::nonNull)
				.map(Folkeregisterpersonstatus::getFolkeregistermetadata)
				.map(folkeregistermetadata ->
						nonNull(folkeregistermetadata) ? folkeregistermetadata.getKilde() : UKJENT_KILDE
				)
				.findAny().orElse(UKJENT_KILDE);
	}
}
