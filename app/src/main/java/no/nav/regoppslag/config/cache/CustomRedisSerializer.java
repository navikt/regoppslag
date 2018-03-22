package no.nav.regoppslag.config.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.pool.KryoPool;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class CustomRedisSerializer implements RedisSerializer<Object> {
	
	private KryoPool kryoPool;

	public CustomRedisSerializer() {
		this.kryoPool = new KryoPool.Builder(Kryo::new).build();
	}
	
	@Override
	public byte[] serialize(Object o) throws SerializationException {
		ByteBufferOutput output = new ByteBufferOutput(10240);
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
	public Object deserialize(byte[] bytes) throws SerializationException {
		if(bytes.length == 0) return null;
		
		Kryo kryo = kryoPool.borrow();
		Object o;
		try {
			o = kryo.readClassAndObject(new ByteBufferInput(bytes));
		} finally {
			kryoPool.release(kryo);
		}
		return o;
	}
}
