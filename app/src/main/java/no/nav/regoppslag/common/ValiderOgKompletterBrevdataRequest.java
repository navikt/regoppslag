package no.nav.regoppslag.common;

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
public class ValiderOgKompletterBrevdataRequest {
		private String dokumentTypeId;
		private String brevdata;
}
