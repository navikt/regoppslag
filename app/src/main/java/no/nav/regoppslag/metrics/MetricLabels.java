package no.nav.regoppslag.metrics;

public final class MetricLabels {
	public static final String DOK_REQUEST = "dok_request";
	public static final String DOK_CONSUMER_REQUEST = "dok_consumer_request";
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

	// Caches
	public static final String HENT_ORGANISASJON = "hentOrganisasjon";
	public static final String HENT_PERSON = "hentPerson";
	public static final String HENT_DOKKAT_SPRAAKINFO = "hentDokumenttypeInfoSpraak";

	private MetricLabels() {
		//no-op
	}

	//Counter event and type
	public static final String MOTTAKERTYPE = "mottakerType";
	public static final String PLUGIN = "plugin";
	public static final String GENERELT = "generelt";
	public static final String ADRESSETYPE = "adresse_type";
	public static final String POSTNUMMER = "postnummer";
	public static final String UKJENT_POSTNUMMER = "ukjent_postnummer";
	public static final String UKJENT_POSTSTED = "ukjent_poststed";
	public static final String PERSON_DISKRESJONSKODE = "person_diskresjonskode";
	public static final String LAND = "land";
	public static final String UKJENT_LAND = "ukjent_land";
	public static final String POSTSTED = "poststed";
	public static final String REST = "Rest";
	public static final String PERSONV3 = "PersonV3";
	public static final String PERSONV3_MAPPER = "PersonV3Mapper";
	public static final String ORGANISASJONV4_MAPPER = "OrganisasjonV4Mapper";
	public static final String TREG002_ADRESSE_MAPPER = "Treg002AdresseMapper";
	public static final String ORGANISASJONV4 = "OrganisasjonV4";
	public static final String NORG2 = "NORG2";
	public static final String LDAP = "LDAP";
	public static final String RECEIVED = "received";
	public static final String PROCESSED_OK = "processed_ok";

}
