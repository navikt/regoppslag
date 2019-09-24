package no.nav.regoppslag.config.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.util.Pool;
import no.nav.dokkat.api.tkat020.v3.SpraakInfoTo;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.JuridiskEnhet;
import no.nav.tjeneste.virksomhet.organisasjonenhetkontaktinformasjon.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.ArrayList;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class CustomRedisSerializer<T> implements RedisSerializer<T> {
	
	private static final Integer MIN_BUFFER_SIZE=1024;
	private Pool<Kryo> kryoPool;

	public CustomRedisSerializer() {
		kryoPool = new Pool<Kryo>(true, false, 8) {
			protected Kryo create () {
				Kryo kryo = new Kryo();
				kryo.register(JuridiskEnhet.class);
				kryo.register(Organisasjonsenhet.class);
				kryo.register(Bruker.class);
				kryo.register(SpraakInfoTo.class);
				kryo.register(ArrayList.class);
				// Configure the Kryo instance.
				return kryo;
			}
		};
	}
	
	@Override
	public byte[] serialize(T o)  {
		ByteBufferOutput output = new ByteBufferOutput(MIN_BUFFER_SIZE, -1); //-1 means maximum possible buffer size on VM.
		Kryo kryo = kryoPool.obtain();
		try {
			kryo.writeClassAndObject(output, o);
		} finally {
			kryoPool.free(kryo);
			output.close();
		}

		return output.toBytes();
	}
	
	@Override
	public T deserialize(byte[] bytes) {
		if(bytes.length == 0) {
			return null;
		}
		Kryo kryo = kryoPool.obtain();
		T o;
		try {
			o = (T)kryo.readClassAndObject(new ByteBufferInput(bytes));
		} finally {
			kryoPool.free(kryo);
		}
		return o;
	}
}
