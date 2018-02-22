package no.nav.regoppslag.treg001;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TO object used in POST
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegOppslagRequestTo {
		private String dokumentTypeId;
		private String brevdata;
}
