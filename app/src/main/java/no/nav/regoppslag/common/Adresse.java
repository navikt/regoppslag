package no.nav.regoppslag.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Adresse {
	
	@ApiModelProperty(example = "Postboks 5 St Olavs Plass")
	private String adresselinje1;
	@ApiModelProperty(example = "null")
	private String adresselinje2;
	@ApiModelProperty(example = "null")
	private String adresselinje3;
	@ApiModelProperty(example = "0130")
	private String postnummer;
	@ApiModelProperty(example = "OSLO")
	private String poststed;
	@ApiModelProperty(example = "NO")
	private String landkode;
}
