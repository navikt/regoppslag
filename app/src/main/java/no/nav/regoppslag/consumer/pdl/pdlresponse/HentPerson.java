package no.nav.regoppslag.consumer.pdl.pdlresponse;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class HentPerson {
	private List<Adressebeskyttelse> adressebeskyttelse;
	private List<PersonNavn> navn;
	private List<Foedsel> foedsel;
	private List<Doedsfall> doedsfall;
	private List<Sikkerhetstiltak> sikkerhetstiltak;
	private List<Folkeregisteridentifikator> folkeregisteridentifikator;
	private List<Kontaktadresse> kontaktadresse;
	private List<Bostedsadresse> bostedsadresse;
	private List<Oppholdsadresse> oppholdsadresse;
	private List<KontaktinformasjonForDoedsbo> kontaktinformasjonForDoedsbo;
	private List<Folkeregisterpersonstatus> folkeregisterpersonstatus;
	private List<TilrettelagtKommunikasjon> tilrettelagtKommunikasjon;

	@Data
	@Builder
	public static class Adressebeskyttelse {
		private Gradering gradering;
	}

	public enum Gradering {
		STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG,
		FORTROLIG, UGRADERT
	}

	@Data
	@Builder
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

	@Data
	@Builder
	public static class Doedsfall {
		private LocalDate doedsdato;
	}

	@Data
	@Builder
	public static class Foedsel {
		private int foedselsaar;
		private LocalDate foedselsdato;
	}

	@Data
	@Builder
	public static class Sikkerhetstiltak {
		private String tiltakstype;
		private String beskrivelse;
	}

	@Data
	@Builder
	public static class Folkeregisteridentifikator {
		private String identifikasjonsnummer;
		private String type;
		private String status;
	}

	@Data
	@Builder
	public static class Folkeregisterpersonstatus {
		private String status;  //midlertidig,doed, bosatt
		private String forenkletStatus;
	}

	@Data
	@Builder
	public static class TilrettelagtKommunikasjon {
		private Tegnspraaktolk tegnspraaktolk;
		private Talespraaktolk talespraaktolk;
	}

	@Data
	public static class Tegnspraaktolk {
		private String spraak;
	}

	@Data
	public static class Talespraaktolk {
		private String spraak;
	}
}
