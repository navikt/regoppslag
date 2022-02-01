package no.nav.regoppslag.metrics;

public final class MetricLabels {
	public static final String DOK_REQUEST = "dok_request";
	public static final String DOK_CONSUMER = "dok_consumer";
	public static final String CONSUMER = "consumer";
	public static final String CONSUMER_NAME = "consumer_name";
	public static final String PROCESS_CODE = "process_code";
	public static final String PROCESS = "process";
	public static final String COMPONENT = "component";
	public static final String OPERATION = "operation";
	public static final String SERVICE = "service";
	public static final String EVENT = "event";
	public static final String TYPE = "type";

	//Service
	public static final String SERVICE_CODE_TREG001 = "TREG001";
	public static final String SERVICE_CODE_TREG002="TREG002";
	public static final String SERVICE_CODE_RREG003 = "RREG003";

	// Caches
	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	public static final String HENT_PERSON = "hentPerson";
	public static final String HENT_NAVN = "hentNavn";
	public static final String HENT_DOKKAT_SPRAAKINFO = "hentDokumenttypeInfoSpraak";
	public static final String RESTSTS_CACHE_NAME = "RESTSTS_CACHE_NAME";

	private MetricLabels() {
		//no-op
	}

	//Counter event and type
	public static final String ADRESSETYPE = "adresse_type";
	public static final String UKJENT_POSTNUMMER = "ukjent_postnummer";
	public static final String UKJENT_POSTSTED = "ukjent_poststed";
	public static final String LAND = "land";
	public static final String UNKNOWN_LANDKODE = "???";
	public static final String KOSOVO = "Kosovo, Republic of";
	public static final String KOSOVO_LANDKODE_NAV_REGISTRENE = "XXK";
	public static final String POSTSTED = "poststed";
	public static final String ORGANISASJONV4_MAPPER = "OrganisasjonV4Mapper";
	public static final String TREG002_ADRESSE_MAPPER = "Treg002AdresseMapper";


}
