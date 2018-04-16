package no.nav.regoppslag.nais.checks;

import no.nav.regoppslag.nais.selftest.support.AbstractNaisIsReadyTest;
import no.nav.regoppslag.nais.selftest.support.ApplicationNotReadyException;
import no.nav.regoppslag.nais.selftest.support.Ping;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class RedisCacheCheck extends AbstractNaisIsReadyTest {
	
	public static final String REDIS_LABEL = "Redis";
	
	private LettuceConnectionFactory lettuceConnectionFactory;
	
	public RedisCacheCheck(LettuceConnectionFactory lettuceConnectionFactory) {
		super(Ping.Type.Redis,
				REDIS_LABEL,
				"",
				"");
		this.lettuceConnectionFactory = lettuceConnectionFactory;
	}
	
	@Override
	protected void doCheck() {
		try {
			lettuceConnectionFactory.getConnection()
					.getSubscription().isAlive();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping Redis cache", e);
		}
	}
	
}
