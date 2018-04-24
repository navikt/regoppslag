package no.nav.regoppslag.metrics;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusLabels {
	
	/**
	 * Labels
	 **/
	
	//Exception
	public static final String LABEL_TECHNICAL_EXCEPTION = "technical";
    public static final String LABEL_FUNCTIONAL_EXCEPTION = "functional";
	public static final String LABEL_SECURITY_EXCEPTION = "security";
	public static final String LABEL_EXCEPTION_DESCRIPTION = "exception_description";
	public static final String LABEL_EXCEPTION_NAME = "exception_name";
	public static final String LABEL_ERROR_TYPE = "error_type";
	
	//Request counter
	public static final String LABEL_PROCESS = "process";
	public static final String LABEL_SERVICE = "service";
    public static final String LABEL_TYPE = "type";
	public static final String LABEL_PROCESS_NAME = "process_name";
	public static final String LABEL_CONSUMER_ID = "consumer_name";
	public static final String LABEL_EVENT = "event";
	
	/**
	 * Metric tags
	 **/
	
	//Service
	public static final String SERVICE_CODE_TREG001 = "TREG001";
	public static final String SERVICE_CODE_TREG002="TREG002";
    
    //Cache
	public static final String LABEL_CACHE_NAME = "name";
	public static final String LABEL_CACHE_OPERATION = "operation";
	public static final String LABEL_CACHE_COUNTER = "cacheCounter";
	public static final String CACHE_MISS = "cacheMiss";
	public static final String CACHE_TOTAL = "cacheTotal";
	public static final String CACHE_ERROR = "cacheError";
	public static final String REDIS_CACHE = "redisCache";
	
	//Counter event and type
	public static final String MOTTAKERTYPE = "mottakerType";
	public static final String PLUGIN = "plugin";
	public static final String ADRESSEMAPPER = "adresse_mapper";
	public static final String REST = "Rest";
	public static final String PERSONV3 = "PersonV3";
	public static final String ORGANISASJONV4 = "OrganisasjonV4";
	public static final String ORGENHETKONTAKTV1 = "OrganisasjonEnhetKontaktinformasjonV1";
	public static final String LDAP = "LDAP";
	public static final String RECEIVED = "received";
	public static final String PROCESSED_OK = "processedOk";
	
}
