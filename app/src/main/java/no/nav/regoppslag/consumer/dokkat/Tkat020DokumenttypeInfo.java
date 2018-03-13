package no.nav.regoppslag.consumer.dokkat;

import no.nav.dokkat.api.tkat020.v3.DokumentTypeInfoToV3;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.regoppslag.config.fasit.DokumenttypeInfoV3Alias;
import no.nav.regoppslag.config.fasit.ServiceuserAlias;
import no.nav.regoppslag.exceptions.RegOppslagFunctionalException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ketill Fenne, Visma Consulting AS
 */
@Service
public class Tkat020DokumenttypeInfo {
	private final RestTemplate restTemplate;

	@Inject
	public Tkat020DokumenttypeInfo(RestTemplateBuilder restTemplateBuilder,
								   HttpComponentsClientHttpRequestFactory requestFactory,
								   DokumenttypeInfoV3Alias dokumenttypeInfoV3Alias,
								   ServiceuserAlias serviceuserAlias) {
		this.restTemplate = restTemplateBuilder
				.requestFactory(requestFactory)
				.rootUri(dokumenttypeInfoV3Alias.getUrl())
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout(dokumenttypeInfoV3Alias.getConnecttimeoutms())
				.setReadTimeout(dokumenttypeInfoV3Alias.getReadtimeoutms())
				.build();
	}

	@Cacheable(cacheNames ="hentDokumenttypeInfoSpraak")
	public List<SpraakInfoTo> hentDokumenttypeInfoSpraak(final String dokumenttypeId) throws RegOppslagFunctionalException{
		try {
			Map<String, Object> uriVariables = new HashMap<>();
			uriVariables.put("dokumenttypeId", dokumenttypeId);
			DokumentTypeInfoToV3 dokumentTypeInfoToV3 =  restTemplate.getForObject("/{dokumenttypeId}", DokumentTypeInfoToV3.class, uriVariables);
			if (dokumentTypeInfoToV3.getDokumentProduksjonsInfo() != null && dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos() != null) {
				return dokumentTypeInfoToV3.getDokumentProduksjonsInfo().getSpraakInfos();
			} else {
				return null;
			}

		} catch (HttpClientErrorException e) {
			throw new RegOppslagFunctionalException("TKAT020 failed with statusCode=" + e.getRawStatusCode() + ", message=" + e
					.getResponseBodyAsString(), e);
		} catch (HttpServerErrorException e) {
			throw new RegOppslagFunctionalException("TKAT020 failed with statusCode=" + e.getRawStatusCode(), e);
		}
	}
}