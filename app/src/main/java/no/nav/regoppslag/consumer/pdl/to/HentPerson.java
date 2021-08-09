package no.nav.regoppslag.consumer.pdl.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HentPerson {
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
}
