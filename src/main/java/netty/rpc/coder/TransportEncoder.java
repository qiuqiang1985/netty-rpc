package netty.rpc.coder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;


/**
 * 对Transport进行编码
 * AllLen--KeyLen-->keyValue--valueLen-->value
 * @author steven.qiu
 *
 */
public class TransportEncoder extends OneToOneEncoder{

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		Transport transport = (Transport)msg;
		byte[] key = transport.getKey();
		byte[] value = transport.getValue();
		int keyLen = key.length;
		int valueLen = value.length;
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
		buf.writeInt(keyLen+valueLen+8);
		buf.writeInt(keyLen);
		buf.writeBytes(key);
		buf.writeInt(valueLen);
		buf.writeBytes(value);
		return buf;
	}

}
