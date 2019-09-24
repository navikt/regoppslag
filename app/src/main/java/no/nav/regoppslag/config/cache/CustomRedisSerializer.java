package no.nav.regoppslag.config.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class CustomRedisSerializer<T> implements RedisSerializer<T> {

	private final KryoPool kryoPool;

	private static final Integer MIN_BUFFER_SIZE=1024;

	public CustomRedisSerializer() {
		this.kryoPool = new KryoPool.Builder(Kryo::new).build();
	}
	
	@Override
	public byte[] serialize(T o)  {
		ByteBufferOutput output = new ByteBufferOutput(MIN_BUFFER_SIZE, -1); //-1 means maximum possible buffer size on VM.
		Kryo kryo = kryoPool.borrow();
		try {
			kryo.writeClassAndObject(output, o);
		} finally {
			kryoPool.release(kryo);
			output.close();
		}

		return output.toBytes();
	}
	
	@Override
	public T deserialize(byte[] bytes) {
		if(bytes.length == 0) {
			return null;
		}
		Kryo kryo = kryoPool.borrow();
		T o;
		try {
			o = (T)kryo.readClassAndObject(new ByteBufferInput(bytes));
		} finally {
			kryoPool.release(kryo);
		}
		return o;
	}
}
